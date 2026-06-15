package com.catchandgo.profile.service;

import com.catchandgo.profile.entity.Profile;
import com.catchandgo.profile.entity.Transaction;
import com.catchandgo.profile.repository.ProfileRepository;
import com.catchandgo.profile.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unchecked", "rawtypes"})
public class WebpayServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WebpayService service;

    private Profile profile;
    private Transaction transaction;

    @BeforeEach
    void setUp() throws Exception {
        // Usar reflexión para inyectar el mock de RestTemplate en la clase WebpayService
        Field field = WebpayService.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(service, restTemplate);

        profile = new Profile();
        profile.setId(10L);
        profile.setUserId("user-123");
        profile.setPlan("FREE");

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setUserId("user-123");
        transaction.setAmount(10000);
        transaction.setBuyOrder("O-12345");
        transaction.setToken("real-token-123");
        transaction.setStatus("PENDING");
        transaction.setCreatedAt(LocalDateTime.now());
    }

    // Inicializar transacción en Webpay Plus exitosamente
    // Verifica que se llame a la API de Transbank, se guarde la transacción con estado PENDING y retorne los datos correspondientes
    @Test
    void initTransaction_success() {
        Map<String, Object> mockResponseBody = new HashMap<>();
        mockResponseBody.put("token", "real-token-123");
        mockResponseBody.put("url", "https://webpay3gint.transbank.cl/webpayserver/initTransaction");

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = service.initTransaction("user-123", 10000, "http://localhost/return");

        assertNotNull(result);
        assertEquals("real-token-123", result.get("token"));
        assertEquals("https://webpay3gint.transbank.cl/webpayserver/initTransaction", result.get("url"));
        assertNotNull(result.get("buyOrder"));

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction savedTx = txCaptor.getValue();
        assertEquals("user-123", savedTx.getUserId());
        assertEquals(10000, savedTx.getAmount());
        assertEquals("real-token-123", savedTx.getToken());
        assertEquals("PENDING", savedTx.getStatus());
    }

    // Inicializar transacción con fallo en la llamada API
    // Verifica que al fallar la API de Transbank, el sistema use el modo fallback local generando un token simulado
    @Test
    void initTransaction_fallback() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("Connection timed out"));

        Map<String, Object> result = service.initTransaction("user-123", 10000, "http://localhost/return");

        assertNotNull(result);
        assertTrue(result.get("token").toString().startsWith("mock-token-"));
        assertEquals("https://webpay3gint.transbank.cl/webpayserver/initTransaction", result.get("url"));

        verify(transactionRepository).save(any(Transaction.class));
    }

    // Confirmar transacción inexistente
    // Verifica que al no encontrar la transacción en base de datos, retorne estado FAILED directamente
    @Test
    void confirmTransaction_notFound() {
        when(transactionRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        Map<String, Object> result = service.confirmTransaction("invalid-token");

        assertNotNull(result);
        assertEquals("FAILED", result.get("status"));
        assertEquals("Transacción no encontrada", result.get("message"));
    }

    // Confirmar transacción con un token simulado local (mock-token)
    // Verifica que se procese como aprobado directamente sin realizar llamadas a la API de Transbank
    @Test
    void confirmTransaction_mockToken_approved() {
        transaction.setToken("mock-token-O-12345");
        when(transactionRepository.findByToken("mock-token-O-12345")).thenReturn(Optional.of(transaction));

        Map<String, Object> result = service.confirmTransaction("mock-token-O-12345");

        assertNotNull(result);
        assertEquals("AUTHORIZED", result.get("status"));
        assertEquals("COMPLETED", transaction.getStatus());
        verify(transactionRepository).save(transaction);
        verifyNoInteractions(restTemplate);
    }

    // Confirmar transacción con token real aprobada por Transbank
    // Verifica que al retornar AUTHORIZED y response_code = 0 se marque la transacción como COMPLETED
    @Test
    void confirmTransaction_realToken_approved() {
        when(transactionRepository.findByToken("real-token-123")).thenReturn(Optional.of(transaction));

        Map<String, Object> mockResponseBody = new HashMap<>();
        mockResponseBody.put("status", "AUTHORIZED");
        mockResponseBody.put("response_code", 0);

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = service.confirmTransaction("real-token-123");

        assertNotNull(result);
        assertEquals("AUTHORIZED", result.get("status"));
        assertEquals("COMPLETED", transaction.getStatus());
        verify(transactionRepository).save(transaction);
    }

    // Confirmar transacción real aprobada por suscripción empresarial (Monto = 99000)
    // Verifica que además de marcar la transacción como COMPLETED, se actualice el plan de perfil de usuario a ENTERPRISE por 30 días
    @Test
    void confirmTransaction_enterpriseSubscription_success() {
        transaction.setAmount(99000);
        when(transactionRepository.findByToken("real-token-123")).thenReturn(Optional.of(transaction));
        when(profileRepository.findByUserId("user-123")).thenReturn(Optional.of(profile));

        Map<String, Object> mockResponseBody = new HashMap<>();
        mockResponseBody.put("status", "AUTHORIZED");
        mockResponseBody.put("response_code", 0);

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = service.confirmTransaction("real-token-123");

        assertNotNull(result);
        assertEquals("AUTHORIZED", result.get("status"));
        assertEquals("COMPLETED", transaction.getStatus());
        assertEquals("ENTERPRISE", profile.getPlan());
        assertNotNull(profile.getPlanExpiry());
        verify(transactionRepository).save(transaction);
        verify(profileRepository).save(profile);
    }

    // Confirmar transacción real rechazada por Transbank
    // Verifica que al retornar un código distinto de 0 o un estado no autorizado, se actualice la transacción a FAILED
    @Test
    void confirmTransaction_realToken_rejected() {
        when(transactionRepository.findByToken("real-token-123")).thenReturn(Optional.of(transaction));

        Map<String, Object> mockResponseBody = new HashMap<>();
        mockResponseBody.put("status", "FAILED");
        mockResponseBody.put("response_code", -1);

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = service.confirmTransaction("real-token-123");

        assertNotNull(result);
        assertEquals("FAILED", result.get("status"));
        assertEquals("FAILED", transaction.getStatus());
        verify(transactionRepository).save(transaction);
    }

    // Confirmar transacción real con error de red en Sandbox
    // Verifica que ante una excepción en la comunicación con Transbank, el sistema aplique el fallback aprobando el pago para Sandbox
    @Test
    void confirmTransaction_realToken_exception_fallback() {
        when(transactionRepository.findByToken("real-token-123")).thenReturn(Optional.of(transaction));

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("Read timeout"));

        Map<String, Object> result = service.confirmTransaction("real-token-123");

        assertNotNull(result);
        assertEquals("AUTHORIZED", result.get("status"));
        assertEquals("COMPLETED", transaction.getStatus());
        verify(transactionRepository).save(transaction);
    }
}
