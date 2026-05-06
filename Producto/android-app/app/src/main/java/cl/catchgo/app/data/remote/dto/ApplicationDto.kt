package cl.catchgo.app.data.remote.dto

import cl.catchgo.app.domain.model.ApplicationStatus
import cl.catchgo.app.domain.model.JobApplication
import kotlinx.serialization.Serializable

@Serializable
data class ApplicationDto(
    val id: String,
    val offerId: String,
    val offerTitle: String,
    val company: String,
    val comuna: String,
    val message: String? = null,
    val status: String,
    val createdAt: String
) {
    fun toDomain(): JobApplication = JobApplication(
        id = id,
        offerId = offerId,
        offerTitle = offerTitle,
        company = company,
        comuna = comuna,
        message = message,
        status = runCatching { ApplicationStatus.valueOf(status.uppercase()) }
            .getOrDefault(ApplicationStatus.PENDING),
        createdAtIso = createdAt
    )
}
