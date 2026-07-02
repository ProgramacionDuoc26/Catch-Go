package com.catchandgo.auth.controller;

import com.catchandgo.auth.dto.AuthResponseDto;
import com.catchandgo.auth.dto.LoginRequestDto;
import com.catchandgo.auth.dto.RegisterRequestDto;
import com.catchandgo.auth.dto.VerifyOtpRequestDto;
import com.catchandgo.auth.dto.ResendOtpRequestDto;
import com.catchandgo.auth.service.UserAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public AuthResponseDto login(@RequestBody LoginRequestDto dto) {
        return service.login(dto);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDto> verifyOtp(@RequestBody VerifyOtpRequestDto dto) {
        return ResponseEntity.ok(service.verifyOtp(dto.email(), dto.otp()));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Void> resendOtp(@RequestBody ResendOtpRequestDto dto) {
        service.resendOtp(dto.email());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{id}")
    public AuthResponseDto.UserDto findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@RequestBody java.util.Map<String, String> body) {
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
