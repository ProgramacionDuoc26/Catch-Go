package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifyPasswordRequest(
    val userId: String,
    val password: String
)
