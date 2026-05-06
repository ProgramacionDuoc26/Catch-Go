package cl.catchgo.app.data.repository

import cl.catchgo.app.data.remote.ApiConfig
import cl.catchgo.app.data.remote.ApplicationsApi
import cl.catchgo.app.data.remote.dto.CreateApplicationRequest
import cl.catchgo.app.domain.model.ApplicationStatus
import cl.catchgo.app.domain.model.JobApplication
import cl.catchgo.app.domain.repository.ApplicationsRepository
import cl.catchgo.app.domain.repository.JobsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationsRepositoryImpl @Inject constructor(
    private val api: ApplicationsApi,
    private val jobsRepository: JobsRepository
) : ApplicationsRepository {

    private val cache = MutableStateFlow(MockApplications.preseed)

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
                createdAtIso = Instant.now().toString()
            )
            cache.value = listOf(application) + cache.value
            application
        } else {
            val dto = api.create(CreateApplicationRequest(offerId = offerId, message = message))
            val domain = dto.toDomain()
            cache.value = listOf(domain) + cache.value
            domain
        }
    }
}
