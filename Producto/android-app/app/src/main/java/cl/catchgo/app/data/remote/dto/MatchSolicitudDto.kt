package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MatchSolicitudDto(
    val ofertaTrabajo: MatchOfertaDto,
    val trabajadores: List<MatchTrabajadorDto>
)
