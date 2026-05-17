package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EmployerApplicationDto(
    val id: Long,
    val jobId: Long,
    val userId: String? = null,
    val estado: String? = null,
    val fechaPostulacion: String? = null
)

@Serializable
data class UpdateStatusRequest(
    val status: String
)

@Serializable
data class CreateJobOfferRequest(
    val titulo: String,
    val descripcion: String,
    val ubicacion: String,
    val categoria: String,
    val remuneracion: Int,
    val fechaInicio: String,
    val fechaFin: String? = null,
    val empresaId: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)
