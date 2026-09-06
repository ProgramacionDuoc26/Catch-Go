package com.catchandgo.auth.controller;

import com.catchandgo.auth.dto.*;
import com.catchandgo.auth.service.UserAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class UserAccountController {

    private final UserAccountService service;

    public UserAccountController(UserAccountService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody RegisterRequestDto dto) {
        return ResponseEntity.ok(service.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(service.login(dto));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody SendOtpRequestDto dto) {
        String code = service.sendOtp(dto);
        return ResponseEntity.ok(Map.of(
            "message", "Código OTP enviado exitosamente",
            "email", dto.email()
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Boolean>> verifyOtp(@RequestBody VerifyOtpRequestDto dto) {
        boolean valid = service.verifyOtp(dto);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequestDto dto) {
        String msg = service.forgotPassword(dto);
        return ResponseEntity.ok(Map.of("message", msg));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequestDto dto) {
        String msg = service.resetPassword(dto);
        return ResponseEntity.ok(Map.of("message", msg));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<AuthResponseDto.UserDto> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@RequestBody Map<String, String> body) {
        Long id = Long.valueOf(body.get("id"));
        String password = body.get("password");
        return ResponseEntity.ok(service.verifyPassword(id, password));
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
