package cl.catchgo.app.data.remote

import cl.catchgo.app.data.remote.dto.ApplicationDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApplicationsApi {
    @GET("jobs/applications/user/{userId}")
    suspend fun listByUser(@Path("userId") userId: String): List<ApplicationDto>

    @POST("jobs/{jobId}/apply")
    suspend fun apply(
        @Path("jobId") jobId: Long,
        @Query("userId") userId: String
    ): ApplicationDto
}
