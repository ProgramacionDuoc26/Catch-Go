package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MatchVentanaDto(
    val diaSemana: String,
    val horaInicio: String,
    val horaFin: String
)
