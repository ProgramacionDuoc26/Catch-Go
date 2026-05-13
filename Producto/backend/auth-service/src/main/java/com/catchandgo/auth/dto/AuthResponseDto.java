package com.catchandgo.auth.dto;

public record AuthResponseDto(
    String token,
    UserDto usuario
) {
    public record UserDto(
        Long id,
        String email,
        String nombre,
        String tipo,
        String telefono,
        Integer nivel
    ) {}
}
