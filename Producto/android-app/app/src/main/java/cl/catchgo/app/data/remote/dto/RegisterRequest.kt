package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val rut: String,
    val phone: String,
    val role: String
)
