package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MatchSugerenciaDto(
    val id: Long? = null,
    val ofertaTrabajoId: Long,
    val trabajadorPerfilId: Long,
    val nombreTrabajador: String? = null,
    val puntajeTotal: Double,
    val puntajeHabilidades: Double,
    val puntajeExperiencia: Double,
    val puntajeDistancia: Double,
    val puntajeDisponibilidad: Double,
    val fechaCreacion: String? = null
)
