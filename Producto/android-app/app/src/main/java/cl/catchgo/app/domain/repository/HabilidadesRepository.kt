package cl.catchgo.app.domain.repository

import cl.catchgo.app.domain.model.CategoriaHabilidades
import cl.catchgo.app.domain.model.Habilidad
import cl.catchgo.app.domain.model.HabilidadUsuario
import cl.catchgo.app.domain.model.RadarData

interface HabilidadesRepository {
    suspend fun getCategorias(): Result<List<CategoriaHabilidades>>
    suspend fun crearHabilidad(nombre: String, categoriaId: Long, creadorId: Long): Result<Habilidad>
    suspend fun getHabilidadesUsuario(usuarioId: Long): Result<List<HabilidadUsuario>>
    suspend fun asignarHabilidad(usuarioId: Long, habilidadId: Long, puntos: Int): Result<HabilidadUsuario>
    suspend fun getRadar(usuarioId: Long): Result<RadarData>
}
