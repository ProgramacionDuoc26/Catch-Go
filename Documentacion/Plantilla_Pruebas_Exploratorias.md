# Plantilla de Sesión de Pruebas Exploratorias (SBTM)
## Proyecto: Catch & Go

Esta plantilla sigue el estándar de **Gestión de Pruebas Basadas en Sesiones (SBTM)**. Utiliza este documento para registrar las sesiones de pruebas exploratorias manuales de la aplicación.

---

## 📋 Información General de la Sesión

| Campo | Detalle |
| :--- | :--- |
| **ID de la Sesión** | `EXP-XXX` (Ej. EXP-001) |
| **Explorador / Tester** | [Nombre del Tester] |
| **Fecha de Ejecución** | [DD/MM/AAAA] |
| **Duración del Timebox** | [Ej: 60 minutos / 90 minutos] |
| **Entorno de Pruebas** | [Ej: Local (Docker) / Staging / Producción (Railway)] |
| **Usuario(s) Utilizado(s)** | [Ej: empresa_test@example.com (Empresa) / trabajador_test@example.com (Trabajador)] |

---

## 🎯 Misión de la Sesión (Charter)

> **Misión:** [Define claramente el objetivo de la sesión. Ej: *“Explorar el flujo de registro e inicio de sesión de un nuevo trabajador, intentando saltarse pasos obligatorios y enviando datos maliciosos en los inputs para validar la validación en el cliente y servidor.”*]

### Áreas de Enfoque:
* [ ] Interfaz de Usuario (UI / UX / Diseño Responsivo)
* [ ] Validaciones de Formularios (Campos requeridos, tipos de datos, límites)
* [ ] Seguridad (Inyección de scripts, acceso no autorizado a rutas privadas)
* [ ] Integración del API Gateway y Microservicios (Errores de red, respuestas HTTP)

---

## 📝 Bitácora de Ejecución (Notas de Pruebas)

*Escribe de manera cronológica los pasos que fuiste realizando, qué observaste y qué comportamientos llamaron tu atención.*

1. **[00:00 - Inicio de Sesión]** Inicié el navegador en modo incógnito e ingresé al enlace del login en Next.js.
2. **[00:10 - Pruebas de Formulario]** Intenté hacer submit del login con campos vacíos. El cliente muestra mensajes de error interactivos.
3. **[00:25 - Intentos de bypass]** Intenté acceder a la ruta `/admin` directamente escribiendo la URL sin estar autenticado. El middleware de Next.js redirigió correctamente al login.
4. **[00:40 - Datos Extremos]** ...

---

## 🐛 Hallazgos y Defectos Detectados

*Registra aquí cualquier comportamiento inusual, bug, o vulnerabilidad de seguridad encontrada durante la exploración.*

### 1. [Título Breve del Defecto 1]
* **Severidad:** [🔴 Alta / 🟡 Media / 🔵 Baja]
* **Descripción:** [Qué sucedió y por qué es un comportamiento incorrecto.]
* **Pasos para Reproducir:**
  1. Ir a la sección `[Nombre]`
  2. Hacer clic en `[Botón]`
  3. Ingresar el valor `[Valor]`
  4. Observar el resultado obtenido vs el esperado.
* **Captura de Pantalla/Evidencia:** [Enlace a imagen o log del microservicio]

### 2. [Título Breve del Defecto 2]
* **Severidad:** [🔴 Alta / 🟡 Media / 🔵 Baja]
* ...

---

## 🏁 Conclusión de la Sesión

* **Estado de la Misión:** [🟢 Completada con éxito / 🟡 Incompleta (requiere más tiempo) / 🔴 Bloqueada (un error crítico impidió continuar)]
* **Resumen Cualitativo:** [Resumen rápido de la salud del módulo explorado. Ej: *"El módulo de login es bastante robusto y las redirecciones funcionan bien. Se requiere poner atención al manejo de sesiones cuando expira el token JWT."*]
* **Recomendación para próximas sesiones:** [Sugerencia de qué explorar a continuación basándose en lo aprendido.]
