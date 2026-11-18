# Catch & Go — Plataforma de Intermediación Laboral Temporal

Catch & Go es una plataforma transaccional diseñada para conectar de forma ágil y eficiente a empresas que necesitan cubrir turnos de emergencia (contingencias laborales) con trabajadores calificados disponibles en las inmediaciones.

---

## 1. Estructura del Proyecto

El repositorio está organizado en las siguientes carpetas principales:

```text
├── Documentacion/       # Diagramas, Carta Gantt, Informes y Plan de Pruebas (Unidad #3)
├── Gestión/             # Fichas técnicas, datos de integrantes y gestión administrativa
├── Producto/            # Código fuente funcional del proyecto
│   ├── android-app/     # Aplicación móvil nativa en Kotlin para Trabajadores
│   ├── backend/         # Arquitectura de Microservicios en Java / Spring Boot
│   └── frontend/        # Aplicación web en Next.js (TypeScript/Tailwind) para Empresas y Admin
├── docker-compose.yml   # Orquestación de infraestructura y microservicios locales
└── README.md            # Este archivo con las instrucciones del proyecto
```

---

## 2. Arquitectura de Servicios

El ecosistema utiliza una arquitectura distribuida basada en microservicios orquestados por un Gateway centralizado:

| Microservicio | Puerto Local | Puerto Producción (Railway) | Descripción |
| :--- | :---: | :---: | :--- |
| `api-gateway` | `8080` | `443` (Gateway Principal) | Enrutador centralizado y manejo de CORS y políticas de seguridad |
| `auth-service` | `8081` | Interno (vía Gateway `/auth`) | Registro de usuarios, login y generación/validación de tokens JWT |
| `profile-service` | `8082` | Interno (vía Gateway `/profiles`) | Gestión de perfiles, datos bancarios de cobro y almacenamiento de CV |
| `jobs-service` | `8083` | Interno (vía Gateway `/jobs`) | Publicación de ofertas laborales, postulaciones y seguimiento de estados |
| `matching-service` | `8084` | Interno (vía Gateway `/matching`) | Algoritmo de puntuación (Scoring) por habilidades y geodistancia |
| `notification-service`| `8088` (WS) | Interno (vía Gateway `/ws-notifications`) | Notificaciones en tiempo real para cambios de estado (WebSockets) |

### Infraestructura Requerida:
* **Base de Datos:** PostgreSQL (puerto `5432`). Administra tablas transaccionales de usuarios, perfiles, ofertas y postulaciones.
* **Cache & Message Broker:** Redis (puerto `6379`). Utilizado para mensajería en tiempo real y caché rápida de matching.
* **Proveedor de Autenticación Externo:** Supabase OAuth (para autenticación con Google).

---

## 3. Requisitos Previos

Para instalar y ejecutar el proyecto en tu máquina local, asegúrate de tener instalado:

1. **Docker y Docker Compose:** Para levantar la infraestructura de base de datos, caché y todos los microservicios de manera automática.
2. **Java JDK 17:** Requerido si deseas compilar y ejecutar los microservicios sin contenedores Docker.
3. **Node.js (versión 18 o superior) y npm:** Para ejecutar el servidor de desarrollo del frontend en Next.js.
4. **Android Studio y Android SDK (API 33+):** Para compilar e instalar la aplicación móvil en un emulador o dispositivo físico.

---

## 4. Instalación y Ejecución Local

### Paso 1: Levantar el Backend (Docker Compose)

Crea un archivo `.env` a partir de `.env.example` y configura `JWT_SECRET` con un valor aleatorio privado de al menos 32 bytes. `auth-service`, `api-gateway` y `jobs-service` deben usar el mismo valor. No publiques el archivo `.env`. Los tokens de administrador antiguos, emitidos sin rol, deben renovarse iniciando sesión nuevamente.
Para levantar la base de datos (PostgreSQL), la caché (Redis) y compilar todos los microservicios Java automáticamente, ejecuta el siguiente comando en la raíz del repositorio:

