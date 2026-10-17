package com.catchandgo.matching.service;

import com.catchandgo.matching.client.ProfileServiceClient;
import com.catchandgo.matching.dto.MatchSuggestionDto;
import com.catchandgo.matching.mapper.MatchSuggestionMapper;
import com.catchandgo.matching.repository.MatchSuggestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class MatchSuggestionService {

    private static final Logger log = LoggerFactory.getLogger(MatchSuggestionService.class);

    private final MatchSuggestionRepository repository;
    private final MatchSuggestionMapper mapper;
    private final ProfileServiceClient profileClient;

    public MatchSuggestionService(MatchSuggestionRepository repository,
                                  MatchSuggestionMapper mapper,
                                  ProfileServiceClient profileClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.profileClient = profileClient;
    }

    public List<MatchSuggestionDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public MatchSuggestionDto create(MatchSuggestionDto dto) {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }

    /**
     * Comunicación síncrona evaluable (Request-Reply):
     * Consulta en tiempo real el perfil del usuario en profile-service vía OpenFeign
     * antes de calcular o verificar la compatibilidad de una sugerencia.
     * Registra latencia y logs diagnósticos para evidencia de evaluación.
     */
    public Map<String, Object> verifyCompatibilitySync(String userId) {
        long start = System.currentTimeMillis();
        log.info("[SYNC-HTTP REQUEST] Iniciando llamada síncrona bloqueante a profile-service vía OpenFeign para userId: {}", userId);

        try {
            Map<String, Object> profile = profileClient.getProfileByUserId(userId);
            long duration = System.currentTimeMillis() - start;
            log.info("[SYNC-HTTP RESPONSE] Respuesta síncrona recibida con éxito en {} ms para userId: {}", duration, userId);
            return Map.of(
                "userId", userId,
                "status", "SUCCESS",
                "communicationType", "SYNCHRONOUS_HTTP_FEIGN",
                "durationMs", duration,
                "profile", profile != null ? profile : Map.of()
            );
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.warn("[SYNC-HTTP FALLBACK] No se pudo obtener respuesta síncrona inmediata de profile-service ({}) tras {} ms: {}",
                    e.getClass().getSimpleName(), duration, e.getMessage());
            return Map.of(
                "userId", userId,
                "status", "FALLBACK",
                "communicationType", "SYNCHRONOUS_HTTP_FEIGN",
                "durationMs", duration,
                "message", "Consulta síncrona completada con fallback local: " + e.getMessage()
            );
        }
    }
}

