package cl.catchgo.app.data.repository

import cl.catchgo.app.data.remote.ApiConfig
import cl.catchgo.app.data.remote.ProfileApi
import cl.catchgo.app.data.remote.dto.ProfileRemoteDto
import cl.catchgo.app.domain.repository.ProfileRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi
) : ProfileRepository {

    override suspend fun getProfile(userId: String): Result<ProfileRemoteDto?> = runCatching {
        val response = api.getProfile(userId)
        if (response.code() == 204) null else response.body()
    }

    override suspend fun saveProfile(dto: ProfileRemoteDto): Result<ProfileRemoteDto> = runCatching {
        api.saveProfile(dto)
    }

    override suspend fun uploadFile(
        userId: String,
        bytes: ByteArray,
        fileName: String,
        mimeType: String
    ): Result<String> = runCatching {
        val body = bytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("file", fileName, body)
        val userIdPart = userId.toRequestBody("text/plain".toMediaType())
        val response = api.uploadFile(part, userIdPart)
        ApiConfig.PROFILE_URL.trimEnd('/') + response.url
    }
}
