package com.catchandgo.matching.dto;

import java.time.LocalDateTime;

public record SugerenciaMatchDto(
    Long id,
    Long ofertaTrabajoId,
    Long trabajadorPerfilId,
    String nombreTrabajador,
    double puntajeTotal,
    double puntajeHabilidades,
    double puntajeExperiencia,
    double puntajeDistancia,
    double puntajeDisponibilidad,
    LocalDateTime fechaCreacion
) {
}
