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
    private final com.catchandgo.jobs.repository.JobApplicationRepository applicationRepository;

    public JobOfferService(JobOfferRepository repository, JobOfferMapper mapper, com.catchandgo.jobs.repository.JobApplicationRepository applicationRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.applicationRepository = applicationRepository;
    }

    public List<JobOfferDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public JobOfferDto findById(Long id) {
        return repository.findById(id).map(mapper::toDto).orElseThrow();
    }

    public JobOfferDto create(JobOfferDto dto) {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }

    public void apply(Long jobId, String userId) {
        System.out.println("Applying to jobId: " + jobId + " for userId: " + userId);
        if (applicationRepository.findByUserIdAndJobId(userId, jobId).isPresent()) {
            throw new RuntimeException("Ya has postulado a esta oferta");
        }
        try {
            com.catchandgo.jobs.entity.JobApplication application = new com.catchandgo.jobs.entity.JobApplication();
            application.setJobId(jobId);
            application.setUserId(userId);
            application.setEstado("PENDIENTE");
            applicationRepository.save(application);
            System.out.println("Application saved successfully");
        } catch (Exception e) {
            System.err.println("Error saving application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<com.catchandgo.jobs.dto.JobApplicationDto> findApplicationsByUserId(String userId) {
        return applicationRepository.findByUserId(userId).stream()
            .map(app -> {
                String title = repository.findById(app.getJobId())
                    .map(com.catchandgo.jobs.entity.JobOffer::getTitulo)
                    .orElse("Oferta no encontrada");
                return new com.catchandgo.jobs.dto.JobApplicationDto(
                    app.getId(),
                    app.getJobId(),
                    title,
                    app.getUserId(),
                    app.getEstado(),
                    app.getFechaPostulacion()
                );
            }).toList();
    }

    public List<com.catchandgo.jobs.entity.JobApplication> findApplicationsByEmpresaId(String empresaId) {
        return applicationRepository.findAllByEmpresaId(empresaId);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public JobOfferDto update(Long id, JobOfferDto dto) {
        com.catchandgo.jobs.entity.JobOffer entity = repository.findById(id).orElseThrow();
        entity.setTitulo(dto.titulo());
        entity.setDescripcion(dto.descripcion());
        entity.setUbicacion(dto.ubicacion());
        entity.setRemuneracion(dto.remuneracion());
        entity.setFechaInicio(dto.fechaInicio());
        return mapper.toDto(repository.save(entity));
    }

    public void deleteApplication(Long id) {
        applicationRepository.deleteById(id);
    }

    public void updateApplicationStatus(Long id, String status) {
        com.catchandgo.jobs.entity.JobApplication application = applicationRepository.findById(id).orElseThrow();
        application.setEstado(status);
        applicationRepository.save(application);
    }
}
