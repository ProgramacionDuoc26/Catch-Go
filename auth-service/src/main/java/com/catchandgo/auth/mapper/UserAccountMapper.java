package com.catchandgo.auth.mapper;

import com.catchandgo.auth.dto.UserAccountDto;
import com.catchandgo.auth.entity.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class UserAccountMapper {
    public UserAccountDto toDto(UserAccount entity) {
        return new UserAccountDto(entity.getId(), entity.getName());
    }

    public UserAccount toEntity(UserAccountDto dto) {
        UserAccount entity = new UserAccount();
        entity.setId(dto.id());
        entity.setName(dto.name());
        return entity;
    }
}
