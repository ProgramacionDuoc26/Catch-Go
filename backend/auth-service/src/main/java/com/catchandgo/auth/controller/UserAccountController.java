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
    public AuthResponseDto.UserDto findById(@PathVariable Long id) {
        return service.findById(id);
    }
}
