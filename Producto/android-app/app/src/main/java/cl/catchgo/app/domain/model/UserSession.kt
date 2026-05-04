package cl.catchgo.app.domain.model

data class UserSession(
    val token: String,
    val user: User
)
