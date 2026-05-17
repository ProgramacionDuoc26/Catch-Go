package com.catchandgo.matching.service;

import com.catchandgo.matching.dto.SugerenciaMatchDto;
import com.catchandgo.matching.dto.SolicitudEjecucionMatchDto;
import com.catchandgo.matching.mapper.SugerenciaMatchMapper;
import com.catchandgo.matching.repository.SugerenciaMatchRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SugerenciaMatchService {
    private final SugerenciaMatchRepository repository;
    private final SugerenciaMatchMapper mapper;
    private final CalculadoraMatch calculadora;

    public SugerenciaMatchService(
        SugerenciaMatchRepository repository,
        SugerenciaMatchMapper mapper,
        CalculadoraMatch calculadora
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.calculadora = calculadora;
    }

    public List<SugerenciaMatchDto> buscarTodas() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public List<SugerenciaMatchDto> buscarPorOfertaTrabajoId(Long ofertaTrabajoId) {
        return repository.findByOfertaTrabajoIdOrderByPuntajeTotalDesc(ofertaTrabajoId)
            .stream()
            .map(mapper::toDto)
            .toList();
    }

    public SugerenciaMatchDto crear(SugerenciaMatchDto dto) {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }

    @Transactional
    public List<SugerenciaMatchDto> ejecutarMatching(SolicitudEjecucionMatchDto solicitud) {
        validarSolicitud(solicitud);

        Long ofertaTrabajoId = solicitud.ofertaTrabajo().id();
        repository.deleteByOfertaTrabajoId(ofertaTrabajoId);

        return solicitud.trabajadores().stream()
            .filter(trabajador -> trabajador.id() != null)
            .map(trabajador -> calculadora.calcular(solicitud.ofertaTrabajo(), trabajador))
            .map(repository::save)
            .map(mapper::toDto)
            .sorted((izquierda, derecha) -> Double.compare(derecha.puntajeTotal(), izquierda.puntajeTotal()))
            .toList();
    }

    private void validarSolicitud(SolicitudEjecucionMatchDto solicitud) {
        if (solicitud == null || solicitud.ofertaTrabajo() == null || solicitud.ofertaTrabajo().id() == null) {
            throw new IllegalArgumentException("La oferta de trabajo es obligatoria para ejecutar matching.");
        }
        if (solicitud.trabajadores() == null || solicitud.trabajadores().isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos un trabajador candidato.");
        }
    }
}
