package cl.catchgo.app.data.remote

import cl.catchgo.app.data.remote.dto.CreateJobOfferRequest
import cl.catchgo.app.data.remote.dto.EmployerApplicationDto
import cl.catchgo.app.data.remote.dto.JobOfferDto
import cl.catchgo.app.data.remote.dto.UpdateStatusRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface JobsApi {
    @GET("jobs")
    suspend fun list(
        @Query("category") category: String? = null,
        @Query("comuna") comuna: String? = null,
        @Query("radius") radius: Int? = null
    ): List<JobOfferDto>

    @GET("jobs/{id}")
    suspend fun get(@Path("id") id: String): JobOfferDto

    @POST("jobs")
    suspend fun create(@Body dto: CreateJobOfferRequest): JobOfferDto

    @PUT("jobs/{id}")
    suspend fun update(@Path("id") id: Long, @Body dto: CreateJobOfferRequest): JobOfferDto

    @DELETE("jobs/{id}")
    suspend fun delete(@Path("id") id: Long): retrofit2.Response<Unit>

    @GET("jobs/applications/employer/{employerId}")
    suspend fun listByEmployer(@Path("employerId") employerId: String): List<EmployerApplicationDto>

    @PUT("jobs/applications/{id}/status")
    suspend fun updateApplicationStatus(
        @Path("id") id: Long,
        @Body body: UpdateStatusRequest
    ): retrofit2.Response<Unit>
}
