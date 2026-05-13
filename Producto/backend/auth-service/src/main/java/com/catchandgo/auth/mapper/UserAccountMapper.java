package com.catchandgo.auth.mapper;

import com.catchandgo.auth.dto.AuthResponseDto;
import com.catchandgo.auth.dto.RegisterRequestDto;
import com.catchandgo.auth.entity.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class UserAccountMapper {
    
    public AuthResponseDto.UserDto toUserDto(UserAccount entity) {
        return new AuthResponseDto.UserDto(
            entity.getId(),
            entity.getEmail(),
            entity.getName(),
            entity.getTipo(),
            entity.getPhone(),
            entity.getNivel()
        );
    }

    public UserAccount toEntity(RegisterRequestDto dto) {
        UserAccount entity = new UserAccount();
        entity.setEmail(dto.email());
        entity.setName(dto.nombre());
        entity.setTipo(dto.tipo());
        entity.setPhone(dto.telefono());
        // Password se setea en el servicio tras hashearlo
        return entity;
    }
}
