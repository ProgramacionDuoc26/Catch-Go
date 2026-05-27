package com.catchandgo.profile.service;

import com.catchandgo.profile.dto.ProfileDto;
import com.catchandgo.profile.entity.Profile;
import com.catchandgo.profile.mapper.ProfileMapper;
import com.catchandgo.profile.repository.ProfileRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final ProfileRepository repository;
    private final ProfileMapper mapper;

    public ProfileService(ProfileRepository repository, ProfileMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ProfileDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public ProfileDto findByUserId(String userId) {
        return repository.findFirstByUserId(userId)
                .map(mapper::toDto)
                .orElse(null);
    }

    public ProfileDto saveOrUpdate(ProfileDto dto) {
        System.out.println("[ProfileService] saveOrUpdate called for userId: " + (dto != null ? dto.getUserId() : "null"));
        if (dto == null || dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("El userId no puede ser nulo o vacío");
        }

        try {
            Profile entity = repository.findFirstByUserId(dto.getUserId())
                    .orElse(new Profile());
            
            System.out.println("[ProfileService] Found entity with id: " + entity.getId() + " for userId: " + dto.getUserId());
            
            // Actualizamos campos
            entity.setUserId(dto.getUserId());
            entity.setName(dto.getName());
            entity.setEmail(dto.getEmail());
            entity.setPhone(dto.getPhone());
            
            if (dto.getBirthDate() != null && !dto.getBirthDate().isEmpty()) {
                try {
                    entity.setBirthDate(java.time.LocalDate.parse(dto.getBirthDate()));
                } catch (Exception e) {
                    System.err.println("[ProfileService] Error parsing date: " + dto.getBirthDate());
                }
            }
            
            entity.setPhotoUrl(dto.getPhotoUrl());
            entity.setCvUrl(dto.getCvUrl());
            entity.setDescription(dto.getDescription());
            entity.setBankName(dto.getBankName());
            entity.setAccountType(dto.getAccountType());
            entity.setAccountNumber(dto.getAccountNumber());
            entity.setType(dto.getType());
            entity.setLatitude(dto.getLatitude());
            entity.setLongitude(dto.getLongitude());
            entity.setSkills(dto.getSkills());
            entity.setRut(dto.getRut());
            
            if (dto.getRating() != null) {
                entity.setRating(dto.getRating());
            }
            if (dto.getRatingCount() != null) {
                entity.setRatingCount(dto.getRatingCount());
            }

            System.out.println("[ProfileService] Saving entity to database...");
            Profile saved = repository.save(entity);
            System.out.println("[ProfileService] Saved successfully. New/Existing id: " + saved.getId());
            return mapper.toDto(saved);
        } catch (Exception e) {
            System.err.println("[ProfileService] CRITICAL ERROR in saveOrUpdate: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    public void deleteByUserId(String userId) {
        repository.findFirstByUserId(userId).ifPresent(repository::delete);
    }
}
