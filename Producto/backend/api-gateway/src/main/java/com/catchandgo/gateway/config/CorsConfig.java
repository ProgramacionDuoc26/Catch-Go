package com.catchandgo.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    /**
     * Comma-separated list of allowed origin patterns.
     * Supports wildcard subdomains (e.g. https://*.up.railway.app).
     * Override via the ALLOWED_ORIGIN_PATTERNS environment variable in production.
     */
    @Value("${ALLOWED_ORIGIN_PATTERNS:http://localhost:3000,https://localhost:3000,http://127.0.0.1:3000,https://*.up.railway.app}")
    private String allowedOriginPatterns;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allowed origins — supports wildcard patterns required for Railway preview URLs
        corsConfig.setAllowedOriginPatterns(
                Arrays.stream(allowedOriginPatterns.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
        );

        // Allowed HTTP methods
        corsConfig.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));

        // Allowed request headers
        corsConfig.setAllowedHeaders(List.of(
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.ACCEPT,
                HttpHeaders.ORIGIN,
                HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                "X-Requested-With"
        ));

        // Headers the browser is allowed to read from the response
        corsConfig.setExposedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE
        ));

        // Allow cookies / Authorization headers with credentials
        corsConfig.setAllowCredentials(true);

        // Cache preflight response for 1 hour (3600 seconds)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
