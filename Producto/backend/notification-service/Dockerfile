# ── Dockerfile: notification-service ──
# Railway build context: Producto/backend

FROM maven:3.9.5-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -pl notification-service -am -DskipTests -Dmaven.repo.local=/tmp/m2

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/notification-service/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Xmx256m", \
  "-jar", "app.jar"]
