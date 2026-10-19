# INFORME EJECUTIVO Y GUÍA INTEGRAL DE EVALUACIÓN
## Arquitectura de Mensajería y Comunicación entre Microservicios: Catch&Go

---

## 1. RESUMEN EJECUTIVO Y CASOS DE NEGOCIO

En la plataforma **Catch&Go** (sistema de reclutamiento y asignación inmediata de empleos temporales y de alta rotación), la interacción entre microservicios se diseñó siguiendo un patrón híbrido que combina **comunicación síncrona** y **comunicación asíncrona orientada a eventos**, optimizando el rendimiento, la experiencia de usuario y la resiliencia del sistema.

```
                      ┌────────────────────────────────────────┐
                      │          CATCH & GO ARQUITECTURA       │
                      └────────────────────────────────────────┘
                                           │
         ┌─────────────────────────────────┴─────────────────────────────────┐
         ▼                                                                   ▼
┌─────────────────────────────────┐                         ┌─────────────────────────────────┐
│     COMUNICACIÓN SÍNCRONA       │                         │     COMUNICACIÓN ASÍNCRONA      │
│  (Spring Cloud OpenFeign / HTTP)│                         │  (Redis Streams / Event-Driven) │
├─────────────────────────────────┤                         ├─────────────────────────────────┤
│ • Consulta de perfil y scoring  │                         │ • Publicación de empleo         │
│ • Bloqueante (espera respuesta) │                         │ • No bloqueante (desacoplado)   │
│ • Dependencia inmediata de datos│                         │ • Alta concurrencia y fondo     │
└─────────────────────────────────┘                         └─────────────────────────────────┘
```

---

### CASO DE NEGOCIO 1: Comunicación Síncrona (HTTP / OpenFeign)
* **Escenario:** Evaluación y cálculo de compatibilidad inmediata de un postulante a un empleo.
* **Microservicios involucrados:** `matching-service` (cliente) $\rightarrow$ `profile-service` (servidor).
* **¿Por qué es Síncrono?**
  Para que el algoritmo de matching pueda evaluar si un candidato es apto para un turno urgente, requiere en ese instante exacto los datos actualizados del perfil (experiencia, certificaciones, estado de validación y disponibilidad). 
  Si esta consulta fuera asíncrona, el usuario tendría que quedarse esperando sin saber si su postulación fue aceptada o rechazada en el momento en que presiona el botón.
* **Analogía de negocio:** Es equivalente al pago con tarjeta en una pasarela o a la validación de un código OTP de seguridad: la operación se detiene una fracción de segundo para esperar un "Aprobado" o "Rechazado" antes de permitir el siguiente paso.

---

### CASO DE NEGOCIO 2: Comunicación Asíncrona (Redis Streams)
* **Escenario:** Publicación de una nueva oferta de trabajo por parte de una empresa empleadora.
* **Microservicios involucrados:** `jobs-service` (productor) $\rightarrow$ **Redis Streams** $\rightarrow$ `matching-service` y `notification-service` (consumidores independientes).
* **¿Por qué es Asíncrono?**
  1. **Respuesta Inmediata al Usuario:** La empresa que publica la oferta no debe esperar a que el sistema busque compatibilidad entre 5.000 trabajadores ni a que se envíen 200 correos electrónicos. La oferta se guarda en base de datos, se emite el evento `job.created` y la empresa recibe un `201 Created` en menos de **40 milisegundos**.
  2. **Tolerancia a Fallos y Desacoplamiento:** Si el servidor de correos o el servicio de notificaciones sufre una caída temporal o se satura, la publicación del empleo **NO se cancela ni se traba**. El evento queda resguardado de forma persistente en Redis hasta que el consumidor se recupere.
  3. **Escalabilidad Independiente:** `matching-service` y `notification-service` procesan el evento a su propio ritmo sin interferir entre sí.

---

## 2. GLOSARIO DE CONCEPTOS CLAVE (EN MODO SIMPLE)

