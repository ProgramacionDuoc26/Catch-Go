package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileRemoteDto(
    val id: Long? = null,
    val userId: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val birthDate: String? = null,
    val photoUrl: String? = null,
    val cvUrl: String? = null,
    val description: String? = null,
    val type: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class FileUploadResponse(
    val url: String,
    val fileName: String
)
