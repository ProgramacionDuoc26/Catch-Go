package cl.catchgo.app.domain.repository

import cl.catchgo.app.domain.model.JobApplication
import kotlinx.coroutines.flow.Flow

interface ApplicationsRepository {
    fun observeMyApplications(): Flow<List<JobApplication>>
    fun observeAppliedOfferIds(): Flow<Set<String>>
    suspend fun apply(offerId: String, message: String?): Result<JobApplication>
    suspend fun refreshFromBackend()
    suspend fun updateStatus(applicationId: Long, newStatus: String): Result<Unit>
    suspend fun cancelApplication(applicationId: Long): Result<Unit>
}
