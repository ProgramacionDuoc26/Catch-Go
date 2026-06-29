package com.catchandgo.auth.controller;

import com.catchandgo.auth.dto.AuthResponseDto;
import com.catchandgo.auth.dto.LoginRequestDto;
import com.catchandgo.auth.dto.RegisterRequestDto;
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
