package cl.catchgo.app.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface HealthApi {
    @GET("actuator/health")
    suspend fun health(): HealthResponse
}

@Serializable
data class HealthResponse(val status: String)