```bash
docker-compose up --build -d
```

* El API Gateway estará disponible localmente en `http://localhost:8080`.
* Puedes acceder a la herramienta de visualización de base de datos **Adminer** ingresando en tu navegador a `http://localhost:8085` (Servidor: `postgres`, Usuario/Contraseña: `catchandgo`).

### Inspeccionar streams con Redis Insight

Con el backend levantado, abre `http://localhost:5540`. En Redis Insight agrega una base de datos con host `redis` y puerto `6379`: ese nombre funciona porque ambos contenedores comparten la red de Docker. El Redis local del proyecto no requiere contraseña en la configuración predeterminada. El puerto de la interfaz se puede cambiar mediante `REDIS_INSIGHT_PORT` en `.env`.

En el navegador de claves busca `stream:job-applications`, `stream:application-status` y sus claves terminadas en `:dlq`. Los streams aparecen cuando los servicios los inicializan y contienen eventos cuando se realizan acciones en la aplicación.

Para consultar los grupos y sus métricas desde la terminal:

```powershell
docker compose exec redis redis-cli XINFO GROUPS stream:job-applications
docker compose exec redis redis-cli XPENDING stream:job-applications notifications-group
docker compose exec redis redis-cli XINFO GROUPS stream:application-status
docker compose exec redis redis-cli XPENDING stream:application-status application-status-notifications
```

En `XINFO GROUPS`, `lag` es la cantidad estimada de eventos aún no entregados al grupo cuando Redis puede calcularla. `pending` cuenta los eventos entregados que aún no se han confirmado. Redis Insight se publica solo en la máquina local y el volumen `redis_insight_data` guarda su configuración entre reinicios.

### Inicio de sesión con Google

Configura `NEXT_PUBLIC_SUPABASE_URL` y `NEXT_PUBLIC_SUPABASE_ANON_KEY` en el frontend, y los mismos valores como `SUPABASE_URL` y `SUPABASE_ANON_KEY` en `auth-service`. En Docker Compose, configura estos dos últimos en el `.env` de la raíz. Utiliza la clave pública del proyecto, no una clave de servicio privada.

Al elegir el tipo de cuenta, el backend verifica el token con Supabase y emite un JWT de Catch-Go. El identificador de Supabase se conserva para mantener la relación con los perfiles existentes. El primer tipo elegido queda guardado en el servidor; seleccionar otro posteriormente no modifica el tipo registrado. Las cuentas de Google no se vinculan automáticamente con cuentas locales que tengan el mismo correo.

### Persistencia de Redis Streams en desarrollo

Docker Compose configura Redis con AOF (`appendonly yes`) y sincronización a disco cada segundo (`appendfsync everysec`). El volumen `redis_data` conserva los streams, los grupos y la configuración de retención al recrear el contenedor. Una interrupción inesperada puede perder las ultimas escrituras que todavia no se hayan sincronizado; esta configuración usa una sola instancia y no proporciona replicación ni alta disponibilidad.

Para comprobar la recuperacion en un entorno local, desde la raíz del proyecto:

```powershell
docker compose up -d redis
docker compose exec redis redis-cli XADD stream:demo-persistencia '*' evento prueba
docker compose up -d --force-recreate redis
docker compose exec redis redis-cli XRANGE stream:demo-persistencia - +
```

El último comando debe mostrar el evento creado antes de recrear el contenedor. La recreación desconecta temporalmente a los servicios que utilizan Redis. No uses `docker compose down -v` si necesitas conservar los datos, porque elimina los volúmenes del proyecto.

Si ya existía un contenedor Redis anterior a esta configuración, el nuevo volumen nombrado puede no contener sus datos anteriores. Antes de recrearlo, respalda o migra los datos que necesites conservar.

### Publicación de eventos con outbox

