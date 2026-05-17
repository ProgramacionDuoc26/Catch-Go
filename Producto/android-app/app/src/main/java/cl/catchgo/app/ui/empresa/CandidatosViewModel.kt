package cl.catchgo.app.ui.empresa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.core.util.MatchEngine
import cl.catchgo.app.data.local.SessionStore
import cl.catchgo.app.data.remote.JobsApi
import cl.catchgo.app.data.remote.ProfileApi
import cl.catchgo.app.data.remote.dto.ProfileRemoteDto
import cl.catchgo.app.data.remote.dto.UpdateStatusRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val estado: String,
    val workerProfile: ProfileRemoteDto?
)

data class CandidatosUiState(
    val isLoading: Boolean = true,
    val candidatos: List<CandidatoItem> = emptyList(),
    val empresaProfile: ProfileRemoteDto? = null,
    val error: String? = null
)

@HiltViewModel
class CandidatosViewModel @Inject constructor(
    private val jobsApi: JobsApi,
    private val profileApi: ProfileApi,
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

                val applications = jobsApi.listByEmployer(empresaId)

                val items = applications.map { app ->
                    async {
                        var workerProfile: ProfileRemoteDto? = null
                        var jobTitle = "Turno #${app.jobId}"
                        var remuneracion = 0
                        var matchScore = 70

                        runCatching {
                            val profResp = profileApi.getProfile(app.userId ?: "")
                            workerProfile = if (profResp.isSuccessful) profResp.body() else null

                            val job = jobsApi.get(app.jobId.toString())
                            jobTitle = job.titulo ?: jobTitle
                            remuneracion = job.remuneracion ?: 0

                            if (workerProfile != null && empresaProfile != null) {
                                matchScore = MatchEngine.calculate(workerProfile!!, empresaProfile, job).total
                            }
                        }

                        CandidatoItem(
                            applicationId = app.id,
                            jobId = app.jobId,
                            userId = app.userId ?: "",
                            nombre = workerProfile?.name ?: "Trabajador #${app.userId?.take(5)}",
                            jobTitle = jobTitle,
                            remuneracion = remuneracion,
                            descripcion = workerProfile?.description ?: "",
                            matchScore = matchScore,
                            estado = app.estado ?: "PENDIENTE",
                            workerProfile = workerProfile
                        )
                    }
                }.awaitAll().sortedByDescending { it.matchScore }

                _state.update { it.copy(isLoading = false, candidatos = items, empresaProfile = empresaProfile) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateStatus(applicationId: Long, newStatus: String) {
        viewModelScope.launch {
            runCatching {
                jobsApi.updateApplicationStatus(applicationId, UpdateStatusRequest(newStatus))
                _state.update { s ->
                    s.copy(candidatos = s.candidatos.map {
                        if (it.applicationId == applicationId) it.copy(estado = newStatus) else it
                    })
                }
            }
        }
    }

    fun submitRating(candidato: CandidatoItem, stars: Int) {
        viewModelScope.launch {
            runCatching {
                val profile = candidato.workerProfile ?: return@runCatching
                val count = profile.ratingCount ?: 0
                val current = profile.rating ?: 0.0
                val newCount = count + 1
                val newRating = (current * count + stars) / newCount
                val updated = profile.copy(rating = newRating, ratingCount = newCount)
                profileApi.saveProfile(updated)
                jobsApi.updateApplicationStatus(candidato.applicationId, UpdateStatusRequest("CALIFICADO_EMPRESA"))
                _state.update { s ->
                    s.copy(candidatos = s.candidatos.map {
                        if (it.applicationId == candidato.applicationId) it.copy(estado = "CALIFICADO_EMPRESA") else it
                    })
                }
            }
        }
    }
}
