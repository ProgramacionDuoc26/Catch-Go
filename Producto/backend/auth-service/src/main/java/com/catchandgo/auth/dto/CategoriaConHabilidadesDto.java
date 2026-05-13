package com.catchandgo.auth.dto;

import java.util.List;

public record CategoriaConHabilidadesDto(
    Long id,
    String nombre,
    List<HabilidadDto> habilidades
) {}
