package cl.catchgo.app.data.remote.dto

import cl.catchgo.app.domain.model.ApplicationStatus
import cl.catchgo.app.domain.model.JobApplication
import kotlinx.serialization.Serializable

@Serializable
data class ApplicationDto(
    val id: Long,
    val jobId: Long,
    val jobTitle: String? = null,
    val userId: String? = null,
    val estado: String? = null,
    val fechaPostulacion: String? = null
) {
    fun toDomain(): JobApplication = JobApplication(
        id = id.toString(),
        offerId = jobId.toString(),
        offerTitle = jobTitle ?: "Oferta #$jobId",
        company = "",
        comuna = "",
        message = null,
        status = when (estado?.uppercase()) {
            "ACEPTADO" -> ApplicationStatus.ACCEPTED
            "RECHAZADO" -> ApplicationStatus.REJECTED
            else -> ApplicationStatus.PENDING
        },
        createdAtIso = fechaPostulacion ?: ""
    )
}
