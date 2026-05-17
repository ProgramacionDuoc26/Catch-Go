package cl.catchgo.app.data.remote

import cl.catchgo.app.data.remote.dto.LoginRequest
import cl.catchgo.app.data.remote.dto.LoginResponse
import cl.catchgo.app.data.remote.dto.RegisterRequest
import cl.catchgo.app.data.remote.dto.VerifyPasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @POST("auth/verify-password")
    suspend fun verifyPassword(@Body request: VerifyPasswordRequest): Response<Boolean>

    @DELETE("auth/users/{userId}")
    suspend fun deleteAccount(@Path("userId") userId: String): Response<Unit>
}
