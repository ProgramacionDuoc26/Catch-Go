# F2 — Capa de red + arquitectura

**Estado:** entregado
**Branch:** `feature/rama-colores-paleta` (continúa sobre F1)
**Objetivo:** dejar la plumbing de red, DI y manejo de errores listos para que F3 (Login) y todas las features posteriores consuman el `api-gateway` Spring Boot sin reinventar nada.

---

## Lo que incluye

### Dependencias y plugins (`gradle/libs.versions.toml` + `build.gradle.kts`)

| Capa | Librería | Versión |
|---|---|---|
| DI | Hilt + KSP | `2.52` / `2.0.21-1.0.27` |
| Serialización | kotlinx-serialization | `1.7.3` |
| HTTP | Retrofit + retrofit2-kotlinx-serialization-converter | `2.11.0` / `1.0.0` |
| HTTP | OkHttp + logging-interceptor | `4.12.0` |
| Persistencia | androidx.datastore-preferences | `1.1.1` |
| Coroutines | kotlinx-coroutines-android | `1.9.0` |
| ViewModel | androidx.lifecycle-viewmodel-compose | `2.10.0` |

KSP elegido sobre KAPT por velocidad de build con Hilt.

### Application + Manifest

- `CatchGoApp` con `@HiltAndroidApp` registrado en el manifest.
- `MainActivity` ahora con `@AndroidEntryPoint`.
- Permiso `INTERNET` declarado.
- `network_security_config.xml` permite cleartext **solo** para `10.0.2.2` y `localhost` (necesario para el backend HTTP en dev). En producción se servirá HTTPS.

### Errores · `core/error/`

- `ApiError`: sealed class con casos cubiertos (`NoConnection`, `Timeout`, `Unauthorized`, `Forbidden`, `NotFound`, `Conflict`, `Validation`, `Server`, `Unknown`).
- `ErrorMapper`: traduce `HttpException`, `SocketTimeoutException`, `UnknownHostException` y `IOException` al `ApiError` correspondiente. Parsea el body de error tipo Spring (`ErrorResponse`) para extraer mensaje y campo `error`. Los servicios `common-exceptions` del backend devuelven un shape compatible.

### UiState · `core/ui/UiState.kt`

Sealed interface con `Idle`, `Loading`, `Success<T>`, `Error(ApiError)`. Toda pantalla con I/O exponen un `StateFlow<UiState<X>>` desde su ViewModel.

### Token de sesión · `data/local/AuthTokenStore.kt`

Wrapper de DataStore Preferences sobre la key `jwt_token`:
- `token: Flow<String?>` para observar.
- `save(token)` / `clear()` suspendidas.

### Networking · `data/remote/`

- `ApiConfig.BASE_URL = "http://10.0.2.2:8080/"` — apunta al `api-gateway` desde el emulador.
- `AuthInterceptor`: lee el token del `AuthTokenStore` (vía `runBlocking`/`first()`) y lo agrega como `Authorization: Bearer ...` cuando existe.
- `HealthApi`: endpoint `GET /actuator/health` para verificar el wiring end-to-end sin depender de auth.
- `ErrorResponse`: DTO del error estándar de Spring Boot (`timestamp`, `status`, `error`, `message`, `path`).

### DI · `di/NetworkModule.kt`

Provee:
- `Json` con `ignoreUnknownKeys = true`, `coerceInputValues = true`, `explicitNulls = false`.
- `HttpLoggingInterceptor` con nivel `BODY` (suficiente para dev; en release se silenciará en F-final).
- `OkHttpClient` con timeouts 15/20/20 s y los dos interceptores enchufados.
- `Retrofit` apuntado al gateway.
- `HealthApi` como ejemplo de servicio.

> **Storage module no es necesario:** `AuthTokenStore` es una clase con `@Inject constructor` y `@Singleton`, Hilt la resuelve sola.

---

## Decisiones documentadas

### Por qué KSP en vez de KAPT
KAPT está deprecado en favor de KSP para Hilt desde Hilt 2.48. KSP procesa anotaciones nativamente en el compilador de Kotlin → builds 30–40% más rápidos. No hay razón para arrastrar KAPT en un proyecto nuevo.

### Por qué `runBlocking` en `AuthInterceptor`
OkHttp interceptors son síncronos por contrato. La lectura del token desde DataStore es suspendida; `runBlocking { token.first() }` es la salida estándar y aceptable porque (a) el primer valor del Flow se entrega de inmediato si ya hay datos en memoria, y (b) la latencia de un read de DataStore es despreciable comparada con el roundtrip de red. Alternativa con cache en memoria queda para F-final si hace falta optimizar.

### Por qué `network_security_config` y no `usesCleartextTraffic` global
`usesCleartextTraffic="true"` global es un footgun: deja entrar HTTP a cualquier dominio. Limitándolo a `10.0.2.2` y `localhost` la app falla cerrada si por error apunta a otro host HTTP — defensa en profundidad gratis.

### Por qué `ApiError` sealed y no `Throwable` genérico
Las pantallas necesitan **discriminar** errores para mostrar mensajes apropiados (un 401 lleva al login, un 500 muestra retry, un timeout muestra otro retry). Un sealed class lo hace `when`-exhaustivo en Kotlin y nos da mensajes en español ya escritos para el usuario.

---

## Cómo verificar

1. Sync Gradle (puede tardar la primera vez por la descarga de Hilt + KSP).
2. Confirmar que el build pasa: `./gradlew compileDebugKotlin`.
3. Levantar el `api-gateway` en `:8080` (desde el repo backend).
4. Para verificar el wiring real, en F3 inyectaremos `HealthApi` en un ViewModel y mostraremos el resultado. F2 deja todo enchufado pero no expone una pantalla nueva — el `DesignSystemShowcase` sigue siendo la pantalla por defecto.

---

## Próximo paso · F3

Login: pantalla con email/password, ViewModel con `UiState<AuthResult>`, repository que llama al endpoint del `auth-service` (a confirmar exactamente qué expone — el controller actual en backend está en `/auth/register` con DTO trivial; coordinar con Iturrieta para endpoint real `POST /auth/login` que retorne JWT).
