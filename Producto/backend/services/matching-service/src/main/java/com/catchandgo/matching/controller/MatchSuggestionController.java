package com.catchandgo.matching.controller;

import com.catchandgo.matching.dto.MatchSuggestionDto;
import com.catchandgo.matching.service.MatchSuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/matching")
public class MatchSuggestionController {

    private final MatchSuggestionService service;

    public MatchSuggestionController(MatchSuggestionService service) {
        this.service = service;
    }

    @GetMapping
    public List<MatchSuggestionDto> findAll() {
        return service.findAll();
    }

    @PostMapping
    public MatchSuggestionDto create(@RequestBody MatchSuggestionDto dto) {
        return service.create(dto);
    }

    /**
     * Endpoint de demostración de comunicación síncrona (Request-Reply):
     * Realiza una consulta síncrona vía OpenFeign hacia profile-service
     * para verificar en tiempo real las habilidades y datos del usuario.
     */
    @GetMapping("/verify/{userId}")
    public ResponseEntity<Map<String, Object>> verifyCompatibility(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(service.verifyCompatibilitySync(userId));
    }
}

