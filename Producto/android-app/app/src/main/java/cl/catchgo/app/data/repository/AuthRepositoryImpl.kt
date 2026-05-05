package cl.catchgo.app.data.repository

import cl.catchgo.app.data.local.SessionStore
import cl.catchgo.app.data.remote.AuthApi
import cl.catchgo.app.data.remote.dto.LoginRequest
import cl.catchgo.app.domain.model.UserSession
import cl.catchgo.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionStore: SessionStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<UserSession> = runCatching {
        val response = api.login(LoginRequest(email = email, password = password))
        val session = UserSession(token = response.token, user = response.user.toDomain())
        sessionStore.save(session)
        session
    }

    override suspend fun logout() {
        sessionStore.clear()
    }
}
