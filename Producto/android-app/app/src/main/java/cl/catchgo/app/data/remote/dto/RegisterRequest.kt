package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val nombre: String,
    val telefono: String,
    val tipo: String
)
