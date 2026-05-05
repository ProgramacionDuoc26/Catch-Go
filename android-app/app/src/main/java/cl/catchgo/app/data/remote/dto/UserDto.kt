package cl.catchgo.app.data.remote.dto

import cl.catchgo.app.domain.model.User
import cl.catchgo.app.domain.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val role: String,
    val fullName: String? = null
) {
    fun toDomain(): User = User(
        id = id,
        email = email,
        role = runCatching { UserRole.valueOf(role.uppercase()) }.getOrDefault(UserRole.UNKNOWN),
        fullName = fullName
    )
}
