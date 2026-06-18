package com.catchandgo.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("Catch&Go Gateway API").version("v1"))
                .servers(List.of(
                        new Server().url("https://api-gateway2-catch-go.up.railway.app").description("API Gateway (Producción)"),
                        new Server().url("http://localhost:8080").description("Local")
                ));
    }
}
