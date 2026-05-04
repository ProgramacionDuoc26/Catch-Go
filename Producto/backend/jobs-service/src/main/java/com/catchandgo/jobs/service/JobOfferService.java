package com.catchandgo.jobs.service;

import com.catchandgo.jobs.dto.JobOfferDto;
import com.catchandgo.jobs.mapper.JobOfferMapper;
import com.catchandgo.jobs.repository.JobOfferRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JobOfferService {
    private final JobOfferRepository repository;
    private final JobOfferMapper mapper;

    public JobOfferService(JobOfferRepository repository, JobOfferMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<JobOfferDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public JobOfferDto create(JobOfferDto dto) {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }
}