| Término | Definición Técnica | Explicación Simple (Para responder en examen) |
| :--- | :--- | :--- |
| **Comunicación Síncrona** | Modelo solicitud-respuesta donde el emisor bloquea el hilo de ejecución hasta recibir respuesta del receptor. | "Es como una llamada telefónica: hablas y te quedas esperando en línea a que la otra persona te conteste para poder continuar." |
| **Comunicación Asíncrona** | Modelo desacoplado en el tiempo donde el emisor publica un mensaje y continúa su trabajo sin esperar al receptor. | "Es como enviar un correo o mensaje de WhatsApp: lo envías, sigues con tu día y la otra parte lo lee y lo procesa cuando esté disponible." |
| **Message Broker / Stream (Redis)** | Plataforma intermediaria de mensajería que recibe, almacena de forma ordenada y distribuye eventos entre servicios. | "Es la oficina de correos central: los servicios dejan paquetes ahí y los destinatarios pasan a recogerlos en orden." |
| **Consumer Group (Grupo de Consumidores)** | Agrupación lógica de consumidores que permite repartir el trabajo o asegurar que cada servicio reciba su copia del evento. | "Es como un equipo de trabajo: si hay 100 mensajes, entre los miembros del equipo se los reparten para terminar más rápido sin duplicar tareas." |
| **ACK (Acknowledgment)** | Señal explícita que envía el consumidor al broker indicando que un mensaje fue procesado exitosamente. | "Es la firma de recibido cuando te entregan una encomienda: confirma que llegó bien y ya se puede archivar." |
| **DLQ (Dead Letter Queue)** | Cola especial de desvío donde van a parar los mensajes que fallaron tras repetidos reintentos de procesamiento. | "Es el cajón de cartas devueltas o con dirección rota: evita que un mensaje fallido tranque toda la fila, guardándolo para revisarlo luego." |
| **OpenFeign** | Cliente HTTP declarativo de Spring Cloud que simplifica el consumo de APIs REST entre microservicios. | "Es un conector inteligente que convierte una simple interfaz Java en una petición HTTP completa hacia otro microservicio." |
| **API Gateway** | Punto de entrada único que centraliza la autenticación, seguridad, balanceo y enrutamiento hacia los microservicios. | "Es el guardia de recepción del edificio: revisa quién eres y te indica exactamente a qué oficina debes dirigirte." |

---

## 3. GUÍA PARA EXPLICAR EL DIAGRAMA EJECUTIVO

Al exponer la lámina del **Diagrama Ejecutivo de Mensajería** (`Diagrama_Ejecutivo_Mensajeria_CatchAndGo.png`), sigue este guión estructurado de 4 pasos:

```
[Cliente Web / Móvil]
         │
         ▼
[API Gateway :8080]
   ├── (Ruta 1) ──► [Jobs Service] ─────(Asíncrono)─────► [Redis Streams: jobs-stream]
   │                                                                 │
   │                                               ┌─────────────────┴─────────────────┐
   │                                               ▼                                   ▼
   │                                      [Matching Service]                [Notification Service]
   │                                               │                                   │
   │                                          (Síncrono)                            (Email/Push)
   │                                               ▼                                   ▼
   └── (Ruta 2) ─────────────────────────► [Profile Service]                     [Usuario Final]
```

### Guión de Explicación (Paso a Paso):

1. **Paso 1: Punto de Entrada Centralizado (API Gateway):**
   > *"Nuestra arquitectura expone un único punto de acceso seguro a través del API Gateway (puerto 8080). Ningún cliente externo accede directamente a las bases de datos ni a los microservicios internos, garantizando seguridad y trazabilidad."*

2. **Paso 2: La Vía Asíncrona (Publicación de Empleo):**
   > *"Cuando un empleador publica una oferta laboral a través de `jobs-service`, el empleo se persiste en PostgreSQL y de inmediato se publica un evento en `Redis Streams` en el canal `jobs-stream`. El empleador recibe respuesta inmediata (`201 Created`), liberando la conexión en milisegundos."*

