package com.catchandgo.profile.controller;

import com.catchandgo.profile.dto.ProfileDto;
import com.catchandgo.profile.service.ProfileService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileDto> findByUserId(@PathVariable String userId) {
        ProfileDto profile = service.findByUserId(userId);
        if (profile == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(profile);
    }

    @PostMapping
    public ProfileDto save(@RequestBody ProfileDto dto) {
        return service.saveOrUpdate(dto);
    }
}

