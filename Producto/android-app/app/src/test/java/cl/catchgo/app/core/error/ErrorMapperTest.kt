package cl.catchgo.app.core.error

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMapperTest {

    /**
     * CP-42: Verificar el correcto mapeo de una SocketTimeoutException a un ApiError.Timeout.
     */
    @Test
    fun map_withSocketTimeoutException_returnsApiErrorTimeout() {
        val exception = SocketTimeoutException("La conexión expiró")
        val apiError = ErrorMapper.map(exception)

        assertTrue(apiError is ApiError.Timeout)
        assertEquals("La solicitud demoró demasiado", apiError.message)
    }

    /**
     * CP-43: Verificar el correcto mapeo de una IOException a un ApiError.NoConnection.
     */
    @Test
    fun map_withIOException_returnsApiErrorNoConnection() {
        val exception = IOException("Fallo de red")
        val apiError = ErrorMapper.map(exception)

        assertTrue(apiError is ApiError.NoConnection)
        assertEquals("Sin conexión a internet", apiError.message)
    }

    /**
     * CP-43 (Alterno): Verificar el correcto mapeo de una UnknownHostException a un ApiError.NoConnection.
     */
    @Test
    fun map_withUnknownHostException_returnsApiErrorNoConnection() {
        val exception = UnknownHostException("No se resolvió el DNS")
        val apiError = ErrorMapper.map(exception)

        assertTrue(apiError is ApiError.NoConnection)
        assertEquals("Sin conexión a internet", apiError.message)
    }

    /**
     * CP-44: Verificar que una excepción genérica no controlada sea mapeada a ApiError.Unknown conservando su mensaje original.
     */
    @Test
    fun map_withGenericThrowable_returnsApiErrorUnknown() {
        val exception = RuntimeException("Error fatal en tiempo de ejecución")
        val apiError = ErrorMapper.map(exception)

        assertTrue(apiError is ApiError.Unknown)
        assertEquals("Error fatal en tiempo de ejecución", apiError.message)
    }

    /**
     * CP-45: Verificar que un código de error HTTP 401 (No Autorizado) sea mapeado a ApiError.Unauthorized.
     */
    @Test
    fun map_withHttpException401_returnsApiErrorUnauthorized() {
        val httpException: HttpException = mockk()
        every { httpException.code() } returns 401
        every { httpException.message() } returns "Unauthorized"
        every { httpException.response() } returns null

        val apiError = ErrorMapper.map(httpException)

        assertTrue(apiError is ApiError.Unauthorized)
        assertEquals("Sesión expirada, vuelve a iniciar sesión", apiError.message)
    }

    /**
     * CP-46: Verificar que un código de error HTTP 404 (No Encontrado) sea mapeado a ApiError.NotFound.
     */
    @Test
    fun map_withHttpException404_returnsApiErrorNotFound() {
        val httpException: HttpException = mockk()
        every { httpException.code() } returns 404
        every { httpException.message() } returns "Not Found"
        every { httpException.response() } returns null

        val apiError = ErrorMapper.map(httpException)

        assertTrue(apiError is ApiError.NotFound)
        assertEquals("Recurso no encontrado", apiError.message)
    }
}
