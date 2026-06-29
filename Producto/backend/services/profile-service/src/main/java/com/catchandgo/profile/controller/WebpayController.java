package com.catchandgo.profile.controller;

import com.catchandgo.profile.service.WebpayService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles/webpay")
public class WebpayController {
    private final WebpayService webpayService;

    public WebpayController(WebpayService webpayService) {
        this.webpayService = webpayService;
    }

    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> init(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        Number amountNum = (Number) payload.get("amount");
        Integer amount = amountNum != null ? amountNum.intValue() : null;
        String returnUrl = (String) payload.get("returnUrl");

        if (userId == null || amount == null || returnUrl == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId, amount, and returnUrl are required"));
        }

        Map<String, Object> response = webpayService.initTransaction(userId, amount, returnUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirm(@RequestBody Map<String, Object> payload) {
        String token = (String) payload.get("token");

        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "token is required"));
        }

        Map<String, Object> response = webpayService.confirmTransaction(token);
        return ResponseEntity.ok(response);
    }
}
