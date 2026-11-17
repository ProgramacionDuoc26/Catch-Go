package com.catchandgo.gateway.security;

import com.catchandgo.common.jwt.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {
    @Bean
    JwtService gatewayJwtService(@Value("${JWT_SECRET}") String secret) {
        return new JwtService(secret);
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, JwtService jwtService) {
        ReactiveAuthenticationManager manager = authentication -> Mono.fromCallable(() -> {
            try {
                var claims = jwtService.parseClaims(authentication.getCredentials().toString());
                if (claims.getSubject() == null || claims.getSubject().isBlank() || claims.getExpiration() == null) {
                    throw new BadCredentialsException("JWT incompleto");
                }
                String role = claims.get("role", String.class);
                var authorities = "ADMIN".equals(role)
                        ? java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
                        : java.util.List.<org.springframework.security.core.authority.SimpleGrantedAuthority>of();
                return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
            } catch (Exception ex) {
                throw new BadCredentialsException("JWT invalido o expirado", ex);
            }
        });
        var unauthorized = new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED);
        var jwtFilter = new AuthenticationWebFilter(manager);
        jwtFilter.setServerAuthenticationConverter(exchange -> {
            String path = exchange.getRequest().getPath().value();
            if (exchange.getRequest().getMethod() == HttpMethod.POST && java.util.Set.of(
                    "/auth/login", "/auth/register", "/auth/send-otp", "/auth/verify-otp",
                    "/auth/forgot-password", "/auth/reset-password").contains(path)) return Mono.empty();
            String header = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) return Mono.empty();
            return Mono.just(UsernamePasswordAuthenticationToken.unauthenticated("jwt", header.substring(7).trim()));
        });
        jwtFilter.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(unauthorized));
        jwtFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http.csrf(csrf -> csrf.disable())
                .cors(withDefaults())
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(errors -> errors.authenticationEntryPoint(unauthorized))
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/auth/login", "/auth/register", "/auth/send-otp",
                                "/auth/verify-otp", "/auth/forgot-password", "/auth/reset-password").permitAll()
                        .pathMatchers("/actuator/health", "/actuator/info", "/gateway/ping",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/ws-notifications", "/ws-notifications/**").permitAll()
                        .pathMatchers("/queues", "/queues/**", "/api/v1/queues", "/api/v1/queues/**").hasRole("ADMIN")
                        .anyExchange().authenticated())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .build();
    }
}
