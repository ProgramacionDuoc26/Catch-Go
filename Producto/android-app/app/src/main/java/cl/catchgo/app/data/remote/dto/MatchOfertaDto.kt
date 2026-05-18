package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MatchOfertaDto(
    val id: Long,
    val titulo: String? = null,
    val empresaId: Long? = null,
    val habilidadesRequeridas: List<String> = emptyList(),
    val experienciaRequeridaMeses: Int? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val disponibilidadRequerida: List<MatchVentanaDto> = emptyList()
)
