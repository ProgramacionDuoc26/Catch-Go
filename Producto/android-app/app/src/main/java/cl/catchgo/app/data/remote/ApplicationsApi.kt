package cl.catchgo.app.data.remote

import cl.catchgo.app.data.remote.dto.ApplicationDto
import cl.catchgo.app.data.remote.dto.CreateApplicationRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApplicationsApi {
    @GET("postulaciones/me")
    suspend fun listMine(): List<ApplicationDto>

    @POST("postulaciones")
    suspend fun create(@Body request: CreateApplicationRequest): ApplicationDto
}
