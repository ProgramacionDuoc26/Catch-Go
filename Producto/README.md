# Producto — Catch & Go

Esta carpeta contiene el código fuente completo de la plataforma Catch & Go, organizado por componente. Incluye el backend con microservicios Java, el frontend web en Next.js y la aplicación móvil Android.

## Estructura del Producto

```
Producto/
├── backend/            → Microservicios Java (Spring Boot)
├── frontend/           → Aplicación web (Next.js + TypeScript)
├── android-app/        → Aplicación móvil (Android + Kotlin)
└── docker-compose.yml  → Orquestación de servicios de infraestructura
```

---

## Backend — Microservicios Java

Arquitectura de microservicios construida sobre Spring Boot 3 y Spring Cloud. Cada servicio tiene responsabilidades delimitadas y se comunica a través del API Gateway.

### Servicios disponibles

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| `api-gateway` | 8080 | Enrutamiento, seguridad y punto de entrada único |
| `auth-service` | 8081 | Registro, inicio de sesión y gestión de tokens JWT |
| `profile-service` | 8082 | Perfiles de trabajadores y empresas |
| `jobs-service` | 8083 | Publicación y gestión de ofertas laborales |
| `matching-service` | 8084 | Algoritmo de matching entre candidatos y ofertas |

### Librerías comunes (shared libraries)

| Módulo | Descripción |
|---|---|
| `common-jwt` | Generación y validación de tokens JWT |
| `common-security` | Configuración de seguridad base (Spring Security) |
| `common-exceptions` | Manejo centralizado de errores y excepciones |
| `common-web` | Estructura estándar de respuestas HTTP (`ApiResponse`) |
| `common-events` | Definición de eventos de dominio |
| `common-test` | Configuración base para pruebas de integración |

### Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 15 (ver `docker-compose.yml`)
- Redis 7 (ver `docker-compose.yml`)

### Ejecución

```bash
# Desde la raíz del proyecto, levantar la infraestructura
docker compose up -d

# Compilar todos los módulos
cd backend
mvn clean install

# Ejecutar un microservicio específico (ejemplo: auth-service)
cd backend/auth-service
mvn spring-boot:run
```

### Base de Datos

Las migraciones de base de datos se gestionan automáticamente con **Flyway**. Los scripts SQL se encuentran en:
```
backend/<servicio>/src/main/resources/db/migration/V1__init.sql
```

---

## Frontend Web — Next.js

Aplicación web desarrollada con Next.js 14, TypeScript y Tailwind CSS. Ofrece interfaces diferenciadas para dos tipos de usuarios: **trabajadores** y **empresas**.

### Funcionalidades principales

**Panel de Trabajador:**
- Dashboard con resumen de actividad
- Exploración y postulación a ofertas laborales
- Gestión de perfil personal
- Seguimiento de postulaciones
- Configuración de cuenta

**Panel de Empresa:**
- Dashboard con métricas de publicaciones
- Creación y gestión de ofertas laborales
- Revisión de candidatos postulados
- Perfil corporativo
- Gestión de suscripción

**Páginas públicas:**
- Inicio
- Quiénes somos
- Contacto
- Inicio de sesión y registro

### Requisitos

- Node.js 20+
- npm o yarn
- PostgreSQL 15 (vía Supabase o Docker)

### Ejecución

```bash
cd frontend
npm install
cp .env.example .env.local   # Completar variables de entorno
npm run dev                   # Servidor de desarrollo en http://localhost:3000
```

### Variables de entorno requeridas

Ver el archivo `.env.example` en la raíz del proyecto para el listado completo de variables necesarias (credenciales de Supabase, URL de base de datos, secretos JWT, etc.).

---

## Aplicación Móvil — Android

Aplicación nativa para Android desarrollada en Kotlin con Jetpack Compose. Implementa arquitectura limpia (Clean Architecture) con el patrón MVVM.

### Funcionalidades implementadas

- **Design System:** Sistema de diseño propio con tipografía, colores, espaciado y componentes reutilizables (botones, tarjetas, campos de texto, estados de carga)
- **Capa de red:** Cliente HTTP con Retrofit, interceptor de autenticación JWT y mapeo de errores
- **Autenticación:** Pantalla de inicio de sesión con ViewModel, manejo de sesión persistente con DataStore y control de navegación por rol

### Arquitectura

```
android-app/app/src/main/java/cl/catchgo/app/
├── core/           → Utilidades transversales (errores, estados UI)
├── data/           → Fuentes de datos remotas y locales
├── di/             → Módulos de inyección de dependencias (Hilt)
├── domain/         → Modelos y contratos de repositorio
└── ui/             → Pantallas, ViewModels y componentes visuales
```

### Requisitos

- Android Studio Hedgehog o superior
- SDK mínimo: Android 8.0 (API 26)
- SDK objetivo: Android 14 (API 34)

### Ejecución

1. Abrir la carpeta `android-app/` en Android Studio.
2. Sincronizar dependencias con Gradle.
3. Ejecutar en emulador o dispositivo físico.

---

## Infraestructura — Docker Compose

El archivo `docker-compose.yml` levanta los servicios de base de datos y caché necesarios para el entorno de desarrollo local.

| Servicio | Imagen | Puerto | Descripción |
|---|---|---|---|
| `postgres` | PostgreSQL 15 | 5432 | Base de datos relacional principal |
| `redis` | Redis 7 | 6379 | Caché y almacenamiento de sesiones |

```bash
# Levantar infraestructura
docker compose up -d

# Detener infraestructura
docker compose down
```
