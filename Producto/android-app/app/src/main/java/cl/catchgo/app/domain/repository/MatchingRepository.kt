package cl.catchgo.app.domain.repository

import cl.catchgo.app.data.remote.dto.MatchSolicitudDto
import cl.catchgo.app.data.remote.dto.MatchSugerenciaDto

interface MatchingRepository {
    suspend fun ejecutarMatching(solicitud: MatchSolicitudDto): Result<List<MatchSugerenciaDto>>
    suspend fun getSugerencias(ofertaId: Long): Result<List<MatchSugerenciaDto>>
}
