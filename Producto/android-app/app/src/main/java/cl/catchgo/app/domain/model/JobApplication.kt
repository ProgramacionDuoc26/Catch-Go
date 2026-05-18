package cl.catchgo.app.domain.model

data class JobApplication(
    val id: String,
    val offerId: String,
    val offerTitle: String,
    val company: String,
    val comuna: String,
    val message: String?,
    val status: ApplicationStatus,
    val createdAtIso: String,
    val rawStatus: String = "PENDIENTE"
)
