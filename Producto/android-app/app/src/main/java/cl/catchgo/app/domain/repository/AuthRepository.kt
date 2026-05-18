package cl.catchgo.app.domain.repository

import cl.catchgo.app.domain.model.RegisterInput
import cl.catchgo.app.domain.model.UserSession

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun register(input: RegisterInput): Result<UserSession>
    suspend fun logout()
    suspend fun verifyPassword(userId: String, password: String): Result<Boolean>
    suspend fun deleteAccount(userId: String): Result<Unit>
    suspend fun loginGoogle(email: String, displayName: String): Result<UserSession>
}
