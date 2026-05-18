package cl.catchgo.app.ui.empresa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.data.local.SessionStore
import cl.catchgo.app.data.remote.JobsApi
import cl.catchgo.app.data.remote.ProfileApi
import cl.catchgo.app.data.remote.dto.JobOfferDto
import cl.catchgo.app.data.remote.dto.MatchOfertaDto
import cl.catchgo.app.data.remote.dto.MatchSolicitudDto
import cl.catchgo.app.data.remote.dto.MatchTrabajadorDto
import cl.catchgo.app.data.remote.dto.ProfileRemoteDto
import cl.catchgo.app.data.remote.dto.UpdateStatusRequest
import cl.catchgo.app.domain.repository.MatchingRepository
import cl.catchgo.app.util.MatchEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class CandidatoItem(
    val applicationId: Long,
    val jobId: Long,
    val userId: String,
    val nombre: String,
    val jobTitle: String,
    val remuneracion: Int,
    val descripcion: String,
    val matchScore: Int,
    val puntajeDistancia: Int,
    val puntajeHabilidades: Int,
    val estado: String,
    val workerProfile: ProfileRemoteDto?
)

data class CandidatosUiState(
    val isLoading: Boolean = true,
    val candidatos: List<CandidatoItem> = emptyList(),
    val empresaProfile: ProfileRemoteDto? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class CandidatosViewModel @Inject constructor(
    private val jobsApi: JobsApi,
    private val profileApi: ProfileApi,
    private val matchingRepository: MatchingRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    val state: StateFlow<CandidatosUiState> get() = _state
    private val _state = MutableStateFlow(CandidatosUiState())

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val session = sessionStore.session.first() ?: return@launch
                val empresaId = session.user.id

                val empProfileResponse = profileApi.getProfile(empresaId)
                val empresaProfile = if (empProfileResponse.isSuccessful) empProfileResponse.body() else null
                val empresaSkills = parseJson(empresaProfile?.skills)

                val applications = jobsApi.listByEmployer(empresaId)

                // Fetch worker profiles + jobs in parallel
                data class AppData(
                    val applicationId: Long,
                    val jobId: Long,
                    val userId: String,
                    val estado: String,
                    val job: JobOfferDto?,
                    val workerProfile: ProfileRemoteDto?
                )

                val appData = applications.map { app ->
                    async {
                        var job: JobOfferDto? = null
                        var workerProfile: ProfileRemoteDto? = null
                        runCatching {
                            job = jobsApi.get(app.jobId.toString())
                            val profResp = profileApi.getProfile(app.userId ?: "")
                            workerProfile = if (profResp.isSuccessful) profResp.body() else null
                        }
                        AppData(app.id, app.jobId, app.userId ?: "", app.estado ?: "PENDIENTE", job, workerProfile)
                    }
                }.awaitAll()

                // Group by jobId to call matching once per offer
                val byJob = appData.groupBy { it.jobId }

                // For each job group, run matching and collect scores per workerId
                val scoreMap = mutableMapOf<String, Triple<Int, Int, Int>>() // userId -> (total, habilidades, distancia)
                byJob.forEach { (jobId, group) ->
                    val job = group.firstOrNull()?.job ?: return@forEach
                    val workers = group.mapNotNull { it.workerProfile }
                    if (workers.isEmpty()) return@forEach

                    val solicitud = buildSolicitud(
                        job = job,
                        empresaId = empresaId,
                        empresaProfile = empresaProfile,
                        empresaSkills = empresaSkills,
                        workers = workers
                    )
                    
                    val remoteScores = matchingRepository.ejecutarMatching(solicitud).getOrNull()
                    if (remoteScores != null && remoteScores.isNotEmpty()) {
                        remoteScores.forEach { sug ->
                            val uid = sug.trabajadorPerfilId.toString()
                            scoreMap[uid] = Triple(
                                sug.puntajeTotal.toInt(),
                                sug.puntajeHabilidades.toInt(),
                                sug.puntajeDistancia.toInt()
                            )
                        }
                    } else {
                        // Fallback local: Si el microservicio remoto falla, calculamos localmente.
                        val jobDomain = job.toDomain()
                        workers.forEach { worker ->
                            val workerId = worker.id ?: return@forEach
                            val score = MatchEngine.calculate(
                                worker = worker,
                                company = empresaProfile,
                                offer = jobDomain,
                                plan = empresaProfile?.plan ?: "FREE"
                            )
                            scoreMap[workerId.toString()] = Triple(score, 0, 0)
                        }
                    }
                }

                val items = appData.map { app ->
                    val scores = scoreMap[app.workerProfile?.id?.toString()]
                    CandidatoItem(
                        applicationId = app.applicationId,
                        jobId = app.jobId,
                        userId = app.userId,
                        nombre = app.workerProfile?.name ?: "Trabajador #${app.userId.take(5)}",
                        jobTitle = app.job?.titulo ?: "Turno #${app.jobId}",
                        remuneracion = app.job?.remuneracion ?: 0,
                        descripcion = app.workerProfile?.description ?: "",
                        matchScore = scores?.first ?: 0,
                        puntajeHabilidades = scores?.second ?: 0,
                        puntajeDistancia = scores?.third ?: 0,
                        estado = app.estado,
                        workerProfile = app.workerProfile
                    )
                }.sortedByDescending { it.matchScore }

                _state.update { it.copy(isLoading = false, candidatos = items, empresaProfile = empresaProfile) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateStatus(applicationId: Long, newStatus: String) {
        viewModelScope.launch {
            val result = runCatching {
                val response = jobsApi.updateApplicationStatus(applicationId, UpdateStatusRequest(newStatus))
                if (!response.isSuccessful) {
                    throw IllegalStateException("Error al actualizar estado: ${response.code()}")
                }
            }
            result.onSuccess {
                val msg = when (newStatus) {
                    "ACEPTADO" -> "Postulación aceptada con éxito"
                    "RECHAZADO" -> "Postulación rechazada"
                    "PAGO_ENVIADO" -> "Pago enviado con éxito"
                    "PAGO_CONFIRMADO" -> "Pago confirmado con éxito"
                    else -> "Estado actualizado con éxito"
                }
                _state.update { s ->
                    s.copy(
                        successMessage = msg,
                        candidatos = s.candidatos.map {
                            if (it.applicationId == applicationId) it.copy(estado = newStatus) else it
                        }
                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(error = "Error al actualizar estado: ${e.localizedMessage}") }
            }
        }
    }

    fun submitRating(candidato: CandidatoItem, stars: Int) {
        viewModelScope.launch {
            val result = runCatching {
                val profile = candidato.workerProfile
                if (profile != null) {
                    val count = profile.ratingCount ?: 0
                    val current = profile.rating ?: 0.0
                    val newCount = count + 1
                    val newRating = (current * count + stars) / newCount
                    val updated = profile.copy(rating = newRating, ratingCount = newCount)
                    profileApi.saveProfile(updated)
                }
                
                val targetStatus = if (candidato.estado == "CALIFICADO_TRABAJADOR") "FINALIZADA" else "CALIFICADO_EMPRESA"
                val response = jobsApi.updateApplicationStatus(candidato.applicationId, UpdateStatusRequest(targetStatus))
                if (!response.isSuccessful) {
                    throw IllegalStateException("Error al actualizar estado: ${response.code()}")
                }
                targetStatus
            }
            result.onSuccess { targetStatus ->
                _state.update { s ->
                    s.copy(
                        successMessage = "Calificación enviada con éxito",
                        candidatos = s.candidatos.map {
                            if (it.applicationId == candidato.applicationId) it.copy(estado = targetStatus) else it
                        }
                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(error = "Error al calificar: ${e.localizedMessage}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearMessage() {
        _state.update { it.copy(successMessage = null) }
    }

    private fun buildSolicitud(
        job: JobOfferDto,
        empresaId: String,
        empresaProfile: ProfileRemoteDto?,
        empresaSkills: JSONObject,
        workers: List<ProfileRemoteDto>
    ): MatchSolicitudDto {
        val habilidadValorada = empresaSkills.optString("habilidadValorada", "")
        val habilidadesRequeridas = if (habilidadValorada.isNotBlank()) listOf(habilidadValorada) else emptyList()

        val oferta = MatchOfertaDto(
            id = job.id,
            titulo = job.titulo,
            empresaId = empresaId.toLongOrNull(),
            habilidadesRequeridas = habilidadesRequeridas,
            latitud = job.latitude ?: empresaProfile?.latitude,
            longitud = job.longitude ?: empresaProfile?.longitude
        )

        val trabajadores = workers.mapNotNull { w ->
            val wId = w.id ?: return@mapNotNull null
            val wSkills = parseJson(w.skills)
            MatchTrabajadorDto(
                id = wId,
                nombre = w.name,
                habilidades = parseHabilidades(wSkills),
                latitud = w.latitude,
                longitud = w.longitude
            )
        }

        return MatchSolicitudDto(ofertaTrabajo = oferta, trabajadores = trabajadores)
    }

    private fun parseJson(json: String?): JSONObject =
        if (json.isNullOrBlank()) JSONObject()
        else runCatching { JSONObject(json) }.getOrDefault(JSONObject())

    private fun parseHabilidades(skills: JSONObject): List<String> {
        val arr: JSONArray = skills.optJSONArray("habilidades") ?: return emptyList()
        return (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } }
    }
}
