package com.catchandgo.auth.dto;

public record HabilidadDto(
    Long id,
    String nombre,
    Long categoriaId,
    String categoriaNombre,
    Boolean predeterminada
) {}
