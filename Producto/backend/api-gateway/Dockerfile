# ── Dockerfile: api-gateway ──
# Railway build context: Producto/backend
# El api-gateway es el único servicio con dominio público expuesto

FROM maven:3.9.5-eclipse-temurin-21-alpine AS builder
WORKDIR /app
ARG CACHE_BUST=1
COPY . .
RUN mvn clean package -pl api-gateway -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Xmx256m", \
  "-jar", "app.jar"]
