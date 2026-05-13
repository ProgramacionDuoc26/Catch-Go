package cl.catchgo.app.data.remote

import cl.catchgo.app.data.remote.dto.AsignarHabilidadRequest
import cl.catchgo.app.data.remote.dto.CategoriaConHabilidadesDto
import cl.catchgo.app.data.remote.dto.CrearHabilidadRequest
import cl.catchgo.app.data.remote.dto.HabilidadItemDto
import cl.catchgo.app.data.remote.dto.HabilidadUsuarioDto
import cl.catchgo.app.data.remote.dto.RadarDataDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface HabilidadesApi {

    @GET("auth/habilidades/categorias")
    suspend fun getCategorias(): List<CategoriaConHabilidadesDto>

    @POST("auth/habilidades")
    suspend fun crearHabilidad(@Body request: CrearHabilidadRequest): HabilidadItemDto

    @GET("auth/user/{id}/habilidades")
    suspend fun getHabilidadesUsuario(@Path("id") id: Long): List<HabilidadUsuarioDto>

    @POST("auth/user/{id}/habilidades")
    suspend fun asignarHabilidad(
        @Path("id") id: Long,
        @Body request: AsignarHabilidadRequest
    ): HabilidadUsuarioDto

    @GET("auth/user/{id}/radar")
    suspend fun getRadar(@Path("id") id: Long): RadarDataDto
}
