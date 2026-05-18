package cl.catchgo.app.data.repository

import cl.catchgo.app.data.local.SessionStore
import cl.catchgo.app.data.remote.ApiConfig
import cl.catchgo.app.data.remote.AuthApi
import cl.catchgo.app.data.remote.dto.LoginRequest
import cl.catchgo.app.data.remote.dto.RegisterRequest
import cl.catchgo.app.data.remote.dto.VerifyPasswordRequest
import cl.catchgo.app.domain.model.RegisterInput
import cl.catchgo.app.domain.model.User
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.domain.model.UserSession
import cl.catchgo.app.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionStore: SessionStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<UserSession> = runCatching {
        val session = if (ApiConfig.USE_MOCK_AUTH) mockLogin(email) else realLogin(email, password)
        sessionStore.save(session)
        session
    }

    override suspend fun register(input: RegisterInput): Result<UserSession> = runCatching {
        val session = if (ApiConfig.USE_MOCK_AUTH) mockRegister(input) else realRegister(input)
        sessionStore.save(session)
        session
    }

    override suspend fun logout() {
        sessionStore.clear()
    }

    override suspend fun verifyPassword(userId: String, password: String): Result<Boolean> = runCatching {
        if (ApiConfig.USE_MOCK_AUTH) return Result.success(true)
        val response = api.verifyPassword(VerifyPasswordRequest(userId, password))
        response.body() ?: false
    }

    override suspend fun deleteAccount(userId: String): Result<Unit> = runCatching {
        if (!ApiConfig.USE_MOCK_AUTH) api.deleteAccount(userId)
        sessionStore.clear()
    }
 
    override suspend fun loginGoogle(email: String, displayName: String): Result<UserSession> = runCatching {
        delay(600)
        val session = buildSession(email, displayName, UserRole.WORKER)
        sessionStore.save(session)
        session
    }

    private suspend fun realLogin(email: String, password: String): UserSession {
        val response = api.login(LoginRequest(email = email, password = password))
        return UserSession(token = response.token, user = response.usuario.toDomain())
    }

    private suspend fun realRegister(input: RegisterInput): UserSession {
        val tipo = when (input.role) {
            UserRole.EMPRESA -> "EMPRESA"
            else -> "TRABAJADOR"
        }
        val response = api.register(
            RegisterRequest(
                email = input.email,
                password = input.password,
                nombre = input.fullName,
                telefono = input.phone,
                tipo = tipo
            )
        )
        return UserSession(token = response.token, user = response.usuario.toDomain())
    }

    private suspend fun mockLogin(email: String): UserSession {
        delay(700)
        val role = if (email.contains("empresa", ignoreCase = true)) UserRole.EMPRESA else UserRole.WORKER
        val displayName = email.substringBefore('@').replaceFirstChar { it.uppercase() }
        return buildSession(email, displayName, role)
    }

    private suspend fun mockRegister(input: RegisterInput): UserSession {
        delay(900)
        return buildSession(input.email, input.fullName, input.role)
    }

    private fun buildSession(email: String, displayName: String, role: UserRole) = UserSession(
        token = "mock-${System.currentTimeMillis()}",
        user = User(
            id = "mock-${email.hashCode()}",
            email = email,
            role = role,
            fullName = displayName
        )
    )
}
