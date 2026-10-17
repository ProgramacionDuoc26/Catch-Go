package com.catchandgo.matching.controller;

import com.catchandgo.matching.dto.MatchSuggestionDto;
import com.catchandgo.matching.service.MatchSuggestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchSuggestionControllerTest {

    @Mock
    private MatchSuggestionService service;

    @InjectMocks
    private MatchSuggestionController controller;

    @Test
    void findAll_returnsList() {
        when(service.findAll()).thenReturn(List.of(new MatchSuggestionDto(1L, "Match 1")));
        List<MatchSuggestionDto> result = controller.findAll();
        assertEquals(1, result.size());
        verify(service).findAll();
    }

    @Test
    void create_returnsDto() {
        MatchSuggestionDto dto = new MatchSuggestionDto(1L, "Match 1");
        when(service.create(dto)).thenReturn(dto);
        MatchSuggestionDto result = controller.create(dto);
        assertEquals(dto, result);
        verify(service).create(dto);
    }

    @Test
    void verifyCompatibility_callsServiceAndReturnsOk() {
        String userId = "usr-100";
        Map<String, Object> serviceResponse = Map.of(
            "userId", userId,
            "status", "SUCCESS",
            "communicationType", "SYNCHRONOUS_HTTP_FEIGN",
            "durationMs", 45L
        );
        when(service.verifyCompatibilitySync(userId)).thenReturn(serviceResponse);

        ResponseEntity<Map<String, Object>> response = controller.verifyCompatibility(userId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(serviceResponse, response.getBody());
        verify(service).verifyCompatibilitySync(userId);
    }
}
