package com.catchandgo.auth.supabase;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/supabase")
public class SupabaseExchangeController {
    public record Request(String accessToken, String tipo) {}
    private final SupabaseExchangeService service;
    public SupabaseExchangeController(SupabaseExchangeService service) { this.service = service; }
    @PostMapping("/exchange")
    public SupabaseExchangeService.Result exchange(@RequestBody Request request) {
        return service.exchange(request.accessToken(), request.tipo());
    }
    @ExceptionHandler(SupabaseAuthException.class)
    public ResponseEntity<Map<String, String>> handleSupabaseError(SupabaseAuthException ex) {
        return ResponseEntity.status(ex.isUnavailable() ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", ex.getMessage()));
    }
}
