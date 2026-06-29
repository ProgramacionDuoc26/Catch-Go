package com.catchandgo.profile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS desactivado en este servicio porque todo el tráfico en producción
 * pasa por el API Gateway, que maneja CORS de forma centralizada.
 * Tener CORS aquí Y en el gateway causaba cabeceras duplicadas
 * (Access-Control-Allow-Origin x2) que el navegador rechazaba.
 *
 * Para desarrollo local directo (sin gateway), ejecutar con
 * spring.profiles.active=local y descomentar el filtro.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // No establecer CORS propios: el Gateway los agrega.
        // Permitimos todo pero SIN enviar cabeceras CORS propias
        // para evitar duplicación con el Gateway.
        config.applyPermitDefaultValues();
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
