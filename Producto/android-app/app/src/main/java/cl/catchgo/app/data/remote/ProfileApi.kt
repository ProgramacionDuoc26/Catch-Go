package cl.catchgo.app.data.remote

import cl.catchgo.app.data.remote.dto.FileUploadResponse
import cl.catchgo.app.data.remote.dto.ProfileRemoteDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ProfileApi {
    @GET("profiles/user/{userId}")
    suspend fun getProfile(@Path("userId") userId: String): Response<ProfileRemoteDto>

    @POST("profiles")
    suspend fun saveProfile(@Body dto: ProfileRemoteDto): ProfileRemoteDto

    @PUT("profiles/{id}")
    suspend fun updateProfile(@Path("id") id: Long, @Body dto: ProfileRemoteDto): ProfileRemoteDto

    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("userId") userId: RequestBody
    ): FileUploadResponse

    @DELETE("profiles/user/{userId}")
    suspend fun deleteProfile(@Path("userId") userId: String): Response<Unit>
}
