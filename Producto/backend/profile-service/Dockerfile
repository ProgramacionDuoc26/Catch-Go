# ── Dockerfile: profile-service ──
# Railway build context: Producto/backend

FROM maven:3.9.5-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring && \
    mkdir -p /app/uploads && \
    chown -R spring:spring /app/uploads

USER spring:spring

COPY --from=builder /app/profile-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Xmx256m", \
  "-jar", "app.jar"]
