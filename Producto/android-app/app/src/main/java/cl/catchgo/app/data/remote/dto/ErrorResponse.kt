package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val timestamp: String? = null,
    val status: Int? = null,
    val error: String = "",
    val message: String = "",
    val path: String? = null,
    @SerialName("trace") val trace: String? = null
)
