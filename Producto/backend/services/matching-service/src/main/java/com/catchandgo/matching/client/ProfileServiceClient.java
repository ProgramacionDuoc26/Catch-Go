package com.catchandgo.matching.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Cliente HTTP Síncrono declarativo mediante Spring Cloud OpenFeign.
 * Demuestra la comunicación síncrona punto a punto (Request-Reply bloqueante) entre microservicios.
 */
@FeignClient(
    name = "profile-service",
    url = "${app.services.profile.url:${PROFILE_SERVICE_URL:http://localhost:8082}}"
)
public interface ProfileServiceClient {

    /**
     * Consulta síncrona del perfil de un usuario para verificación de habilidades y cálculo de afinidad.
     * Mapea directamente al endpoint de profile-service: GET /profiles/user/{userId}
     *
     * @param userId Identificador del usuario a consultar
     * @return Mapa con los datos del perfil retornado por profile-service
     */
    @GetMapping("/profiles/user/{userId}")
    Map<String, Object> getProfileByUserId(@PathVariable("userId") String userId);
}
