package com.catchandgo.auth.dto;

import java.util.List;

public record RadarDataDto(
    List<EjeRadarDto> ejes
) {
    public record EjeRadarDto(
        Long categoriaId,
        String nombre,
        Double valor
    ) {}
}
