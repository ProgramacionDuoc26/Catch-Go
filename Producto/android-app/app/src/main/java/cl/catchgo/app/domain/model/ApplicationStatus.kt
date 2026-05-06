package cl.catchgo.app.domain.model

enum class ApplicationStatus(val display: String) {
    PENDING("Pendiente"),
    ACCEPTED("Aceptada"),
    REJECTED("Rechazada")
}
