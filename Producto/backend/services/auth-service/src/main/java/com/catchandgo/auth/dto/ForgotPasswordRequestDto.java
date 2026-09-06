package com.catchandgo.auth.dto;

public record ForgotPasswordRequestDto(
    String email,
    String captchaToken
) {}
