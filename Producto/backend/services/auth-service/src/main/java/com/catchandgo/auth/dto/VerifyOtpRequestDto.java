package com.catchandgo.auth.dto;

public record VerifyOtpRequestDto(
    String email,
    String code,
    String purpose
) {}
