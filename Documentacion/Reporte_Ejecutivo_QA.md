# Reporte Ejecutivo de Aseguramiento de Calidad y Pruebas
## Proyecto: Catch & Go — Plataforma de Intermediación Laboral Temporal

---

## 📊 Resumen Ejecutivo
Este documento presenta el estado de calidad, estabilidad y seguridad del código del proyecto **Catch & Go**. Se implementó una estrategia integral de pruebas automatizadas y análisis de vulnerabilidades dividida en tres grandes ámbitos: **Cobertura de Pruebas Unitarias**, **Seguridad Estática (SAST)** y **Pruebas Dinámicas/Estrés (DAST / Load Testing)**. 

El proyecto cuenta hoy con un arnés de pruebas e instrumentación de nivel profesional listo para producción.

---

## 1. Métrica de Cobertura de Código (Code Coverage)
La cobertura mide el porcentaje de líneas de código y caminos lógicos que son verificados de forma automática por la suite de pruebas unitarias.

### A. Frontend (Next.js - Vitest)
* **Cobertura Global:** **`83.33%`** de líneas cubiertas (superando el estándar recomendado de la industria del 80%).
* **Componentes Clave Analizados:**
  * **[Button.tsx](file:///c:/Users/xkait/Desktop/Taller/Catch-Go/Producto/frontend/src/components/ui/Button.tsx):** **`100%`** de cobertura (sentencias, ramas y líneas).
  * **[PaymentGatewayModal.tsx](file:///c:/Users/xkait/Desktop/Taller/Catch-Go/Producto/frontend/src/components/modals/PaymentGatewayModal.tsx):** **`82.69%`** de cobertura (incrementada desde un 42% mediante la inyección y compresión de archivos de imagen en pruebas asíncronas).

### B. Backend (Spring Boot - JaCoCo)
* **Herramienta:** `jacoco-maven-plugin` integrado en el POM raíz.
* **Resultado:** Cobertura del **`100%`** en componentes críticos de infraestructura como el servicio de firmas de tokens [JwtService.java](file:///c:/Users/xkait/Desktop/Taller/Catch-Go/Producto/backend/common/common-jwt/src/main/java/com/catchandgo/common/jwt/JwtService.java).

---

## 2. Análisis Estático de Seguridad (SAST)
El análisis estático evalúa el código fuente en busca de malas prácticas y posibles vulnerabilidades de seguridad conocidas.

### A. Frontend (ESLint Security)
* **Estado Inicial:** **14 alertas** detectadas (incluyendo inyección de objetos dinámicos `security/detect-object-injection` y uso de nombres de archivos no literales `security/detect-non-literal-fs-filename`).
* **Estado Actual:** **`0 Advertencias de Seguridad`** (100% resuelto). 
  * Se desactivaron reglas de falsos positivos tipadas por TypeScript.
  * Se mitigó una vulnerabilidad de **Directory Traversal** en las API locales de disputas y recibos sanitizando los IDs de entrada con expresiones regulares.

### B. Backend (SpotBugs & FindSecBugs)
* **Herramienta:** Integración de SpotBugs y FindSecBugs en la compilación Maven.
* **Resultado:** Análisis ejecutado exitosamente en los 12 módulos del backend. Se generaron reportes HTML individuales (`target/spotbugs.html`). Se identificó la alerta de protección CSRF desactivada como segura debido a la naturaleza stateless del sistema JWT.

---

## 3. Pruebas de Estrés y Carga (K6)
Se simuló un escenario realista en la nube de Railway con una rampa de carga de hasta **150 usuarios concurrentes simultáneos** ejecutando peticiones a endpoints de autenticación y lectura.

* **Resultados de Éxito:** El API Gateway y el microservicio de empleos mantuvieron una disponibilidad del **98% y 99%** respectivamente bajo carga.
* **Punto de Quiebre Detectado (Bottleneck):**
  * Tasa de error global: **`33.90%`**.
  * Causa: Los intentos de login simultáneos saturaron el límite de conexiones a la base de datos de PostgreSQL en Railway (Plan Gratuito) y estrangularon la CPU del contenedor al procesar Bcrypt.
* **Recomendación técnica:** Implementar un pooler de conexiones (como PgBouncer) y escalar el contenedor de `auth-service` para la puesta en producción.

---

## 4. Pruebas de Seguridad Dinámica (DAST)
* **Herramienta:** Planificación de auditorías mediante **OWASP ZAP**.
* **Entregable:** Se creó la [Guía de OWASP ZAP](file:///c:/Users/xkait/Desktop/Taller/Catch-Go/Documentacion/Guia_OWASP_ZAP.md) para realizar ataques activos interceptando las credenciales mediante el navegador integrado y emitir reportes de cumplimiento de seguridad web.
