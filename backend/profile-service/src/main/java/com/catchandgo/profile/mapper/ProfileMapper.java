package com.catchandgo.profile.mapper;

import com.catchandgo.profile.dto.ProfileDto;
import com.catchandgo.profile.entity.Profile;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class ProfileMapper {
    public ProfileDto toDto(Profile entity) {
        if (entity == null) return null;
        ProfileDto dto = new ProfileDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        if (entity.getBirthDate() != null) {
            dto.setBirthDate(entity.getBirthDate().toString());
        }
        dto.setPhotoUrl(entity.getPhotoUrl());
        dto.setCvUrl(entity.getCvUrl());
        dto.setDescription(entity.getDescription());
        dto.setBankName(entity.getBankName());
        dto.setAccountType(entity.getAccountType());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setType(entity.getType());
        return dto;
    }

    public Profile toEntity(ProfileDto dto) {
        if (dto == null) return null;
        Profile entity = new Profile();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        if (dto.getBirthDate() != null && !dto.getBirthDate().isEmpty()) {
            entity.setBirthDate(LocalDate.parse(dto.getBirthDate()));
        }
        entity.setPhotoUrl(dto.getPhotoUrl());
        entity.setCvUrl(dto.getCvUrl());
        entity.setDescription(dto.getDescription());
        entity.setBankName(dto.getBankName());
        entity.setAccountType(dto.getAccountType());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setType(dto.getType());
        return entity;
    }
}
