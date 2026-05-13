package com.catchandgo.auth.dto;

public record LoginRequestDto(
    String email,
    String password
) {}
