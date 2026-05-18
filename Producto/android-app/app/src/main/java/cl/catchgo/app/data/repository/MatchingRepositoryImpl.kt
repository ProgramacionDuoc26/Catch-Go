package cl.catchgo.app.data.repository

import cl.catchgo.app.data.remote.MatchingApi
import cl.catchgo.app.data.remote.dto.MatchSolicitudDto
import cl.catchgo.app.data.remote.dto.MatchSugerenciaDto
import cl.catchgo.app.domain.repository.MatchingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchingRepositoryImpl @Inject constructor(
    private val api: MatchingApi
) : MatchingRepository {

    override suspend fun ejecutarMatching(solicitud: MatchSolicitudDto): Result<List<MatchSugerenciaDto>> =
        runCatching { api.ejecutarMatching(solicitud) }

    override suspend fun getSugerencias(ofertaId: Long): Result<List<MatchSugerenciaDto>> =
        runCatching { api.getSugerencias(ofertaId) }
}
