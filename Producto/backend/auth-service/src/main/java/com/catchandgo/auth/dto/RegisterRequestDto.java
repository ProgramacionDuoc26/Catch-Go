package com.catchandgo.auth.dto;

public record RegisterRequestDto(
    String email,
    String password,
    String nombre,
    String tipo,
    String telefono
) {}
