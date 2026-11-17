package com.catchandgo.jobs.security;

import com.catchandgo.common.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Valida el JWT tambien cuando la administracion recibe peticiones directas. */
public class QueueAdminJwtFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    public QueueAdminJwtFilter(JwtService jwt) { this.jwt = jwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            try {
                var claims = jwt.parseClaims(header.substring(7).trim());
                if (claims.getSubject() != null && !claims.getSubject().isBlank() && claims.getExpiration() != null) {
                    String role = claims.get("role", String.class);
                    var authorities = "ADMIN".equals(role) ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                            : List.<SimpleGrantedAuthority>of();
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities));
                }
            } catch (RuntimeException ex) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
