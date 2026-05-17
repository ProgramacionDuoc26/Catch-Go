package com.catchandgo.matching.mapper;

import com.catchandgo.matching.dto.SugerenciaMatchDto;
import com.catchandgo.matching.entity.SugerenciaMatch;
import org.springframework.stereotype.Component;

@Component
public class SugerenciaMatchMapper {
    public SugerenciaMatchDto toDto(SugerenciaMatch entity) {
        return new SugerenciaMatchDto(
            entity.getId(),
            entity.getOfertaTrabajoId(),
            entity.getTrabajadorPerfilId(),
            entity.getNombreTrabajador(),
            entity.getPuntajeTotal(),
            entity.getPuntajeHabilidades(),
            entity.getPuntajeExperiencia(),
            entity.getPuntajeDistancia(),
            entity.getPuntajeDisponibilidad(),
            entity.getFechaCreacion()
        );
    }

    public SugerenciaMatch toEntity(SugerenciaMatchDto dto) {
        SugerenciaMatch entity = new SugerenciaMatch();
        entity.setId(dto.id());
        entity.setOfertaTrabajoId(dto.ofertaTrabajoId());
        entity.setTrabajadorPerfilId(dto.trabajadorPerfilId());
        entity.setNombreTrabajador(dto.nombreTrabajador());
        entity.setPuntajeTotal(dto.puntajeTotal());
        entity.setPuntajeHabilidades(dto.puntajeHabilidades());
        entity.setPuntajeExperiencia(dto.puntajeExperiencia());
        entity.setPuntajeDistancia(dto.puntajeDistancia());
        entity.setPuntajeDisponibilidad(dto.puntajeDisponibilidad());
        entity.setFechaCreacion(dto.fechaCreacion());
        return entity;
    }
}
