package cl.catchgo.app.data.remote

import cl.catchgo.app.data.remote.dto.MatchSolicitudDto
import cl.catchgo.app.data.remote.dto.MatchSugerenciaDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MatchingApi {

    @POST("matching/ejecutar")
    suspend fun ejecutarMatching(@Body solicitud: MatchSolicitudDto): List<MatchSugerenciaDto>

    @GET("matching/job-offers/{ofertaId}/sugerencias")
    suspend fun getSugerencias(@Path("ofertaId") ofertaId: Long): List<MatchSugerenciaDto>
}
