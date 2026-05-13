package cl.catchgo.app.data.remote.dto

import cl.catchgo.app.domain.model.CategoriaHabilidades
import cl.catchgo.app.domain.model.Habilidad
import cl.catchgo.app.domain.model.HabilidadUsuario
import cl.catchgo.app.domain.model.RadarData
import cl.catchgo.app.domain.model.EjeRadar
import kotlinx.serialization.Serializable

@Serializable
data class HabilidadItemDto(
    val id: Long,
    val nombre: String,
    val categoriaId: Long,
    val categoriaNombre: String,
    val predeterminada: Boolean = false
) {
    fun toDomain() = Habilidad(id, nombre, categoriaId, categoriaNombre, predeterminada)
}

@Serializable
data class CategoriaConHabilidadesDto(
    val id: Long,
    val nombre: String,
    val habilidades: List<HabilidadItemDto>
) {
    fun toDomain() = CategoriaHabilidades(id, nombre, habilidades.map { it.toDomain() })
}

@Serializable
data class HabilidadUsuarioDto(
    val id: Long,
    val habilidadId: Long,
    val nombre: String,
    val categoriaId: Long,
    val categoriaNombre: String,
    val puntos: Int,
    val trabajosAplicados: Int = 0
) {
    fun toDomain() = HabilidadUsuario(id, habilidadId, nombre, categoriaId, categoriaNombre, puntos)
}

@Serializable
data class EjeRadarDto(
    val categoriaId: Long,
    val nombre: String,
    val valor: Double
)

@Serializable
data class RadarDataDto(
    val ejes: List<EjeRadarDto>
) {
    fun toDomain() = RadarData(ejes.map { EjeRadar(it.categoriaId, it.nombre, it.valor) })
}

@Serializable
data class CrearHabilidadRequest(
    val nombre: String,
    val categoriaId: Long,
    val creadorId: Long
)

@Serializable
data class AsignarHabilidadRequest(
    val habilidadId: Long,
    val puntos: Int
)
