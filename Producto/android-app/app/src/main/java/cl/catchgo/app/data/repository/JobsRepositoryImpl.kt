package cl.catchgo.app.data.repository

import cl.catchgo.app.data.local.SessionStore
import cl.catchgo.app.data.remote.ApiConfig
import cl.catchgo.app.data.remote.JobsApi
import cl.catchgo.app.domain.model.JobFilter
import cl.catchgo.app.domain.model.JobOffer
import cl.catchgo.app.domain.repository.JobsRepository
import cl.catchgo.app.domain.repository.ProfileRepository
import cl.catchgo.app.util.MatchEngine
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobsRepositoryImpl @Inject constructor(
    private val api: JobsApi,
    private val sessionStore: SessionStore,
    private val profileRepository: ProfileRepository
) : JobsRepository {

    private val cache = MutableStateFlow(MockJobs.all)

    override fun observeOffers(filter: JobFilter): Flow<List<JobOffer>> =
        cache.map { offers ->
            offers
                .let { if (filter.category == null) it else it.filter { o -> o.category == filter.category } }
                .sortedByDescending { it.score }
        }

    override suspend fun refresh(): Result<Unit> = runCatching {
        if (ApiConfig.USE_MOCK_JOBS) {
            delay(700)
            cache.value = MockJobs.all.shuffled()
        } else {
            val dtoList = api.list()
            val session = sessionStore.session.first()
            val workerProfile = session?.user?.id?.let { profileRepository.getProfile(it).getOrNull() }
            
            // Parallel fetch of company profiles to map names and match scores
            val companyProfiles = coroutineScope {
                dtoList.mapNotNull { it.empresaId }.distinct().associateWith { id ->
                    async { profileRepository.getProfile(id).getOrNull() }
                }.mapValues { it.value.await() }
            }

            cache.value = dtoList.map { dto ->
                val companyProfile = companyProfiles[dto.empresaId]
                val companyName = companyProfile?.name ?: if (!dto.empresaId.isNullOrBlank()) "Empresa #${dto.empresaId}" else "Sin empresa"
                
                val baseOffer = dto.toDomain().copy(company = companyName)
                
                val score = MatchEngine.calculate(
                    worker = workerProfile,
                    company = companyProfile,
                    offer = baseOffer,
                    plan = workerProfile?.plan ?: "TRIAL"
                )
                
                val dist = MatchEngine.calculateDistance(
                    lat1 = workerProfile?.latitude,
                    lon1 = workerProfile?.longitude,
                    lat2 = dto.latitude ?: companyProfile?.latitude,
                    lon2 = dto.longitude ?: companyProfile?.longitude
                )

                baseOffer.copy(
                    score = score,
                    latitude = dto.latitude ?: companyProfile?.latitude,
                    longitude = dto.longitude ?: companyProfile?.longitude,
                    empresaId = dto.empresaId,
                    distanceKm = if (dist >= 0.0) dist else null,
                    photoUrl = companyProfile?.photoUrl
                )
            }.sortedByDescending { it.score }
        }
    }

    override suspend fun getOffer(id: String): Result<JobOffer> = runCatching {
        if (ApiConfig.USE_MOCK_JOBS) {
            delay(300)
            cache.value.firstOrNull { it.id == id }
                ?: throw NoSuchElementException("Oferta $id no encontrada")
        } else {
            val dto = api.get(id)
            val session = sessionStore.session.first()
            val workerProfile = session?.user?.id?.let { profileRepository.getProfile(it).getOrNull() }
            val companyProfile = dto.empresaId?.let { profileRepository.getProfile(it).getOrNull() }
            val companyName = companyProfile?.name ?: if (!dto.empresaId.isNullOrBlank()) "Empresa #${dto.empresaId}" else "Sin empresa"

            val baseOffer = dto.toDomain().copy(company = companyName)

            val score = MatchEngine.calculate(
                worker = workerProfile,
                company = companyProfile,
                offer = baseOffer,
                plan = workerProfile?.plan ?: "TRIAL"
            )

            val dist = MatchEngine.calculateDistance(
                lat1 = workerProfile?.latitude,
                lon1 = workerProfile?.longitude,
                lat2 = dto.latitude ?: companyProfile?.latitude,
                lon2 = dto.longitude ?: companyProfile?.longitude
            )

            baseOffer.copy(
                score = score,
                latitude = dto.latitude ?: companyProfile?.latitude,
                longitude = dto.longitude ?: companyProfile?.longitude,
                empresaId = dto.empresaId,
                distanceKm = if (dist >= 0.0) dist else null,
                photoUrl = companyProfile?.photoUrl
            )
        }
    }
}
