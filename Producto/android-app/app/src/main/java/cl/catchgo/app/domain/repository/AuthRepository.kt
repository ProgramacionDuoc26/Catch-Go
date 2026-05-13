package cl.catchgo.app.domain.repository

import cl.catchgo.app.domain.model.RegisterInput
import cl.catchgo.app.domain.model.UserSession

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun register(input: RegisterInput): Result<UserSession>
    suspend fun logout()
}
