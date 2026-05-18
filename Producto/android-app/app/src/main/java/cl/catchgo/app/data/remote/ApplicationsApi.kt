package cl.catchgo.app.data.remote

import cl.catchgo.app.data.remote.dto.ApplicationDto
import cl.catchgo.app.data.remote.dto.UpdateStatusRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApplicationsApi {
    @GET("jobs/applications/user/{userId}")
    suspend fun listByUser(@Path("userId") userId: String): List<ApplicationDto>

    @POST("jobs/{jobId}/apply")
    suspend fun apply(
        @Path("jobId") jobId: Long,
        @Query("userId") userId: String
    ): retrofit2.Response<Unit>

    @DELETE("jobs/applications/{id}")
    suspend fun deleteApplication(@Path("id") id: Long): retrofit2.Response<Unit>

    @PUT("jobs/applications/{id}/status")
    suspend fun updateApplicationStatus(
        @Path("id") id: Long,
        @Body body: UpdateStatusRequest
    ): retrofit2.Response<Unit>
}
