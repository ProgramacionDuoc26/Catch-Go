package com.catchandgo.auth.dto;

public record SendOtpRequestDto(
    String email,
    String purpose
) {}
