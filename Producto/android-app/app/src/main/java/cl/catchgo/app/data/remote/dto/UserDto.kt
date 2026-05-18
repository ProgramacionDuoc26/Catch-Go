package cl.catchgo.app.data.remote.dto

import cl.catchgo.app.domain.model.User
import cl.catchgo.app.domain.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val email: String,
    val nombre: String? = null,
    val tipo: String? = null,
    val telefono: String? = null,
    val nivel: Int = 1
) {
    fun toDomain(): User = User(
        id = id.toString(),
        email = email,
        role = when (tipo?.uppercase()) {
            "TRABAJADOR" -> UserRole.WORKER
            "EMPRESA" -> UserRole.EMPRESA
            else -> UserRole.UNKNOWN
        },
        fullName = nombre,
        nivel = nivel
    )
}
