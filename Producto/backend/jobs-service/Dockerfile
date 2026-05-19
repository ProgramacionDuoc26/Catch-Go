# ── Dockerfile: jobs-service ──
# Railway build context: Producto/backend

FROM maven:3.9.5-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/jobs-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Xmx256m", \
  "-jar", "app.jar"]