3. **Paso 3: Consumo Independiente y Paralelo:**
   > *"En segundo plano, dos servicios leen este mismo evento de forma paralela mediante Consumer Groups:*
   > * *`matching-service`: Recibe el evento, analiza los requisitos del trabajo y busca candidatos.*
   > * *`notification-service`: Prepara y despacha correos y notificaciones móviles a los trabajadores disponibles.*
   > *Si el servicio de notificaciones tarda o se reinicia, `matching-service` no se ve afectado en lo absoluto."*

4. **Paso 4: La Vía Síncrona (Matching a Perfil vía OpenFeign):**
   > *"Por otro lado, cuando `matching-service` necesita conocer los detalles finos de un candidato (habilidades, disponibilidad y puntuación), realiza una llamada HTTP directa y síncrona hacia `profile-service` usando Spring Cloud OpenFeign. Esto es síncrono porque el algoritmo necesita ese dato en el instante para ponderar el resultado."*

---

## 4. GUÍA PRÁCTICA DE PRUEBAS (CURL & POSTMAN)

> **Nota de Entornos:**
> - **En Producción (Railway):** Reemplazar `{{baseUrl}}` por la URL del Gateway:  
>   `https://catchandgo-api-gateway-production.up.railway.app`
> - **En Local:** Usar `http://localhost:8080`.

---

### PRUEBA 1: Flujo Síncrono (Matching consultando Perfil vía Feign)

* **Objetivo:** Demostrar que `matching-service` invoca a `profile-service`, espera su respuesta y retorna el perfil evaluado.
* **Comando cURL:**
```bash
curl -X GET "https://catchandgo-api-gateway-production.up.railway.app/api/v1/matching/candidates/1/evaluate" \
     -H "Accept: application/json"
```
* **Respuesta Esperada (`200 OK`):**
```json
{
  "candidateId": 1,
  "fullName": "Juan Pérez",
  "status": "ACTIVE",
  "qualificationScore": 95,
  "isEligible": true,
  "evaluatedAt": "2026-10-19T10:05:00Z"
}
```

---

### PRUEBA 2: Flujo Asíncrono (Publicación de Empleo y emisión de Evento)

* **Objetivo:** Demostrar que `jobs-service` guarda el empleo, emite el evento a Redis Streams y responde de inmediato al cliente.
* **Comando cURL:**
```bash
curl -X POST "https://catchandgo-api-gateway-production.up.railway.app/api/v1/jobs" \
     -H "Content-Type: application/json" \
     -d '{
       "title": "Reponedor Nocturno",
       "description": "Turno de reposición en supermercado",
       "category": "LOGISTICA",
       "hourlyRate": 6500.0,
       "vacancies": 3,
       "employerId": 10
     }'
```
* **Respuesta Esperada (`201 Created` en < 50ms):**
```json
{
  "id": 101,
  "title": "Reponedor Nocturno",
  "status": "OPEN",
  "createdAt": "2026-10-19T10:05:30Z",
  "eventDispatched": true
}
```

---

### PRUEBA 3: Auditoría y Resiliencia DLQ (Mensajería Segura)

* **Objetivo:** Demostrar al evaluador que el sistema cuenta con control de errores para mensajes fallidos.
* **Consultar mensajes en DLQ (cURL):**
```bash
curl -X GET "https://catchandgo-api-gateway-production.up.railway.app/api/v1/admin/dlq" \
     -H "Accept: application/json"
```
* **Reintentar un mensaje fallido (cURL):**
```bash
curl -X POST "https://catchandgo-api-gateway-production.up.railway.app/api/v1/admin/dlq/1729332000000-0/retry"
```

---

### INSTRUCTIVO PARA EJECUTAR EN POSTMAN

