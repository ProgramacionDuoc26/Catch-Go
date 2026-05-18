package cl.catchgo.app.data.repository

import cl.catchgo.app.data.remote.HabilidadesApi
import cl.catchgo.app.data.remote.dto.AsignarHabilidadRequest
import cl.catchgo.app.data.remote.dto.CrearHabilidadRequest
import cl.catchgo.app.domain.model.CategoriaHabilidades
import cl.catchgo.app.domain.model.Habilidad
import cl.catchgo.app.domain.model.HabilidadUsuario
import cl.catchgo.app.domain.model.RadarData
import cl.catchgo.app.domain.repository.HabilidadesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabilidadesRepositoryImpl @Inject constructor(
    private val api: HabilidadesApi
) : HabilidadesRepository {

    override suspend fun getCategorias(): Result<List<CategoriaHabilidades>> =
        runCatching { api.getCategorias().map { it.toDomain() } }

    override suspend fun crearHabilidad(nombre: String, categoriaId: Long, creadorId: Long): Result<Habilidad> =
        runCatching { api.crearHabilidad(CrearHabilidadRequest(nombre, categoriaId, creadorId)).toDomain() }

    override suspend fun getHabilidadesUsuario(usuarioId: Long): Result<List<HabilidadUsuario>> =
        runCatching { api.getHabilidadesUsuario(usuarioId).map { it.toDomain() } }

    override suspend fun asignarHabilidad(usuarioId: Long, habilidadId: Long, puntos: Int): Result<HabilidadUsuario> =
        runCatching { api.asignarHabilidad(usuarioId, AsignarHabilidadRequest(habilidadId, puntos)).toDomain() }

    override suspend fun getRadar(usuarioId: Long): Result<RadarData> =
        runCatching { api.getRadar(usuarioId).toDomain() }
}
