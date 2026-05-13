package cl.catchgo.app.domain.model

data class EjeRadar(
    val categoriaId: Long,
    val nombre: String,
    val valor: Double
)

data class RadarData(val ejes: List<EjeRadar>)
