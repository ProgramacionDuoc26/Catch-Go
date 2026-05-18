package cl.catchgo.app.domain.model

data class Habilidad(
    val id: Long,
    val nombre: String,
    val categoriaId: Long,
    val categoriaNombre: String,
    val predeterminada: Boolean = false
)

data class CategoriaHabilidades(
    val id: Long,
    val nombre: String,
    val habilidades: List<Habilidad>
)

data class HabilidadUsuario(
    val id: Long,
    val habilidadId: Long,
    val nombre: String,
    val categoriaId: Long,
    val categoriaNombre: String,
    val puntos: Int
)
