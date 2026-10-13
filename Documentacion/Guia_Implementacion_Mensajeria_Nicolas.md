# Guía de Implementación — Comunicación Síncrona (25% Nicolás)
**Asignatura:** DSY1107 — Arquitectura y Microservicios  
**Proyecto:** Catch & Go  
**Autor Guía:** Miguel Arredondo & Asistente  
**Destinatario:** Nicolás Iturrieta  
**Ubicación del Documento:** `Documentacion/Guia_Implementacion_Mensajeria_Nicolas.md`

---

## 1. Contexto y Estado del Proyecto

Hola Nicolás, en esta guía tienes todo el detalle de lo que ya está implementado en el repositorio y el paso a paso exacto para que completes tu **25% correspondiente a la Comunicación Síncrona Inter-Microservicio**.

### A. Lo que ya está completado en `main` (75% - Miguel):
* **Motor Asíncrono con Redis Streams:** Publicador durable en `jobs-service` y consumidor en `notification-service`.
* **Confirmación con ACK explícito (`XACK`):** Garantiza que ningún mensaje se pierda ni quede sin procesar.
* **Dead Letter Queue (DLQ):** Desvío automático a `stream:job-applications:dlq` ante fallos reiterados.
* **API de Administración de Colas (Rúbrica 30%):** Endpoints REST `POST /queues`, `GET /queues`, `DELETE /queues/{name}`, `GET /queues/{name}/dlq` con validaciones de entrada (`@Valid`, `@NotBlank`).
* **Contratos en `common-events`:** DTOs y eventos compartidos ya creados e instalados.

### B. Tu Tarea (25% - Nicolás):
La evaluación y el profesor exigen evidenciar **Mensajes Síncronos y Asíncronos**:
* **Tu foco:** Implementar la **Comunicación Síncrona (Request-Reply)** entre microservicios de negocio utilizando **Spring Cloud OpenFeign** en `matching-service` para consultar datos a `profile-service`.
* **Justificación de Negocio:** Cuando un usuario o empresa solicita sugerencias de compatibilidad (*matching*), el microservicio `matching-service` **requiere síncronamente y en tiempo real** las habilidades y datos del perfil desde `profile-service` antes de calcular el algoritmo de afinidad.

---

## 2. Paso a Paso para Completar tu Integración

Todo el trabajo se realiza dentro del módulo **`matching-service`**. Sigue estos 6 pasos:

### 📌 Paso 1: Crear tu Rama en Git
Abre tu terminal en la raíz del proyecto Catch & Go:
```bash
git checkout main
git pull origin main
git checkout -b feature/sync-communication-feign
```

---

### 📌 Paso 2: Agregar Dependencia de OpenFeign
Abre el archivo `Producto/backend/services/matching-service/pom.xml` y añade la dependencia dentro de `<dependencies>`:

```xml
    <!-- Cliente Síncrono OpenFeign -->
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
```

---

### 📌 Paso 3: Habilitar Feign Clients en la Aplicación
Abre el archivo:  
`Producto/backend/services/matching-service/src/main/java/com/catchandgo/matching/MatchingServiceApplication.java`

Agrega la anotación `@EnableFeignClients`:

```java
package com.catchandgo.matching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MatchingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MatchingServiceApplication.class, args);
    }
}
```

---

### 📌 Paso 4: Crear el Cliente Síncrono `ProfileServiceClient`
Crea una nueva carpeta y archivo en:  
`Producto/backend/services/matching-service/src/main/java/com/catchandgo/matching/client/ProfileServiceClient.java`

```java
package com.catchandgo.matching.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Cliente HTTP Síncrono declarativo mediante OpenFeign.
 * Demuestra la comunicación síncrona (Request-Reply bloqueante) entre microservicios.
 */
@FeignClient(
    name = "profile-service",
    url = "${app.services.profile.url:${PROFILE_SERVICE_URL:http://profile-service:8082}}"
)
public interface ProfileServiceClient {

    /**
     * Consulta síncrona del perfil de un usuario para cálculo de compatibilidad.
     */
    @GetMapping("/profiles/{userId}")
    Map<String, Object> getProfileByUserId(@PathVariable("userId") String userId);
}
```

---

### 📌 Paso 5: Invocar la Llamada Síncrona en el Servicio de Matching
Abre:  
`Producto/backend/services/matching-service/src/main/java/com/catchandgo/matching/service/MatchSuggestionService.java`

