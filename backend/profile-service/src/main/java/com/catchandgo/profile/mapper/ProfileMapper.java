package com.catchandgo.profile.mapper;

import com.catchandgo.profile.dto.ProfileDto;
import com.catchandgo.profile.entity.Profile;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {
    public ProfileDto toDto(Profile entity) {
        return new ProfileDto(entity.getId(), entity.getName());
    }

    public Profile toEntity(ProfileDto dto) {
        Profile entity = new Profile();
        entity.setId(dto.id());
        entity.setName(dto.name());
        return entity;
    }
}
