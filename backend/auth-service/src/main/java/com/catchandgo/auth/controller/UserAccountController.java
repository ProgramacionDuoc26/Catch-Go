package com.catchandgo.auth.controller;

import com.catchandgo.auth.dto.UserAccountDto;
import com.catchandgo.auth.service.UserAccountService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/register")
public class UserAccountController {
    private final UserAccountService service;

    public UserAccountController(UserAccountService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserAccountDto> findAll() {
        return service.findAll();
    }

    @PostMapping
    public UserAccountDto create(@RequestBody UserAccountDto dto) {
        return service.create(dto);
    }
}
