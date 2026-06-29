package com.catchandgo.profile.controller;

import com.catchandgo.profile.dto.ProfileDto;
import com.catchandgo.profile.service.ProfileService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ProfileDto> findByUserId(@PathVariable("userId") String userId) {
        ProfileDto profile = service.findByUserId(userId);
        if (profile == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(profile);
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody ProfileDto dto) {
        try {
            ProfileDto saved = service.saveOrUpdate(dto);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            System.err.println("[ProfileController] Validation error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("[ProfileController] Error saving profile: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteByUserId(@PathVariable("userId") String userId) {
        service.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}

