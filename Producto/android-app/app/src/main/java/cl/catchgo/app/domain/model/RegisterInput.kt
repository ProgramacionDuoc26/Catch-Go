package cl.catchgo.app.domain.model

data class RegisterInput(
    val email: String,
    val password: String,
    val fullName: String,
    val rut: String,
    val phone: String,
    val role: UserRole
)
