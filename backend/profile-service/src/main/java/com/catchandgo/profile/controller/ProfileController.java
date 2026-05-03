package com.catchandgo.profile.controller;

import com.catchandgo.profile.dto.ProfileDto;
import com.catchandgo.profile.service.ProfileService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles")
public class ProfileController {
    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProfileDto> findAll() {
        return service.findAll();
    }

    @PostMapping
    public ProfileDto create(@RequestBody ProfileDto dto) {
        return service.create(dto);
    }
}
