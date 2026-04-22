package com.catchandgo.jobs.mapper;

import com.catchandgo.jobs.dto.JobOfferDto;
import com.catchandgo.jobs.entity.JobOffer;
import org.springframework.stereotype.Component;

@Component
public class JobOfferMapper {
    public JobOfferDto toDto(JobOffer entity) {
        return new JobOfferDto(entity.getId(), entity.getName());
    }

    public JobOffer toEntity(JobOfferDto dto) {
        JobOffer entity = new JobOffer();
        entity.setId(dto.id());
        entity.setName(dto.name());
        return entity;
    }
}
