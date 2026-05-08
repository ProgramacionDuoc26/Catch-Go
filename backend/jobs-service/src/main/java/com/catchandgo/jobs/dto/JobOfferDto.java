package com.catchandgo.jobs.dto;

public record JobOfferDto(
    Long id, 
    String titulo, 
    String descripcion, 
    String ubicacion, 
    Integer remuneracion, 
    String fechaInicio, 
    String fechaFin, 
    String estado, 
    String empresaId
) {
}
