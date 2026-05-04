# Catch & Go — Rama: feature/microservices-base

Rama que contiene la arquitectura base de microservicios del backend de la plataforma Catch & Go. Implementa un monorepo Maven multi-módulo con Java 21 y Spring Boot 3, siguiendo una arquitectura de microservicios para el desarrollo del MVP académico.

## Propósito de esta Rama

Establecer la estructura base del backend mediante microservicios independientes, cada uno con su propia base de datos, expuestos a través de un API Gateway centralizado.

## Microservicios Implementados

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| `api-gateway` | 8080 | Punto de entrada único, enrutamiento y seguridad |
| `auth-service` | 8081 | Registro de usuarios, inicio de sesión y tokens JWT |
| `profile-service` | 8082 | Gestión de perfiles de trabajadores y empresas |
| `jobs-service` | 8083 | Publicación y administración de ofertas laborales |
| `matching-service` | 8084 | Algoritmo de emparejamiento entre candidatos y ofertas |

## Librerías Compartidas (common-*)

| Módulo | Función |
|---|---|
| `common-jwt` | Generación y validación de tokens JWT |
| `common-security` | Configuración base de Spring Security |
| `common-exceptions` | Manejo centralizado de errores HTTP |
| `common-web` | Estructura estándar de respuestas API (`ApiResponse`) |
| `common-events` | Definición de eventos de dominio entre servicios |
| `common-test` | Clase base para pruebas de integración con Testcontainers |

## Stack Tecnológico

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 3.3.x · Spring Cloud Gateway
- **Seguridad:** Spring Security + JWT
- **Base de Datos:** PostgreSQL 15 + Flyway (migraciones)
- **Caché:** Redis 7
- **Documentación API:** OpenAPI / Swagger UI
- **Pruebas:** JUnit 5 · Mockito · Testcontainers

## Requisitos Previos

- Java 21
- Maven 3.9+
- Docker y Docker Compose

## Levantar el Entorno de Desarrollo

```bash
# Copiar y completar variables de entorno
cp .env.example .env

# Levantar PostgreSQL y Redis
docker compose up -d

# Compilar todos los módulos y ejecutar pruebas
mvn clean verify
```

## Ejecutar Servicios de Forma Individual

```bash
# Desde la raíz del proyecto
mvn -pl api-gateway spring-boot:run
mvn -pl auth-service spring-boot:run
mvn -pl profile-service spring-boot:run
mvn -pl jobs-service spring-boot:run
mvn -pl matching-service spring-boot:run
```

## Endpoints de Verificación

| Servicio | URL |
|---|---|
| Gateway (health check) | `http://localhost:8080/actuator/health` |
| Auth Service (Swagger) | `http://localhost:8081/swagger-ui.html` |
| Profile Service (Swagger) | `http://localhost:8082/swagger-ui.html` |
| Jobs Service (Swagger) | `http://localhost:8083/swagger-ui.html` |
| Matching Service (Swagger) | `http://localhost:8084/swagger-ui.html` |

## Estructura del Proyecto

```
backend/
├── pom.xml                 ← POM padre del monorepo
├── api-gateway/
├── auth-service/
├── profile-service/
├── jobs-service/
├── matching-service/
├── common-jwt/
├── common-security/
├── common-exceptions/
├── common-web/
├── common-events/
└── common-test/
```
