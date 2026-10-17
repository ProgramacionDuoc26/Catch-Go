package com.catchandgo.matching.service;

import com.catchandgo.matching.client.ProfileServiceClient;
import com.catchandgo.matching.dto.MatchSuggestionDto;
import com.catchandgo.matching.entity.MatchSuggestion;
import com.catchandgo.matching.mapper.MatchSuggestionMapper;
import com.catchandgo.matching.repository.MatchSuggestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class MatchSuggestionServiceTest {

    @Mock
    private MatchSuggestionRepository repository;

    @Mock
    private MatchSuggestionMapper mapper;

    @Mock
    private ProfileServiceClient profileClient;

    @InjectMocks
    private MatchSuggestionService service;

    private MatchSuggestion entity;
    private MatchSuggestionDto dto;

    @BeforeEach
    void setUp() {
        entity = new MatchSuggestion();
        entity.setId(1L);
        entity.setName("Sugerencia de emparejamiento 1");

        dto = new MatchSuggestionDto(1L, "Sugerencia de emparejamiento 1");
    }

    // Buscar todas las sugerencias de emparejamiento exitosamente
    // Verifica que retorne la lista de sugerencias mapeadas a DTO
    @Test
    void findAll_success() {
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        List<MatchSuggestionDto> result = service.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
        verify(repository).findAll();
    }

    // Buscar todas las sugerencias cuando la base de datos está vacía
    // Verifica que retorne una lista vacía
    @Test
    void findAll_empty() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<MatchSuggestionDto> result = service.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }

    // Crear una sugerencia de emparejamiento exitosamente
    // Verifica que guarde la entidad en base de datos y retorne el DTO
    @Test
    void create_success() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        MatchSuggestionDto result = service.create(dto);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(repository).save(entity);
    }

    // Test de comunicación síncrona: Respuesta exitosa desde profile-service
    @Test
    void verifyCompatibilitySync_success() {
        String userId = "usr-123";
        java.util.Map<String, Object> mockProfile = java.util.Map.of(
            "id", "prof-1",
            "userId", userId,
            "title", "Desarrollador Backend"
        );
        when(profileClient.getProfileByUserId(userId)).thenReturn(mockProfile);

        java.util.Map<String, Object> result = service.verifyCompatibilitySync(userId);

        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertEquals("SYNCHRONOUS_HTTP_FEIGN", result.get("communicationType"));
        assertEquals(userId, result.get("userId"));
        assertEquals(mockProfile, result.get("profile"));
        assertTrue(result.containsKey("durationMs"));
        verify(profileClient).getProfileByUserId(userId);
    }

    // Test de comunicación síncrona: Fallback controlado cuando profile-service no responde o falla
    @Test
    void verifyCompatibilitySync_fallbackOnException() {
        String userId = "usr-error";
        when(profileClient.getProfileByUserId(userId)).thenThrow(new RuntimeException("Connection timed out"));

        java.util.Map<String, Object> result = service.verifyCompatibilitySync(userId);

        assertNotNull(result);
        assertEquals("FALLBACK", result.get("status"));
        assertEquals("SYNCHRONOUS_HTTP_FEIGN", result.get("communicationType"));
        assertEquals(userId, result.get("userId"));
        assertTrue(result.get("message").toString().contains("Connection timed out"));
        verify(profileClient).getProfileByUserId(userId);
    }
}
