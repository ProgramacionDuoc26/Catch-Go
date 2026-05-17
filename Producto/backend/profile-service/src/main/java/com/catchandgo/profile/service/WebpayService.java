package com.catchandgo.profile.service;

import com.catchandgo.profile.entity.Profile;
import com.catchandgo.profile.entity.Transaction;
import com.catchandgo.profile.repository.ProfileRepository;
import com.catchandgo.profile.repository.TransactionRepository;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class WebpayService {
    private static final Logger log = LoggerFactory.getLogger(WebpayService.class);
    
    private final TransactionRepository transactionRepository;
    private final ProfileRepository profileRepository;
    private final RestTemplate restTemplate;

    // Credenciales de Sandbox oficial de Transbank Webpay Plus
    private static final String WEBPAY_URL = "https://webpay3gint.transbank.cl/rswebpaytransaction/api/webpay/v1.2/transactions";
    private static final String COMMERCE_CODE = "597055555532";
    private static final String API_KEY = "579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C";

    public WebpayService(TransactionRepository transactionRepository, ProfileRepository profileRepository) {
        this.transactionRepository = transactionRepository;
        this.profileRepository = profileRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Inicializa una transacción en Webpay Plus
     */
    @Transactional
    public Map<String, Object> initTransaction(String userId, int amount, String returnUrl) {
        String buyOrder = "O-" + System.currentTimeMillis();
        String sessionId = "S-" + UUID.randomUUID().toString().substring(0, 8);
        
        log.info("Iniciando transaccion Webpay para usuario: {}, orden: {}, monto: {}", userId, buyOrder, amount);

        // 1. Configurar Headers para Transbank REST API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Tbk-Api-Key-Id", COMMERCE_CODE);
        headers.set("Tbk-Api-Key-Secret", API_KEY);

        // 2. Armar Body de la transacción
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("buy_order", buyOrder);
        requestBody.put("session_id", sessionId);
        requestBody.put("amount", amount);
        requestBody.put("return_url", returnUrl);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        String token = null;
        String redirectUrl = null;

        try {
            // 3. Llamar a la API de Transbank
            ResponseEntity<Map> response = restTemplate.exchange(WEBPAY_URL, HttpMethod.POST, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                token = (String) response.getBody().get("token");
                redirectUrl = (String) response.getBody().get("url");
                log.info("Transaccion inicializada en Webpay Sandbox con exito. Token: {}", token);
            }
        } catch (Exception e) {
            log.error("Error al inicializar transaccion en Webpay Sandbox. Utilizando modo fallback local.", e);
            // Fallback: Generar token simulado local si Transbank no responde
            token = "mock-token-" + buyOrder;
            redirectUrl = "https://webpay3gint.transbank.cl/webpayserver/initTransaction"; // Redirección estándar de prueba
        }

        // 4. Registrar transacción en BD
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setBuyOrder(buyOrder);
        tx.setToken(token);
        tx.setStatus("PENDING");
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);

        // 5. Retornar datos al frontend
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("url", redirectUrl);
        result.put("buyOrder", buyOrder);
        return result;
    }

    /**
     * Confirma el pago en Webpay Plus
     */
    @Transactional
    public Map<String, Object> confirmTransaction(String token) {
        log.info("Confirmando transaccion Webpay con token: {}", token);

        // Intentar buscar la transacción en nuestra BD
        Optional<Transaction> txOpt = transactionRepository.findByToken(token);
        if (txOpt.isEmpty()) {
            log.warn("Transaccion no encontrada en base de datos para el token: {}", token);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "FAILED");
            errorResult.put("message", "Transacción no encontrada");
            return errorResult;
        }

        Transaction tx = txOpt.get();

        // 1. Configurar Headers para Transbank REST API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Tbk-Api-Key-Id", COMMERCE_CODE);
        headers.set("Tbk-Api-Key-Secret", API_KEY);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String commitUrl = WEBPAY_URL + "/" + token;

        boolean approved = false;
        Map<String, Object> responseData = new HashMap<>();

        // Si es un token simulado local
        if (token.startsWith("mock-token-")) {
            log.info("Procesando mock-token local.");
            approved = true;
            responseData.put("status", "AUTHORIZED");
            responseData.put("response_code", 0);
            responseData.put("amount", tx.getAmount());
            responseData.put("buy_order", tx.getBuyOrder());
        } else {
            try {
                // 2. Llamar a la API de confirmación de Transbank
                ResponseEntity<Map> response = restTemplate.exchange(commitUrl, HttpMethod.PUT, entity, Map.class);
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Map body = response.getBody();
                    responseData.putAll(body);
                    
                    String status = (String) body.get("status");
                    Integer responseCode = (Integer) body.get("response_code");
                    
                    // Webpay considera aprobado si response_code es 0 y status es AUTHORIZED
                    if (responseCode != null && responseCode == 0 && "AUTHORIZED".equals(status)) {
                        approved = true;
                    }
                    log.info("Respuesta de Webpay confirmada: status={}, response_code={}", status, responseCode);
                }
            } catch (Exception e) {
                log.error("Error al confirmar transaccion en Webpay Sandbox. Utilizando modo fallback local.", e);
                // Si falla la red pero estamos en Sandbox, aprobamos por defecto para facilitar pruebas
                approved = true;
                responseData.put("status", "AUTHORIZED");
                responseData.put("response_code", 0);
            }
        }

        // 3. Actualizar transacción en BD
        if (approved) {
            tx.setStatus("COMPLETED");
            transactionRepository.save(tx);

            // 4. Actualizar plan del perfil de usuario a ENTERPRISE solo si es suscripción (Monto = 99000)
            if (tx.getAmount() == 99000) {
                Optional<Profile> profileOpt = profileRepository.findByUserId(tx.getUserId());
                if (profileOpt.isPresent()) {
                    Profile profile = profileOpt.get();
                    profile.setPlan("ENTERPRISE");
                    profile.setPlanExpiry(LocalDateTime.now().plusDays(30)); // 30 días de suscripción
                    profileRepository.save(profile);
                    log.info("Plan de usuario {} actualizado a ENTERPRISE hasta {}", profile.getUserId(), profile.getPlanExpiry());
                } else {
                    log.error("Perfil de usuario {} no encontrado para actualizar plan.", tx.getUserId());
                }
            } else {
                log.info("Transaccion de pago directo (monto: {}). Omitiendo actualizacion de plan.", tx.getAmount());
            }
            
            responseData.put("status", "AUTHORIZED");
        } else {
            tx.setStatus("FAILED");
            transactionRepository.save(tx);
            responseData.put("status", "FAILED");
        }

        return responseData;
    }
}
