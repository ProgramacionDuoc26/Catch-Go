package cl.catchgo.app.data.repository

import cl.catchgo.app.data.local.SessionStore
import cl.catchgo.app.data.remote.ApiConfig
import cl.catchgo.app.data.remote.AuthApi
import cl.catchgo.app.data.remote.dto.LoginRequest
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

    override suspend fun logout() {
        sessionStore.clear()
    }

    private suspend fun realLogin(email: String, password: String): UserSession {
        val response = api.login(LoginRequest(email = email, password = password))
        return UserSession(token = response.token, user = response.user.toDomain())
    }

    private suspend fun mockLogin(email: String): UserSession {
        delay(700)
        val role = if (email.contains("empresa", ignoreCase = true)) UserRole.EMPRESA else UserRole.WORKER
        val displayName = email.substringBefore('@').replaceFirstChar { it.uppercase() }
        return UserSession(
            token = "mock-${System.currentTimeMillis()}",
            user = User(
                id = "mock-${email.hashCode()}",
                email = email,
                role = role,
                fullName = displayName
            )
        )
    }
}
