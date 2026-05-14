package com.catchandgo.matching.dto;

import java.util.List;

public record OfertaTrabajoMatchDto(
    Long id,
    String titulo,
    Long empresaId,
    List<String> habilidadesRequeridas,
    Integer experienciaRequeridaMeses,
    Double latitud,
    Double longitud,
    List<VentanaDisponibilidadDto> disponibilidadRequerida
) {
}
