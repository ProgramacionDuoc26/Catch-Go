package com.catchandgo.matching.dto;

import java.util.List;

public record TrabajadorMatchDto(
    Long id,
    String nombre,
    List<String> habilidades,
    Integer experienciaMeses,
    Double latitud,
    Double longitud,
    Integer tiempoViajeMinutos,
    List<VentanaDisponibilidadDto> disponibilidad
) {
}
