# ── Etapa 1: Compilación (Builder) ──
FROM maven:3.9.5-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copiamos todo el código fuente del monorepo
COPY . .

# Compilamos todo el proyecto omitiendo los tests para acelerar la construcción
RUN mvn clean package -DskipTests

# ── Etapa 2: Runtime ──
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Argumento para indicar qué módulo (microservicio) empaquetar en la imagen
ARG MODULE
ENV MODULE=${MODULE}

# Crear directorio de uploads y usuario sin privilegios
RUN addgroup -S spring && adduser -S spring -G spring && \
    mkdir -p /app/uploads && \
    chown -R spring:spring /app/uploads

USER spring:spring

# Copiamos solo el JAR del módulo específico desde la etapa de compilación
# Nota: La convención de Spring Boot con Maven nombra el archivo como <artifactId>-<version>.jar
# Asumimos que el jar generado termina en -0.0.1-SNAPSHOT.jar según el pom.xml.
COPY --from=builder /app/${MODULE}/target/*.jar app.jar

# Puerto por defecto (se sobrescribe en docker-compose si es necesario)
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Xmx256m", "-jar", "app.jar"]
