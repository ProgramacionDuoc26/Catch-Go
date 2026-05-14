package com.catchandgo.matching.controller;

import com.catchandgo.matching.dto.SugerenciaMatchDto;
import com.catchandgo.matching.dto.SolicitudEjecucionMatchDto;
import com.catchandgo.matching.service.SugerenciaMatchService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matching")
public class SugerenciaMatchController {
    private final SugerenciaMatchService servicio;

    public SugerenciaMatchController(SugerenciaMatchService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<SugerenciaMatchDto> buscarTodas() {
        return servicio.buscarTodas();
    }

    @GetMapping({"/ofertas-trabajo/{ofertaTrabajoId}/sugerencias", "/job-offers/{ofertaTrabajoId}/sugerencias"})
    public List<SugerenciaMatchDto> buscarPorOfertaTrabajoId(@PathVariable Long ofertaTrabajoId) {
        return servicio.buscarPorOfertaTrabajoId(ofertaTrabajoId);
    }

    @PostMapping
    public SugerenciaMatchDto crear(@RequestBody SugerenciaMatchDto dto) {
        return servicio.crear(dto);
    }

    @PostMapping({"/ejecutar", "/run"})
    public List<SugerenciaMatchDto> ejecutarMatching(@RequestBody SolicitudEjecucionMatchDto solicitud) {
        return servicio.ejecutarMatching(solicitud);
    }
}
