package cl.catchgo.app.data.repository

import cl.catchgo.app.data.local.SessionStore
import cl.catchgo.app.data.remote.ApiConfig
import cl.catchgo.app.data.remote.ApplicationsApi
import cl.catchgo.app.domain.model.ApplicationStatus
import cl.catchgo.app.domain.model.JobApplication
import cl.catchgo.app.domain.repository.ApplicationsRepository
import cl.catchgo.app.domain.repository.JobsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationsRepositoryImpl @Inject constructor(
    private val api: ApplicationsApi,
    private val jobsRepository: JobsRepository,
    private val sessionStore: SessionStore
) : ApplicationsRepository {

    private val cache = MutableStateFlow<List<JobApplication>>(
        if (ApiConfig.USE_MOCK_APPLICATIONS) MockApplications.preseed else emptyList()
    )

    override fun observeMyApplications(): Flow<List<JobApplication>> = cache.asStateFlow()

    override fun observeAppliedOfferIds(): Flow<Set<String>> =
        cache.map { list -> list.map { it.offerId }.toSet() }

    override suspend fun apply(offerId: String, message: String?): Result<JobApplication> = runCatching {
        if (cache.value.any { it.offerId == offerId }) {
            throw IllegalStateException("Ya postulaste a esta oferta")
        }
        if (ApiConfig.USE_MOCK_APPLICATIONS) {
            delay(600)
            val offer = jobsRepository.getOffer(offerId).getOrThrow()
            val application = JobApplication(
                id = "a-${System.currentTimeMillis()}",
                offerId = offerId,
                offerTitle = offer.title,
                company = offer.company,
                comuna = offer.comuna,
                message = message?.takeIf { it.isNotBlank() },
                status = ApplicationStatus.PENDING,
                createdAtIso = Instant.now().toString(),
                photoUrl = offer.photoUrl
            )
            cache.value = listOf(application) + cache.value
            application
        } else {
            val userId = sessionStore.session.first()?.user?.id
                ?: throw IllegalStateException("Sin sesión activa")
            val response = api.apply(jobId = offerId.toLong(), userId = userId)
            if (!response.isSuccessful) {
                throw IllegalStateException("Error al postular: ${response.code()}")
            }
            val offer = jobsRepository.getOffer(offerId).getOrNull()
            val application = JobApplication(
                id = "a-${System.currentTimeMillis()}",
                offerId = offerId,
                offerTitle = offer?.title ?: "Oferta #$offerId",
                company = offer?.company ?: "Empresa",
                comuna = offer?.comuna ?: "Santiago",
                message = message?.takeIf { it.isNotBlank() },
                status = ApplicationStatus.PENDING,
                rawStatus = "PENDIENTE",
                createdAtIso = Instant.now().toString(),
                photoUrl = offer?.photoUrl
            )
            cache.value = listOf(application) + cache.value
            application
        }
    }

    override suspend fun refreshFromBackend() = loadFromBackend()

    override suspend fun updateStatus(applicationId: Long, newStatus: String): Result<Unit> = runCatching {
        if (ApiConfig.USE_MOCK_APPLICATIONS) {
            delay(500)
            cache.value = cache.value.map { app ->
                if (app.id == applicationId.toString()) {
                    val statusEnum = when (newStatus) {
                        "ACEPTADO" -> ApplicationStatus.ACCEPTED
                        "RECHAZADO" -> ApplicationStatus.REJECTED
                        else -> app.status
                    }
                    app.copy(rawStatus = newStatus, status = statusEnum)
                } else app
            }
        } else {
            val response = api.updateApplicationStatus(applicationId, cl.catchgo.app.data.remote.dto.UpdateStatusRequest(newStatus))
            if (!response.isSuccessful) {
                throw IllegalStateException("Error al actualizar estado: ${response.code()}")
            }
            // Update local cache
            cache.value = cache.value.map { app ->
                if (app.id == applicationId.toString()) {
                    val statusEnum = when (newStatus) {
                        "ACEPTADO" -> ApplicationStatus.ACCEPTED
                        "RECHAZADO" -> ApplicationStatus.REJECTED
                        else -> app.status
                    }
                    app.copy(rawStatus = newStatus, status = statusEnum)
                } else app
            }
        }
    }

    override suspend fun cancelApplication(applicationId: Long): Result<Unit> = runCatching {
        if (ApiConfig.USE_MOCK_APPLICATIONS) {
            delay(500)
            cache.value = cache.value.filter { it.id != applicationId.toString() }
        } else {
            val response = api.deleteApplication(applicationId)
            if (!response.isSuccessful) {
                throw IllegalStateException("Error al cancelar postulación: ${response.code()}")
            }
            cache.value = cache.value.filter { it.id != applicationId.toString() }
        }
    }

    suspend fun loadFromBackend() {
        if (ApiConfig.USE_MOCK_APPLICATIONS) return
        val userId = sessionStore.session.first()?.user?.id ?: return
        val remote = runCatching {
            val list = api.listByUser(userId)
            list.map { dto ->
                val domain = dto.toDomain()
                val offer = jobsRepository.getOffer(dto.jobId.toString()).getOrNull()
                if (offer != null) {
                    domain.copy(company = offer.company, comuna = offer.comuna, photoUrl = offer.photoUrl)
                } else domain
            }
        }.getOrNull() ?: return
        cache.value = remote
    }
}
