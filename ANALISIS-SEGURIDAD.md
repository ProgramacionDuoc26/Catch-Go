# Analisis de Seguridad - Catch-Go

Fecha: 2026-05-08
Alcance: revision estatica del repositorio local (sin cambios de codigo)

## Resumen ejecutivo

El estado actual presenta riesgos de seguridad de prioridad alta.
El problema principal no es un bug aislado, sino una combinacion de:

- secretos debiles/hardcodeados,
- autorizacion abierta en gateway y servicios,
- exposicion de datos sensibles,
- endpoints de archivos sin controles fuertes.

Con la configuracion actual, un usuario no autenticado podria acceder a recursos que deberian estar protegidos.

## Hallazgos criticos

### 1) Secreto JWT hardcodeado

- Archivo: `backend/auth-service/src/main/java/com/catchandgo/auth/AuthServiceApplication.java:25`
- Hallazgo: se instancia `JwtService` con un secreto fijo en codigo.
- Riesgo: invalida la estrategia de secretos por entorno y facilita compromiso de tokens si el secreto se filtra.

### 2) Gateway con acceso abierto

- Archivo: `backend/api-gateway/src/main/java/com/catchandgo/gateway/security/GatewaySecurityConfig.java:18`
- Hallazgo: `anyExchange().permitAll()`.
- Riesgo: no hay barrera de autenticacion/autorizacion en el punto de entrada principal.

### 3) Microservicios con reglas permisivas

- Archivos:
  - `backend/auth-service/src/main/java/com/catchandgo/auth/AuthServiceApplication.java:32`
  - `backend/jobs-service/src/main/java/com/catchandgo/jobs/JobsServiceApplication.java:19`
  - `backend/profile-service/src/main/java/com/catchandgo/profile/ProfileServiceApplication.java:19`
- Hallazgo: reglas globales con `permitAll()` para cualquier request.
- Riesgo: acceso no autenticado a operaciones de negocio.

### 4) Exposicion de datos sensibles en perfiles

- Archivos:
  - `backend/profile-service/src/main/java/com/catchandgo/profile/dto/ProfileDto.java:15`
  - `backend/profile-service/src/main/java/com/catchandgo/profile/controller/ProfileController.java:18`
- Hallazgo: DTO incluye datos bancarios y rutas publicas listan/consultan perfiles.
- Riesgo: fuga de datos personales y financieros.

### 5) Upload/lectura de archivos sin endurecimiento

- Archivo: `backend/profile-service/src/main/java/com/catchandgo/profile/controller/FileUploadController.java:22`
- Archivo: `backend/profile-service/src/main/java/com/catchandgo/profile/controller/FileUploadController.java:60`
- Hallazgo: falta validacion estricta de tipo, tamano, ownership y sanitizacion robusta.
- Riesgo: abuso de subida/lectura de archivos, exfiltracion y potencial contenido malicioso.

## Hallazgos altos

### 6) Token en localStorage

- Archivo: `frontend/src/lib/api/client.ts:18`
- Hallazgo: token de sesion leido desde `localStorage`.
- Riesgo: mayor exposicion ante XSS frente a enfoque con cookie `httpOnly`.

### 7) Contratos API inconsistentes

- Archivos:
  - `frontend/src/lib/api/matching.ts:18`
  - `backend/matching-service/src/main/java/com/catchandgo/matching/dto/MatchSuggestionDto.java:3`
- Hallazgo: frontend espera campos de matching distintos a los DTO/entity reales del backend.
- Riesgo: errores de validacion, fallas funcionales y bypasses logicos por manejo inconsistente.

### 8) Configuracion de migraciones permisiva

- Archivos:
  - `backend/jobs-service/src/main/resources/application.yml:20`
  - `backend/profile-service/src/main/resources/application.yml:20`
  - `backend/matching-service/src/main/resources/application.yml:20`
- Hallazgo: uso de `repair-on-migrate: true` y `validate-on-migrate: false` en servicios.
- Riesgo: deriva de esquema no detectada y perdida de trazabilidad.

### 9) Logging no estructurado en servicios

- Archivos:
  - `backend/jobs-service/src/main/java/com/catchandgo/jobs/service/JobOfferService.java:34`
  - `backend/profile-service/src/main/java/com/catchandgo/profile/service/ProfileService.java:25`
- Hallazgo: uso de `System.out.println`, `System.err.println` y `printStackTrace()`.
- Riesgo: fuga de datos en logs, baja auditabilidad y ruido operativo.

## Impacto potencial

- Acceso no autorizado a datos personales y bancarios.
- Manipulacion de ofertas y postulaciones sin autenticacion real.
- Abuso del modulo de archivos (subida/lectura).
- Riesgo de compromiso de sesion si existe XSS en frontend.

## Plan recomendado (priorizado)

### Prioridad 0 - Contencion inmediata

1. Eliminar secretos hardcodeados y leerlos solo desde entorno.
2. Rotar `JWT_SECRET` y credenciales que hayan estado expuestas.
3. Verificar que archivos `.env` no se publiquen ni se incluyan en artefactos.

### Prioridad 1 - Cerrar superficie publica

1. Reemplazar `permitAll` global por reglas minimas por endpoint.
2. Mantener publicos solo health/docs/login/register segun necesidad real.
3. Requerir autenticacion para endpoints de negocio.

### Prioridad 2 - AuthN/AuthZ consistente

1. Validar JWT en gateway y/o en cada microservicio de forma consistente.
2. Propagar identidad y roles de forma confiable.
3. Implementar autorizacion por recurso (ownership y rol EMPRESA/TRABAJADOR).

### Prioridad 3 - Proteccion de datos

1. Separar DTO publico vs privado para perfiles.
2. Excluir datos bancarios de respuestas generales.
3. Aplicar minima exposicion (need-to-know).

### Prioridad 4 - Endurecer modulo de archivos

1. Allowlist de MIME/extensiones y tamano maximo.
2. Nombres de archivo seguros y bloqueo de rutas peligrosas.
3. Control de acceso para lectura (solo dueno/roles autorizados).
4. Registrar auditoria de subida y descarga.

### Prioridad 5 - Calidad y trazabilidad

1. Migrar logs a `Logger` estructurado.
2. Agregar pruebas de seguridad basicas para auth y permisos.
3. Endurecer politica de migraciones (validaciones activas en entornos no-dev).

## Nota metodologica

Este documento corresponde a analisis de estado actual.
No se realizaron cambios de codigo, estructura de carpetas, ni configuraciones del proyecto durante esta revision.
