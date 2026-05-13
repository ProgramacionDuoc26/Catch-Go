package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val usuario: UserDto
)
