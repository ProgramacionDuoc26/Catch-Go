package cl.catchgo.app.domain.model

data class User(
    val id: String,
    val email: String,
    val role: UserRole,
    val fullName: String?,
    val nivel: Int = 1
)
