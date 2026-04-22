# catch-and-go-monorepo

Monorepo Maven multi-módulo con Java 21 y Spring Boot 3.x para un MVP académico con arquitectura de microservicios.

## Módulos

- Servicios: `api-gateway`, `auth-service`, `profile-service`, `jobs-service`, `matching-service`
- Librerías compartidas: `common-security`, `common-jwt`, `common-web`, `common-exceptions`, `common-test`, `common-events`

## Stack

- Spring Boot 3.3.x
- Spring Cloud Gateway
- Spring Security + JWT
- PostgreSQL + Flyway
- Redis
- OpenAPI/Swagger
- Actuator
- JUnit 5 + Mockito + Testcontainers

## Requisitos

- Java 21
- Maven 3.9+
- Docker + Docker Compose

## Levantar infraestructura local

```bash
cp .env.example .env
docker compose up -d
```

## Compilar y probar

```bash
mvn clean verify
```

## Ejecutar servicios en local

```bash
mvn -pl api-gateway spring-boot:run
mvn -pl auth-service spring-boot:run
mvn -pl profile-service spring-boot:run
mvn -pl jobs-service spring-boot:run
mvn -pl matching-service spring-boot:run
```

## Endpoints útiles

- Gateway: `http://localhost:8080/actuator/health`
- Auth docs: `http://localhost:8081/swagger-ui.html`
- Profile docs: `http://localhost:8082/swagger-ui.html`
- Jobs docs: `http://localhost:8083/swagger-ui.html`
- Matching docs: `http://localhost:8084/swagger-ui.html`
