package cl.catchgo.app.data.remote

import cl.catchgo.app.data.remote.dto.JobOfferDto
import retrofit2.http.GET
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
}
