package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MatchTrabajadorDto(
    val id: Long,
    val nombre: String? = null,
    val habilidades: List<String> = emptyList(),
    val experienciaMeses: Int? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val tiempoViajeMinutos: Int? = null,
    val disponibilidad: List<MatchVentanaDto> = emptyList()
)
