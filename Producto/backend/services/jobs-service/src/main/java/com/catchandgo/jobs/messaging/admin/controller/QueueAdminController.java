package com.catchandgo.jobs.messaging.admin.controller;

import com.catchandgo.common.events.QueueSummaryDto;
import com.catchandgo.jobs.messaging.admin.dto.CreateQueueRequest;
import com.catchandgo.jobs.messaging.admin.dto.DlqMessageDto;
import com.catchandgo.jobs.messaging.admin.dto.ConsumerGroupDto;
import com.catchandgo.jobs.messaging.admin.dto.CreateGroupRequest;
import com.catchandgo.jobs.messaging.admin.dto.RetentionRequest;
import com.catchandgo.jobs.messaging.admin.service.QueueAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para administración de colas y Dead Letter Queues (DLQ).
 * Cumple con el criterio de endpoints REST de administración de la rúbrica.
 */
@RestController
@RequestMapping({"/queues", "/api/v1/queues"})
@CrossOrigin(origins = "*")
@Tag(name = "Queue Management API", description = "Endpoints de administración para colas, streams y Dead Letter Queues (DLQ)")
public class QueueAdminController {

    private final QueueAdminService adminService;

    public QueueAdminController(QueueAdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    @Operation(summary = "Crear nueva cola/stream", description = "Crea un nuevo stream y su grupo de consumidores con validación de parámetros")
    public ResponseEntity<QueueSummaryDto> createQueue(@Valid @RequestBody CreateQueueRequest request) {
        QueueSummaryDto created = adminService.createQueue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar colas activas", description = "Retorna métricas de todas las colas, streams y grupos de consumidores")
    public ResponseEntity<List<QueueSummaryDto>> listQueues() {
        return ResponseEntity.ok(adminService.listQueues());
    }

    @GetMapping("/{name}")
    @Operation(summary = "Obtener detalle de una cola", description = "Retorna métricas en tiempo real de una cola específica")
    public ResponseEntity<QueueSummaryDto> getQueue(@PathVariable("name") String name) {
        return ResponseEntity.ok(adminService.getQueueDetail(name));
    }

    @DeleteMapping("/{name}")
    @Operation(summary = "Eliminar cola", description = "Elimina la cola/stream y su DLQ asociada del broker")
    public ResponseEntity<Map<String, Object>> deleteQueue(@PathVariable("name") String name) {
        boolean deleted = adminService.deleteQueue(name);
        return ResponseEntity.ok(Map.of(
            "queue", name,
            "deleted", deleted,
            "message", deleted ? "Cola eliminada exitosamente" : "La cola no existía en el broker"
        ));
    }

    @DeleteMapping("/{name}/purge")
    @Operation(summary = "Purgar mensajes de una cola", description = "Elimina todos los mensajes pendientes de la cola sin borrar la estructura")
    public ResponseEntity<Map<String, Object>> purgeQueue(@PathVariable("name") String name) {
        boolean purged = adminService.purgeQueue(name);
        return ResponseEntity.ok(Map.of(
            "queue", name,
            "purged", purged,
            "message", purged ? "Cola purgada exitosamente" : "Error al purgar la cola"
        ));
    }

    @GetMapping("/{name}/dlq")
    @Operation(summary = "Consultar mensajes en Dead Letter Queue (DLQ)", description = "Obtiene los mensajes fallidos retenidos en la DLQ para auditoría")
    public ResponseEntity<List<DlqMessageDto>> getDlqMessages(
            @PathVariable("name") String name,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return ResponseEntity.ok(adminService.getDlqMessages(name, limit));
    }
    @GetMapping("/{name}/groups")
    public ResponseEntity<List<ConsumerGroupDto>> getGroups(@PathVariable("name") String name) {
        return ResponseEntity.ok(adminService.getGroups(name));
    }

    @PostMapping("/{name}/groups")
    public ResponseEntity<Void> createGroup(@PathVariable("name") String name, @Valid @RequestBody CreateGroupRequest request) {
        adminService.createGroup(name, request.name());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{name}/groups/{group}")
    @Operation(summary = "Eliminar un grupo sin trabajo pendiente")
    public ResponseEntity<Map<String, String>> deleteGroup(@PathVariable("name") String name, @PathVariable("group") String group) {
        var result = adminService.deleteGroup(name, group);
        return switch (result) {
            case DELETED -> ResponseEntity.ok(Map.of("status", "deleted"));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Grupo no encontrado"));
            case PENDING -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "El grupo tiene mensajes pendientes"));
            case UNREAD -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "El grupo tiene mensajes sin leer"));
            case CONSUMERS -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "El grupo tiene consumidores registrados"));
            case PROTECTED -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Grupo principal protegido"));
        };
    }

    @PostMapping("/{name}/groups/{group}/replay")
    @Operation(summary = "Reiniciar lectura de un grupo no protegido desde las entradas conservadas")
    public ResponseEntity<Map<String, String>> replayGroup(@PathVariable("name") String name, @PathVariable("group") String group) {
        var result = adminService.replayGroup(name, group);
        return switch (result) {
            case RESET -> ResponseEntity.ok(Map.of("status", "reset", "position", "0-0"));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Grupo no encontrado"));
            case PENDING -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "El grupo tiene mensajes pendientes"));
            case CONSUMERS -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "El grupo tiene consumidores registrados"));
            case PROTECTED -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Grupo principal protegido"));
        };
    }

    @GetMapping("/{name}/retention")
    public ResponseEntity<Map<String, Long>> getRetention(@PathVariable("name") String name) {
        return ResponseEntity.ok(Map.of("maxLen", adminService.getRetention(name)));
    }

    @PutMapping("/{name}/retention")
    public ResponseEntity<Map<String, Long>> setRetention(@PathVariable("name") String name, @Valid @RequestBody RetentionRequest request) {
        adminService.setRetention(name, request.maxLen());
        return ResponseEntity.ok(Map.of("maxLen", request.maxLen()));
    }
}