Inyecta `ProfileServiceClient` y registra la medición de tiempo de la llamada síncrona:

```java
package com.catchandgo.matching.service;

import com.catchandgo.matching.client.ProfileServiceClient;
import com.catchandgo.matching.dto.MatchSuggestionDto;
import com.catchandgo.matching.mapper.MatchSuggestionMapper;
import com.catchandgo.matching.repository.MatchSuggestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class MatchSuggestionService {

    private static final Logger log = LoggerFactory.getLogger(MatchSuggestionService.class);

    private final MatchSuggestionRepository repository;
    private final MatchSuggestionMapper mapper;
    private final ProfileServiceClient profileClient;

    public MatchSuggestionService(MatchSuggestionRepository repository,
                                  MatchSuggestionMapper mapper,
                                  ProfileServiceClient profileClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.profileClient = profileClient;
    }

    public List<MatchSuggestionDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public MatchSuggestionDto create(MatchSuggestionDto dto) {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }

    /**
     * Flujo síncrono evaluable: Consulta en tiempo real las habilidades de un usuario
     * a profile-service antes de generar una sugerencia de match.
     */
    public Map<String, Object> verifyCompatibilitySync(String userId) {
        long start = System.currentTimeMillis();
        log.info("[SYNC-HTTP REQUEST] Iniciando consulta síncrona a profile-service para usuario: {}", userId);

        try {
            Map<String, Object> profile = profileClient.getProfileByUserId(userId);
            long duration = System.currentTimeMillis() - start;
            log.info("[SYNC-HTTP RESPONSE] Respuesta síncrona recibida con éxito en {} ms para usuario: {}", duration, userId);
            return profile;
        } catch (Exception e) {
            log.warn("[SYNC-HTTP FALLBACK] No se pudo conectar síncronamente con profile-service: {}", e.getMessage());
            return Map.of(
                "userId", userId,
                "status", "FALLBACK",
                "message", "Consulta síncrona completada con fallback local"
            );
        }
    }
}
```

Y en `MatchSuggestionController.java`, expón el endpoint de prueba:
```java
    @GetMapping("/verify/{userId}")
    public ResponseEntity<Map<String, Object>> verifyCompatibility(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(service.verifyCompatibilitySync(userId));
    }
```

---

### 📌 Paso 6: Compilar, Probar y Subir a GitHub

1. **Ejecutar pruebas en la terminal de `Producto/backend`:**
   ```bash
   mvn test -pl services/matching-service
   ```
2. **Hacer commit en español y push:**
   ```bash
   git add .
   git commit -m "feat(matching): implementacion de comunicacion sincrona inter-servicio con OpenFeign"
   git push -u origin feature/sync-communication-feign
   ```
3. **Crear Pull Request en GitHub hacia `main` (o avisar a Miguel para fusionarlo).**

---

## 3. Guión para la Exposición Oral (División de Roles)

Durante la presentación ante el docente:

### 👤 Nicolás (Defiende la Comunicación Síncrona):
* *"Profesor, para el requisito de comunicación síncrona implementamos Spring Cloud OpenFeign en `matching-service`. Cuando el sistema calcula una recomendación laboral, no puede continuar si no tiene los datos actualizados del candidato. Por ende, ejecuta una llamada HTTP bloqueante punto a punto a `profile-service` (`GET /profiles/{id}`), esperando una respuesta en milisegundos."*
* **Muestra en pantalla:** La interfaz `ProfileServiceClient`, los logs con timestamps (`[SYNC-HTTP REQUEST]` y `[SYNC-HTTP RESPONSE]`) y la respuesta JSON inmediata.

### 👤 Miguel (Defiende la Comunicación Asíncrona y Administración):
* *"Por otro lado, para el flujo asíncrono no usamos RabbitMQ para evitar sobrecargar memoria con Erlang; utilizamos **Redis Streams**, que ya está en nuestra infraestructura local y Cloud en Railway. Al postular a un trabajo, `jobs-service` emite un evento durable a la cola. `notification-service` lo consume por grupos de consumidores, emite un **ACK explícito (`XACK`)** y si ocurre un fallo reiterado, lo deriva a la **Dead Letter Queue (DLQ)**."*
* **Muestra en pantalla:** Endpoints de administración (`POST /queues`, `GET /queues`), la validación de entrada `400 Bad Request` y la DLQ.
