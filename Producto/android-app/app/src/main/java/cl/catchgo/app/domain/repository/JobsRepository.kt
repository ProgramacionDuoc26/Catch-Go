package cl.catchgo.app.domain.repository

import cl.catchgo.app.domain.model.JobFilter
import cl.catchgo.app.domain.model.JobOffer
import kotlinx.coroutines.flow.Flow

interface JobsRepository {
    fun observeOffers(filter: JobFilter): Flow<List<JobOffer>>
    suspend fun refresh(): Result<Unit>
    suspend fun getOffer(id: String): Result<JobOffer>
}
