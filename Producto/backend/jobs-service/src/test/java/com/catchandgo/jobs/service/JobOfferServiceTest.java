package com.catchandgo.jobs.service;

import com.catchandgo.jobs.dto.JobOfferDto;
import com.catchandgo.jobs.entity.JobOffer;
import com.catchandgo.jobs.entity.JobApplication;
import com.catchandgo.jobs.mapper.JobOfferMapper;
import com.catchandgo.jobs.repository.JobOfferRepository;
import com.catchandgo.jobs.repository.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class JobOfferServiceTest {

    @Mock
    private JobOfferRepository repository;

    @Mock
    private JobOfferMapper mapper;

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private JobOfferService service;

    private JobOffer jobOffer;
    private JobOfferDto jobOfferDto;
    private JobApplication jobApplication;

    @BeforeEach
    void setUp() {
        jobOffer = new JobOffer();
        jobOffer.setId(1L);
        jobOffer.setTitulo("Auxiliar de Bodega");
        jobOffer.setDescripcion("Apoyo en inventario");
        jobOffer.setUbicacion("Maipú");
        jobOffer.setRemuneracion(4000);
        jobOffer.setFechaInicio("15-06-2026");
        jobOffer.setEstado("ABIERTA");
        jobOffer.setEmpresaId("empresa-123");
        jobOffer.setLatitude(-33.456);
        jobOffer.setLongitude(-70.648);
        jobOffer.setCategoria("Logística");

        jobOfferDto = new JobOfferDto(
                1L,
                "Auxiliar de Bodega",
                "Apoyo en inventario",
                "Maipú",
                4000,
                "15-06-2026",
                "16-06-2026",
                "ABIERTA",
                "empresa-123",
                -33.456,
                -70.648,
                "Logística",
                null
        );

        jobApplication = new JobApplication();
        jobApplication.setId(10L);
        jobApplication.setJobId(1L);
        jobApplication.setUserId("worker-456");
        jobApplication.setEstado("PENDIENTE");
    }

    // CP-12: Publicar oferta de trabajo exitosamente.
    // Verifica que al crear una nueva oferta con todos los datos correspondientes,
    // se guarde correctamente en el repositorio y devuelva el DTO mapeado.
    @Test
    void create_success() {
        when(mapper.toEntity(jobOfferDto)).thenReturn(jobOffer);
        when(repository.save(jobOffer)).thenReturn(jobOffer);
        when(mapper.toDto(jobOffer)).thenReturn(jobOfferDto);

        JobOfferDto result = service.create(jobOfferDto);

        assertNotNull(result);
        assertEquals(jobOfferDto, result);
        verify(repository).save(jobOffer);
    }

    // CP-14: Postulación exitosa a oferta laboral.
    // Verifica que si un trabajador no tiene postulaciones activas a la oferta,
    // se cree una nueva postulación con estado 'PENDIENTE' y se notifique a la empresa.
    @Test
    void apply_success() {
        when(applicationRepository.findByUserIdAndJobId("worker-456", 1L)).thenReturn(Collections.emptyList());
        when(repository.findById(1L)).thenReturn(Optional.of(jobOffer));
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(jobApplication);
        doNothing().when(notificationPublisher).sendNotification(anyString(), anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> service.apply(1L, "worker-456"));

        verify(applicationRepository).save(any(JobApplication.class));
        verify(notificationPublisher).sendNotification(
                eq("empresa-123"),
                eq("Nueva Postulación"),
                contains("Auxiliar de Bodega"),
                eq("info")
        );
    }

    // CP-14 (Fallo): Intento de postulación duplicada activa.
    // Verifica que si el trabajador ya tiene una postulación activa (por ejemplo, con estado PENDIENTE),
    // el sistema impida registrar la postulación lanzando una excepción explicativa.
    @Test
    void apply_fail_alreadyApplied() {
        List<JobApplication> activeApps = new ArrayList<>();
        activeApps.add(jobApplication); // estado = PENDIENTE

        when(applicationRepository.findByUserIdAndJobId("worker-456", 1L)).thenReturn(activeApps);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.apply(1L, "worker-456");
        });

        assertEquals("Ya tienes una postulación activa a esta oferta", exception.getMessage());
        verify(applicationRepository, never()).save(any());
    }

    // CP-14 (Fallo): Intento de postulación a oferta que no existe.
    // Verifica que lance excepción de 'Oferta no encontrada' si se ingresa un ID incorrecto.
    @Test
    void apply_fail_jobNotFound() {
        when(applicationRepository.findByUserIdAndJobId("worker-456", 1L)).thenReturn(Collections.emptyList());
        when(repository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.apply(1L, "worker-456");
        });

        assertEquals("Oferta no encontrada", exception.getMessage());
        verify(applicationRepository, never()).save(any());
    }

    // CP-16: Aceptación de postulación por parte de la empresa.
    // Verifica que al cambiar el estado de la postulación a 'ACEPTADO',
    // el sistema actualice el estado y envíe una notificación push de éxito al trabajador.
    @Test
    void updateApplicationStatus_accepted() {
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(jobApplication));
        when(applicationRepository.save(jobApplication)).thenReturn(jobApplication);
        doNothing().when(notificationPublisher).sendNotification(anyString(), anyString(), anyString(), anyString());

        service.updateApplicationStatus(10L, "ACEPTADO");

        assertEquals("ACEPTADO", jobApplication.getEstado());
        verify(notificationPublisher).sendNotification(
                eq("worker-456"),
                eq("¡Postulación Aceptada!"),
                eq("Has sido seleccionado para un nuevo turno."),
                eq("success")
        );
    }

    // CP-16: Rechazo de postulación por parte de la empresa.
    // Verifica que al cambiar el estado a 'RECHAZADO',
    // el sistema actualice el estado en BD y envíe una notificación de advertencia (warning) al trabajador.
    @Test
    void updateApplicationStatus_rejected() {
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(jobApplication));
        when(applicationRepository.save(jobApplication)).thenReturn(jobApplication);
        doNothing().when(notificationPublisher).sendNotification(anyString(), anyString(), anyString(), anyString());

        service.updateApplicationStatus(10L, "RECHAZADO");

        assertEquals("RECHAZADO", jobApplication.getEstado());
        verify(notificationPublisher).sendNotification(
                eq("worker-456"),
                eq("Postulación Rechazada"),
                eq("Lo sentimos, no has sido seleccionado para esta oferta."),
                eq("warning")
        );
    }

    // Búsqueda de todas las ofertas de trabajo.
    // Verifica que retorne la lista de ofertas del sistema mapeadas a DTO.
    @Test
    void findAll_success() {
        when(repository.findAll()).thenReturn(List.of(jobOffer));
        when(mapper.toDto(jobOffer)).thenReturn(jobOfferDto);

        List<JobOfferDto> result = service.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(jobOfferDto, result.get(0));
    }
}
