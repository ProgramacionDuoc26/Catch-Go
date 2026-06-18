package com.catchandgo.profile.controller;

import com.catchandgo.profile.service.WebpayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WebpayController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@SuppressWarnings("null")
public class WebpayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebpayService webpayService;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> initPayload;
    private Map<String, Object> confirmPayload;

    @BeforeEach
    void setUp() {
        initPayload = new HashMap<>();
        initPayload.put("userId", "user-123");
        initPayload.put("amount", 10000);
        initPayload.put("returnUrl", "http://localhost/return");

        confirmPayload = new HashMap<>();
        confirmPayload.put("token", "real-token-123");
    }

    // CP-69: Validación de parámetros faltantes en /init
    // Verifica que al llamar a /profiles/webpay/init con parámetros incompletos (monto faltante), responda HTTP 400 Bad Request
    @Test
    void init_missingParameters_returnsBadRequest() throws Exception {
        initPayload.remove("amount");

        mockMvc.perform(post("/profiles/webpay/init")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("userId, amount, and returnUrl are required"));

        verifyNoInteractions(webpayService);
    }

    // CP-70: Inicialización exitosa de transacción
    // Verifica que al proveer parámetros válidos a /init, invoque al servicio de Webpay y responda HTTP 200 OK
    @Test
    void init_success_returnsOk() throws Exception {
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("token", "real-token-123");
        serviceResponse.put("url", "https://webpay3gint.transbank.cl/webpayserver/initTransaction");

        when(webpayService.initTransaction("user-123", 10000, "http://localhost/return")).thenReturn(serviceResponse);

        mockMvc.perform(post("/profiles/webpay/init")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("real-token-123"))
                .andExpect(jsonPath("$.url").value("https://webpay3gint.transbank.cl/webpayserver/initTransaction"));

        verify(webpayService, times(1)).initTransaction("user-123", 10000, "http://localhost/return");
    }

    // CP-71: Validación de token faltante en /confirm
    // Verifica que al llamar a /profiles/webpay/confirm sin el token de Transbank, responda HTTP 400 Bad Request
    @Test
    void confirm_missingToken_returnsBadRequest() throws Exception {
        confirmPayload.remove("token");

        mockMvc.perform(post("/profiles/webpay/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("token is required"));

        verifyNoInteractions(webpayService);
    }

    // CP-72: Confirmación exitosa de transacción
    // Verifica que al llamar a /confirm con un token válido, llame a confirmTransaction en el servicio y responda HTTP 200 OK
    @Test
    void confirm_success_returnsOk() throws Exception {
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("status", "AUTHORIZED");

        when(webpayService.confirmTransaction("real-token-123")).thenReturn(serviceResponse);

        mockMvc.perform(post("/profiles/webpay/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHORIZED"));

        verify(webpayService, times(1)).confirmTransaction("real-token-123");
    }
}
