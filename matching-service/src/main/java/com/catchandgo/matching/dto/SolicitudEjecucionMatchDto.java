package com.catchandgo.matching.dto;

import java.util.List;

public record SolicitudEjecucionMatchDto(
    OfertaTrabajoMatchDto ofertaTrabajo,
    List<TrabajadorMatchDto> trabajadores
) {
}
