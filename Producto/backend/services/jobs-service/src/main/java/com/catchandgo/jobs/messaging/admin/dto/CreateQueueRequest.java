package com.catchandgo.jobs.messaging.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de solicitud para la creación de colas/streams.
 * Cumple con el criterio de validación de entradas de la rúbrica.
 */
public record CreateQueueRequest(
    @NotBlank(message = "El nombre de la cola es obligatorio y no puede estar vacío")
    @Pattern(regexp = "^[a-zA-Z0-9:_-]+$", message = "El nombre de la cola solo puede contener caracteres alfanuméricos, guiones y dos puntos")
    String name,

    String consumerGroup,

    Long maxLen
) {
    public String getEffectiveGroup() {
        return (consumerGroup != null && !consumerGroup.isBlank()) ? consumerGroup : name + "-group";
    }
}
