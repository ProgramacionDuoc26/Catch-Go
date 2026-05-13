package com.catchandgo.auth.dto;

import java.time.LocalDateTime;

public record DocumentoDto(
    Long id,
    String tipo,
    String filename,
    String url,
    LocalDateTime fechaSubida
) {}
