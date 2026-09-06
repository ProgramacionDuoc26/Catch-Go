package com.catchandgo.auth.dto;

public record ResetPasswordRequestDto(
    String email,
    String code,
    String newPassword
) {}