Al crear una postulación o cambiar su estado, `jobs-service` guarda el cambio y su evento en `jobs_event_outbox` dentro de la misma transacción de PostgreSQL. Un proceso periódico busca eventos pendientes y los publica en el stream correspondiente; si Redis no está disponible, registra el fallo y vuelve a intentarlo. La migración se aplica al iniciar el servicio con Flyway.

El consumidor registra en Redis los identificadores de eventos procesados durante siete días para reducir notificaciones repetidas. Si el servicio cae después de enviar por WebSocket y antes de guardar ese registro, todavía podría repetir la alerta; WebSocket tampoco confirma que el usuario la haya recibido. Los registros publicados permanecen en la tabla de outbox y requieren una política de limpieza antes de acumular un volumen considerable.

### Paso 2: Configurar y Ejecutar el Frontend Web (Next.js)
1. Navega a la carpeta del frontend:
   ```bash
   cd Producto/frontend
   ```
2. Instala las dependencias necesarias de npm:
   ```bash
   npm install
   ```
3. Ejecuta el servidor de desarrollo:
   ```bash
   npm run dev
   ```
* Abre tu navegador e ingresa a `http://localhost:3000` para acceder a la aplicación web.

### Paso 3: Configurar y Compilar la Aplicación Android
1. Abre Android Studio.
2. Selecciona **Open an Existing Project** y navega hasta la carpeta `Producto/android-app`.
3. Sincroniza el proyecto con Gradle.
4. **Configuración de red:**
   * Por defecto, la app móvil está configurada en `app/src/main/java/cl/catchgo/app/data/remote/ApiConfig.kt` para conectarse al entorno de producción en Railway.
   * Si deseas apuntar a tu backend local de Docker:
     * Cambia la constante `HOST` por la dirección IP de tu máquina en la red local (ej. `192.168.1.XX` o `10.0.2.2` si usas el emulador oficial de Android).
     * Desactiva la **Opción A** (API Gateway con HTTPS de producción) y activa la **Opción B** (Conexión Directa por puertos de desarrollo).

---

## 5. Ejecución de Pruebas Unitarias

### Backend (Java)
Para compilar y ejecutar las pruebas de todos los microservicios a través de Maven, navega al directorio del backend o ejecuta en cada servicio:

```bash
mvn clean test
```

### Integración de eventos con PostgreSQL y Redis

Estas pruebas crean contenedores temporales aislados de PostgreSQL y Redis mediante Testcontainers; no utilizan los volúmenes de la aplicación. Requieren que Docker Desktop esté iniciado y que las imágenes estén disponibles o se puedan descargar. Desde `Producto/backend`, ejecuta:

```powershell
mvn -pl services/jobs-service,services/notification-service -am test "-Dtest=OutboxRedisPostgresIntegrationTest,EventDeliveryGuardRedisIntegrationTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

Las pruebas comprueban la migración del outbox, la publicación de los dos eventos, el reintento tras un fallo simulado y la reserva de eventos en Redis. Si Docker no está disponible, se omiten: un resultado omitido no demuestra que la integración haya funcionado.

### Frontend (Next.js)
Las pruebas unitarias y de componentes del frontend se ejecutan con Vitest y React Testing Library. Para iniciarlas:

1. Ve a la carpeta de la interfaz web:
   ```bash
   cd Producto/frontend
   ```
2. Ejecuta el script de pruebas:
   ```bash
   npm run test
   ```

---

## 6. Despliegue en Producción (Railway)

El backend de este proyecto cuenta con la configuración para ser desplegado en Railway a través del archivo `railway.toml`. Para migrar la base de datos y actualizar los entornos en producción, se deben proveer las variables indicadas en el archivo de plantilla [.env.railway.example](file:///c:/Users/xkait/Desktop/Taller/Catch-Go/.env.railway.example) en la sección de Variables de cada servicio dentro de la plataforma Railway.
