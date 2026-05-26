# ── Dockerfile: auth-service ──
# Railway build context: Producto/backend
# Este archivo compila SOLO el auth-service desde el monorepo Maven

FROM maven:3.9.5-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copiar todo el monorepo Maven
COPY . .

# Compilar solo el módulo auth-service y sus dependencias comunes
RUN mvn clean package -pl auth-service -am -DskipTests -Dmaven.repo.local=/tmp/m2

# ── Runtime ──
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/auth-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Xmx256m", \
  "-jar", "app.jar"]
