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
    private final NotificationPublisher notificationPublisher;

    public JobOfferService(JobOfferRepository repository, 
                           JobOfferMapper mapper, 
                           com.catchandgo.jobs.repository.JobApplicationRepository applicationRepository,
                           NotificationPublisher notificationPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.applicationRepository = applicationRepository;
        this.notificationPublisher = notificationPublisher;
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
        if (applicationRepository.findByUserIdAndJobId(userId, jobId).isPresent()) {
            throw new RuntimeException("Ya has postulado a esta oferta");
        }
        
        com.catchandgo.jobs.entity.JobOffer offer = repository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Oferta no encontrada"));

        try {
            com.catchandgo.jobs.entity.JobApplication application = new com.catchandgo.jobs.entity.JobApplication();
            application.setJobId(jobId);
            application.setUserId(userId);
            application.setEstado("PENDIENTE");
            application.setEmpresaId(offer.getEmpresaId()); // Asegurar que tenga el ID de la empresa
            applicationRepository.save(application);

            // Notificar a la Empresa
            notificationPublisher.sendNotification(
                offer.getEmpresaId(), 
                "Nueva Postulación", 
                "Un trabajador ha postulado a tu oferta: " + offer.getTitulo(),
                "info"
            );

        } catch (Exception e) {
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

        String title = status.equals("ACEPTADO") ? "¡Postulación Aceptada!" : "Postulación Rechazada";
        String message = status.equals("ACEPTADO") 
            ? "Has sido seleccionado para un nuevo turno." 
            : "Lo sentimos, no has sido seleccionado para esta oferta.";
        String type = status.equals("ACEPTADO") ? "success" : "warning";

        notificationPublisher.sendNotification(
            application.getUserId(),
            title,
            message,
            type
        );
    }
}
