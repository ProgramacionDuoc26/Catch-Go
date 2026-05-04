package cl.catchgo.app.core.error

sealed class ApiError(open val message: String) {
    data object NoConnection : ApiError("Sin conexión a internet")
    data object Timeout : ApiError("La solicitud demoró demasiado")
    data object Unauthorized : ApiError("Sesión expirada, vuelve a iniciar sesión")
    data object Forbidden : ApiError("No tienes permiso para esta acción")
    data object NotFound : ApiError("Recurso no encontrado")
    data class Conflict(override val message: String) : ApiError(message)
    data class Validation(override val message: String) : ApiError(message)
    data class Server(override val message: String) : ApiError(message)
    data class Unknown(override val message: String) : ApiError(message)
}
