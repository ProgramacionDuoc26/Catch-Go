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
        System.out.println("Fetching profile for userId: " + userId);
        return repository.findByUserId(userId)
                .map(mapper::toDto)
                .orElse(null);
    }

    public ProfileDto saveOrUpdate(ProfileDto dto) {
        System.out.println("Saving profile for userId: " + dto.getUserId() + ", birthDate: " + dto.getBirthDate());
        Profile entity = repository.findByUserId(dto.getUserId())
                .orElse(new Profile());
        
        // Actualizamos campos
        entity.setUserId(dto.getUserId());
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        
        if (dto.getBirthDate() != null && !dto.getBirthDate().isEmpty()) {
            try {
                entity.setBirthDate(java.time.LocalDate.parse(dto.getBirthDate()));
            } catch (Exception e) {
                System.err.println("Error parsing date: " + dto.getBirthDate());
            }
        }
        
        entity.setPhotoUrl(dto.getPhotoUrl());
        entity.setCvUrl(dto.getCvUrl());
        entity.setDescription(dto.getDescription());
        entity.setBankName(dto.getBankName());
        entity.setAccountType(dto.getAccountType());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setType(dto.getType());

        return mapper.toDto(repository.save(entity));
    }
}
