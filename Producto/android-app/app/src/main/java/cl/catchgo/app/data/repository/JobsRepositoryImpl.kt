package cl.catchgo.app.data.repository

import cl.catchgo.app.data.remote.ApiConfig
import cl.catchgo.app.data.remote.JobsApi
import cl.catchgo.app.domain.model.JobFilter
import cl.catchgo.app.domain.model.JobOffer
import cl.catchgo.app.domain.repository.JobsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobsRepositoryImpl @Inject constructor(
    private val api: JobsApi
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
            cache.value = api.list().map { it.toDomain() }
        }
    }

    override suspend fun getOffer(id: String): Result<JobOffer> = runCatching {
        if (ApiConfig.USE_MOCK_JOBS) {
            delay(300)
            cache.value.firstOrNull { it.id == id }
                ?: throw NoSuchElementException("Oferta $id no encontrada")
        } else {
            api.get(id).toDomain()
        }
    }
}
