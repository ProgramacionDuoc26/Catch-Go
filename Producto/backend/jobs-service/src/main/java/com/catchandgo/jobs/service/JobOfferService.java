package com.catchandgo.jobs.service;

import com.catchandgo.jobs.dto.JobOfferDto;
import com.catchandgo.jobs.dto.JobApplicationDto;
import com.catchandgo.jobs.entity.JobOffer;
import com.catchandgo.jobs.entity.JobApplication;
import com.catchandgo.jobs.mapper.JobOfferMapper;
import com.catchandgo.jobs.repository.JobOfferRepository;
import com.catchandgo.jobs.repository.JobApplicationRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JobOfferService {
    private final JobOfferRepository repository;
    private final JobOfferMapper mapper;
    private final JobApplicationRepository applicationRepository;
    private final NotificationPublisher notificationPublisher;

    public JobOfferService(JobOfferRepository repository, 
                           JobOfferMapper mapper, 
                           JobApplicationRepository applicationRepository,
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
        
        JobOffer offer = repository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Oferta no encontrada"));

        try {
            JobApplication application = new JobApplication();
            application.setJobId(jobId);
            application.setUserId(userId);
            application.setEstado("PENDIENTE");
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

    public List<JobApplicationDto> findApplicationsByUserId(String userId) {
        return applicationRepository.findByUserId(userId).stream()
            .map(app -> {
                JobOffer offer = repository.findById(app.getJobId())
                    .orElse(null);
                
                String title = offer != null ? offer.getTitulo() : "Oferta no encontrada";
                String location = offer != null ? offer.getUbicacion() : "";
                Double lat = offer != null ? offer.getLatitude() : null;
                Double lon = offer != null ? offer.getLongitude() : null;

                return new JobApplicationDto(
                    app.getId(),
                    app.getJobId(),
                    title,
                    app.getUserId(),
                    app.getEstado(),
                    app.getFechaPostulacion(),
                    location,
                    lat,
                    lon
                );
            }).toList();
    }

    public List<JobApplication> findApplicationsByEmpresaId(String empresaId) {
        return applicationRepository.findAllByEmpresaId(empresaId);
    }

    public List<JobApplication> findAllApplications() {
        return applicationRepository.findAll();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public JobOfferDto update(Long id, JobOfferDto dto) {
        JobOffer entity = repository.findById(id).orElseThrow();
        entity.setTitulo(dto.titulo());
        entity.setDescripcion(dto.descripcion());
        entity.setUbicacion(dto.ubicacion());
        entity.setRemuneracion(dto.remuneracion());
        entity.setFechaInicio(dto.fechaInicio());
        entity.setLatitude(dto.latitude());
        entity.setLongitude(dto.longitude());
        entity.setCategoria(dto.categoria());
        return mapper.toDto(repository.save(entity));
    }

    public void deleteApplication(Long id) {
        applicationRepository.deleteById(id);
    }

    public void updateApplicationStatus(Long id, String status) {
        JobApplication application = applicationRepository.findById(id).orElseThrow();
        application.setEstado(status);
        applicationRepository.save(application);

        String title;
        String message;
        String type;

        switch (status) {
            case "ACEPTADO":
                title = "¡Postulación Aceptada!";
                message = "Has sido seleccionado para un nuevo turno.";
                type = "success";
                break;
            case "RECHAZADO":
                title = "Postulación Rechazada";
                message = "Lo sentimos, no has sido seleccionado para esta oferta.";
                type = "warning";
                break;
            case "PAGO_ENVIADO":
                title = "Pago Enviado";
                message = "La empresa ha enviado un comprobante de pago. Por favor, revísalo en tu panel.";
                type = "info";
                break;
            case "PAGO_CONFIRMADO":
                title = "Pago Confirmado";
                message = "La recepción del pago ha sido confirmada.";
                type = "success";
                break;
            case "CALIFICADO_EMPRESA":
            case "CALIFICADO_TRABAJADOR":
                title = "Calificación Registrada";
                message = "Se ha registrado una nueva calificación en el turno.";
                type = "success";
                break;
            default:
                title = "Actualización de Postulación";
                message = "El estado de tu postulación ha cambiado.";
                type = "info";
                break;
        }

        notificationPublisher.sendNotification(
            application.getUserId(),
            title,
            message,
            type
        );
    }
}
