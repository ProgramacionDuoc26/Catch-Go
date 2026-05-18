package cl.catchgo.app.domain.repository

import cl.catchgo.app.data.remote.dto.ProfileRemoteDto

interface ProfileRepository {
    suspend fun getProfile(userId: String): Result<ProfileRemoteDto?>
    suspend fun saveProfile(dto: ProfileRemoteDto): Result<ProfileRemoteDto>
    suspend fun uploadFile(userId: String, bytes: ByteArray, fileName: String, mimeType: String): Result<String>
    suspend fun deleteProfile(userId: String): Result<Unit>
}
