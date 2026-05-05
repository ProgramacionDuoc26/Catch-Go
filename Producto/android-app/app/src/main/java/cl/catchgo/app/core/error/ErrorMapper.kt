package cl.catchgo.app.core.error

import cl.catchgo.app.data.remote.dto.ErrorResponse
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun map(throwable: Throwable): ApiError = when (throwable) {
        is HttpException -> mapHttp(throwable)
        is SocketTimeoutException -> ApiError.Timeout
        is UnknownHostException -> ApiError.NoConnection
        is IOException -> ApiError.NoConnection
        else -> ApiError.Unknown(throwable.message ?: "Error desconocido")
    }

    private fun mapHttp(exception: HttpException): ApiError {
        val parsed = parseBody(exception)
        val msg = parsed?.message ?: parsed?.error ?: exception.message()
        return when (exception.code()) {
            400 -> ApiError.Validation(msg.ifBlank { "Solicitud inválida" })
            401 -> ApiError.Unauthorized
            403 -> ApiError.Forbidden
            404 -> ApiError.NotFound
            409 -> ApiError.Conflict(msg.ifBlank { "Conflicto" })
            in 500..599 -> ApiError.Server(msg.ifBlank { "Error en el servidor" })
            else -> ApiError.Unknown(msg.ifBlank { "Error HTTP ${exception.code()}" })
        }
    }

    private fun parseBody(exception: HttpException): ErrorResponse? = runCatching {
        exception.response()?.errorBody()?.string()?.let { json.decodeFromString<ErrorResponse>(it) }
    }.getOrNull()
}
