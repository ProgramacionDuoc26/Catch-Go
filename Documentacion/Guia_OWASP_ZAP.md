# Guía de Pruebas de Seguridad Dinámica (DAST) con OWASP ZAP
## Catch & Go — Plataforma de Intermediación Laboral Temporal

Esta guía detalla el procedimiento para realizar análisis dinámicos de seguridad (DAST) sobre los endpoints del backend de Catch & Go desplegados en Railway, utilizando la herramienta de código abierto **OWASP ZAP (Zed Attack Proxy)**.

---

## 1. ¿Qué es OWASP ZAP y por qué lo usamos?
OWASP ZAP es un escáner de seguridad dinámico. A diferencia de las herramientas SAST (que leen el código fuente), ZAP interactúa con la aplicación en tiempo de ejecución. 

Funciona realizando peticiones HTTP/S reales y analizando las respuestas del servidor para detectar fallos como:
* Inyecciones SQL o de scripts (XSS).
* Errores de configuración del servidor (cabeceras de seguridad ausentes).
* Exposición indebida de datos sensibles o stacktraces de bases de datos.
* Vulnerabilidades en el manejo de sesiones (cookies sin directivas de seguridad).

---

## 2. Instalación de OWASP ZAP en Windows
Dado que la base de datos y los microservicios están en la nube (Railway), utilizaremos la versión de escritorio de ZAP.

1. Descarga el instalador oficial para Windows de 64 bits desde la página oficial:
   👉 [https://www.zaproxy.org/download/](https://www.zaproxy.org/download/)
2. Ejecuta el instalador `.msi` descargado y sigue los pasos del asistente (selecciona la instalación estándar).
3. Una vez instalado, abre **ZAP** desde el menú de inicio de Windows.

---

## 3. Método 1: Escaneo Automatizado Rápido (Quick Start)
Esta es la manera más sencilla de realizar un escaneo inicial pasivo y activo sobre los endpoints públicos de tu API Gateway.

```
                  +-----------------------------------------+
                  |         OWASP ZAP (Quick Start)         |
                  +-----------------------------------------+
                                       |
                                       | (Ataque automatizado)
                                       v
                  +-----------------------------------------+
                  |      API Gateway (Railway Cloud)        |
                  +-----------------------------------------+
```

### Pasos para ejecutarlo:
1. Al abrir ZAP, selecciona la opción *"No, I do not want to persist this session right now"* (a menos que desees guardar las pruebas para el futuro) y presiona **Start**.
2. En la pestaña central **Quick Start**, haz clic en el botón grande **Automated Scan**.
3. En el campo **URL to attack**, ingresa la URL de tu **API Gateway de Railway** (por ejemplo, `https://api-gateway2-catch-go.up.railway.app`).
4. En **Traditional Spider**, déjala marcada (esto rastreará todas las URLs que el Gateway exponga públicamente).
5. Haz clic en el botón **Attack**.
6. **¿Qué sucederá?:** 
   * ZAP iniciará el rastreo (Spidering) para descubrir las rutas existentes.
   * Luego, ejecutará un **Active Scan** inyectando payloads sobre esas rutas buscando fallas de inyección, fugas de cabeceras, etc.
   * Puedes ver el progreso en la parte inferior en la pestaña **Active Scan**.

---

## 4. Método 2: Escaneo Interceptando el Navegador (Manual Explorer)
El escaneo rápido no puede loguearse ni realizar flujos complejos (como publicar un empleo). Para escanear flujos protegidos detrás de autenticación, usamos el navegador integrado de ZAP.

```
  +------------------+      HTTP/S      +------------------+      HTTP/S      +---------------+
  | Navegador de ZAP | ===============> |   ZAP (Proxy)    | ===============> |  API Gateway  |
  |  (Login/Flujo)   |                  | (Registra/Ataca) |                  |   (Railway)   |
  +------------------+                  +------------------+                  +---------------+
```

### Pasos para ejecutarlo:
1. En la pestaña **Quick Start** de ZAP, haz clic en **Manual Explore**.
2. En el campo **URL to explore**, introduce la URL de tu **Frontend en Next.js** de Railway (por ejemplo, `https://frontend-catch-go.up.railway.app`).
3. Deja marcada la opción **Launch Browser** y selecciona un navegador (por ejemplo, *Firefox* o *Chrome*).
4. Haz clic en **Launch Browser**. Se abrirá una ventana de navegador especial controlada por ZAP.
5. **Realiza el flujo en tu aplicación:**
   * Inicia sesión con un usuario de prueba en Catch & Go.
   * Publica una oferta de contingencia laboral.
   * Realiza una postulación.
6. **Ejecutar el escaneo dinámico en ZAP:**
   * Regresa a la ventana principal de OWASP ZAP.
   * Verás en la pestaña izquierda **Sites** cómo se ha construido el árbol de todas las peticiones capturadas.
   * Busca tu API Gateway en la lista, haz clic derecho sobre él y selecciona:
     👉 **Attack** ➔ **Active Scan...**
   * Presiona **Start Scan** en la ventana emergente. ZAP atacará únicamente los endpoints y parámetros con los que interactuaste en tu sesión de navegador, garantizando que el escaneo pase por la autenticación.

---

## 5. Interpretación de Resultados e Informe de Vulnerabilidades
Cuando finalice el escaneo, la pestaña más importante es **Alerts** (Alertas), ubicada en la esquina inferior izquierda.

### Clasificación de Alertas por Criticidad:
* 🔴 **High (Alta):** Vulnerabilidades críticas que deben resolverse de inmediato (ej. Inyecciones SQL, Cross-Site Scripting - XSS, vulnerabilidad de Remote Code Execution).
* 🟡 **Medium (Media):** Fallas importantes como exposición de logs de base de datos, páginas de administración expuestas, CSRF o falta de cifrado seguro.
* 🔵 **Low (Baja):** Problemas menores de configuración, como la falta de directivas de seguridad en cabeceras o cookies que no tienen la bandera `HttpOnly`.

### Generar Reporte para Presentación Académica o Taller:
1. En el menú superior de ZAP, haz clic en **Report** ➔ **Generate Report**.
2. Selecciona una plantilla (se recomienda **Modern HTML Report**).
3. Configura el nombre del informe (por ejemplo, `Reporte_DAST_CatchGo.html`) y la carpeta de destino.
4. Haz clic en **Generate Report**.
5. **¿Qué obtendrás?:** Un informe visual y estructurado que detalla cada falla encontrada, la petición HTTP que la causó, y recomendaciones de mitigación listas para ser presentadas.
