package cl.catchgo.app.data.remote

import cl.catchgo.app.data.remote.dto.LoginRequest
import cl.catchgo.app.data.remote.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
