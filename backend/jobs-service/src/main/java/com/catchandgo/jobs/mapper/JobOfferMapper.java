package com.catchandgo.jobs.mapper;

import com.catchandgo.jobs.dto.JobOfferDto;
import com.catchandgo.jobs.entity.JobOffer;
import org.springframework.stereotype.Component;

@Component
public class JobOfferMapper {
    public JobOfferDto toDto(JobOffer entity) {
        return new JobOfferDto(
            entity.getId(), 
            entity.getTitulo(),
            entity.getDescripcion(),
            entity.getUbicacion(),
            entity.getRemuneracion(),
            entity.getFechaInicio(),
            entity.getFechaFin(),
            entity.getEstado(),
            entity.getEmpresaId()
        );
    }

    public JobOffer toEntity(JobOfferDto dto) {
        JobOffer entity = new JobOffer();
        entity.setId(dto.id());
        entity.setTitulo(dto.titulo());
        entity.setDescripcion(dto.descripcion());
        entity.setUbicacion(dto.ubicacion());
        entity.setRemuneracion(dto.remuneracion());
        entity.setFechaInicio(dto.fechaInicio());
        entity.setFechaFin(dto.fechaFin());
        entity.setEstado(dto.estado());
        entity.setEmpresaId(dto.empresaId());
        return entity;
    }
}