En la raíz del repositorio se encuentra el archivo listo:  
[`Documentacion/Postman_Collection_CatchAndGo_Mensajeria.json`](file:///c:/Users/xkait/Desktop/Catch_Go/Catch-Go/Documentacion/Postman_Collection_CatchAndGo_Mensajeria.json)

1. **Abrir Postman** y hacer clic en **Import** (esquina superior izquierda).
2. Seleccionar el archivo `Postman_Collection_CatchAndGo_Mensajeria.json`.
3. Verás una colección llamada **"CatchAndGo - Mensajeria y Microservicios"** con 3 carpetas:
   - `1. Sincrono - Matching a Profile`
   - `2. Asincrono - Jobs a Redis Streams`
   - `3. Resiliencia - Dead Letter Queue (DLQ)`
4. **Configurar Variable de Entorno:**
   - La colección ya trae la variable `{{baseUrl}}`.
   - Haz clic en la colección $\rightarrow$ pestaña **Variables** $\rightarrow$ verifica que `baseUrl` apunte a:
     `https://catchandgo-api-gateway-production.up.railway.app` (o `http://localhost:8080`).
5. **Ejecutar:** Abre cada petición y presiona **Send**. Verás los códigos de estado `200 OK` y `201 Created` con sus tiempos de respuesta.

---

## 5. CÓMO REVISAR Y DEMOSTRAR LOS LOGS EN VIVO

Para evidenciar ante el docente que la mensajería asíncrona está operando en tiempo real, abre los logs en Railway (o en la terminal de cada servicio):

### 1. En `jobs-service` (Emisor del Evento):
Al hacer el `POST /api/v1/jobs`, verás en los logs:
```text
INFO  [jobs-service] c.c.jobs.service.JobService : Job created successfully with ID: 101
INFO  [jobs-service] c.c.jobs.messaging.JobEventProducer : [STREAM PRODUCER] Emitted event [job.created] to stream [jobs-stream] with Redis Message ID: 1729332330125-0
```

### 2. En `matching-service` (Consumidor 1):
Milisegundos después, sin intervención del usuario:
```text
INFO  [matching-service] c.c.matching.consumer.JobCreatedConsumer : [STREAM CONSUMER] Received event [job.created] for Job ID: 101
INFO  [matching-service] c.c.matching.service.MatchingService : Running matching algorithm for job requirements...
INFO  [matching-service] c.c.matching.consumer.JobCreatedConsumer : [ACK] Message 1729332330125-0 acknowledged successfully.
```

### 3. En `notification-service` (Consumidor 2):
En paralelo:
```text
INFO  [notification-service] c.c.notification.consumer.NotificationConsumer : [STREAM CONSUMER] Processing job alert for category: LOGISTICA
INFO  [notification-service] c.c.notification.service.EmailService : Queued push notifications to 15 workers.
INFO  [notification-service] c.c.notification.consumer.NotificationConsumer : [ACK] Message 1729332330125-0 acknowledged successfully.
```

---

## 6. RONDA DE PREGUNTAS Y RESPUESTAS DEL PROFESOR / EVALUADOR

A continuación, las preguntas más habituales y complejas que suele formular una comisión evaluadora, junto a la respuesta óptima recomendada:

---

### Pregunta 1: *"¿Por qué eligieron Redis Streams y no RabbitMQ o Apache Kafka?"*
* **Respuesta clave:**
  > *"Elegimos **Redis Streams** por tres razones arquitectónicas:*
  > 1. *Ya utilizábamos Redis en el ecosistema para gestión de sesiones y caché distribuida, por lo que evitamos añadir un componente de infraestructura pesado adicional como Kafka (que requiere Zookeeper/KRaft y alta memoria).*
  > 2. *Redis Streams ofrece nativamente soporte para **Consumer Groups**, **persistencia en disco (AOF/RDB)** y **confirmaciones de lectura (ACK)**, que cubren perfectamente el volumen y la necesidad de resiliencia de Catch&Go con una latencia inferior al milisegundo.*
  > 3. *Si en el futuro la plataforma escala a millones de eventos por segundo, el desacoplamiento de interfaces nos permite migrar a Kafka sin alterar la lógica de negocio de los microservicios."*

---

### Pregunta 2: *"¿Qué sucede si el servicio de notificaciones está apagado o colapsa al momento de publicar un empleo?"*
* **Respuesta clave:**
  > *"No ocurre ninguna pérdida de datos ni falla en la publicación:*
  > 1. *El empleador recibe su respuesta exitosa inmediatamente (`201 Created`).*
  > 2. *El mensaje queda almacenado de manera persistente en el stream de Redis.*
  > 3. *El servicio de notificaciones maneja un puntero de lectura dentro de su Consumer Group. Cuando vuelva a levantarse, leerá los mensajes pendientes a partir del último ID confirmado (ACK) y se pondrá al día automáticamente."*

---

### Pregunta 3: *"¿Por qué la consulta de perfil en Matching se hizo síncrona y no asíncrona por eventos?"*
* **Respuesta clave:**
  > *"Porque es una operación de consulta con **dependencia causal inmediata**. Si un usuario está intentando tomar un turno urgente, el algoritmo necesita calcular en ese preciso instante el puntaje de aptitud con los datos actuales de su perfil. Si lo hiciéramos asíncrono, la interfaz quedaría en un estado intermedio indefinido esperando un evento de respuesta, lo cual degradaría la experiencia del usuario."*

---

### Pregunta 4: *"¿Cómo previenen que un mensaje se procese dos veces si hay un fallo de red (Idempotencia)?"*
* **Respuesta clave:**
  > *"Implementamos control de **idempotencia**. Cada evento transporta un identificador único de mensaje (`eventId` o `messageId`). Antes de procesar la lógica de negocio, el consumidor verifica si ese `eventId` ya fue registrado en la base de datos o en una llave de Redis. Si ya existe, se confirma (ACK) inmediatamente y se descarta el duplicado, evitando dobles cobros o dobles notificaciones."*

---

### Pregunta 5: *"¿Qué es un ACK y qué ocurre si un consumidor nunca lo envía?"*
* **Respuesta clave:**
  > *"El **ACK (Acknowledgment)** es la confirmación explícita que el consumidor le da a Redis indicando que terminó de procesar el evento. Si el consumidor muere a mitad del procesamiento o lanza un error fatal antes del ACK, el mensaje permanece en la lista de pendientes (**PEL - Pending Entries List**). Luego, un mecanismo de recuperación (`XAUTOCLAIM` o un retry worker) detecta el mensaje huérfano y se lo reasigna a otro consumidor para no perderlo."*

---

### Pregunta 6: *"¿Para qué sirve la Dead Letter Queue (DLQ) que implementaron?"*
* **Respuesta clave:**
  > *"La **DLQ (Cola de Mensajes Muertos)** protege la disponibilidad del sistema. Si un mensaje viene corrupto (por ejemplo, con un JSON malformado o un ID inexistente) y falla reiteradamente (más de 3 intentos), intentar procesarlo infinitamente provocaría lo que se conoce como un **Poison Message**, bloqueando la cola de procesamiento. La DLQ desvía ese mensaje tóxico a un canal aislado y permite que el flujo continúe normalmente, permitiendo además al administrador auditar y reintentar el mensaje corregido mediante la API de administración."*

---

### Pregunta 7: *"¿Cómo escala esta arquitectura si la plataforma crece exponencialmente?"*
* **Respuesta clave:**
  > *"Nuestra arquitectura escala horizontalmente de manera transparente:*
  > 1. *En la parte síncrona, el API Gateway balancea la carga entre múltiples réplicas de los microservicios.*
  > 2. *En la parte asíncrona, los **Consumer Groups** de Redis Streams permiten levantar 5 o 10 instancias de `matching-service`, y Redis repartirá los eventos de la cola de forma equitativa entre las instancias sin procesar mensajes duplicados."*
