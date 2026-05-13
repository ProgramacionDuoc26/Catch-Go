package com.catchandgo.auth.dto;

public record HabilidadUsuarioDto(
    Long id,
    Long habilidadId,
    String nombre,
    Long categoriaId,
    String categoriaNombre,
    Integer puntos,
    Integer trabajosAplicados
) {}
