# F3 — Login

**Estado:** entregado
**Branch:** `feature/rama-colores-paleta` (continúa sobre F1+F2)
**Objetivo:** primera pantalla real consumiendo backend. Reemplaza el `DesignSystemShowcase` como pantalla inicial. La app gatea contra el `auth-service` y persiste la sesión en `DataStore`.

---

## Lo que incluye

### Capa de dominio · `domain/`

| Archivo | Contenido |
|---|---|
| `model/UserRole.kt` | Enum `WORKER`, `EMPRESA`, `UNKNOWN`. `UNKNOWN` es fallback si el backend devuelve un rol que el cliente todavía no conoce — evita crash por enum desconocido. |
| `model/User.kt` | Modelo de dominio: `id`, `email`, `role`, `fullName?`. |
| `model/UserSession.kt` | `token` (JWT) + `user`. Lo que la app persiste y observa. |
| `repository/AuthRepository.kt` | Interface: `login(email, password): Result<UserSession>`, `logout()`. |

### Capa de datos · `data/`

| Archivo | Rol |
|---|---|
| `local/SessionStore.kt` | **Reemplaza** `AuthTokenStore` de F2. Wrapper de `DataStore` que persiste token + user (id, email, role, fullName). Expone `Flow<UserSession?>` y método sincronizado `token()` para el interceptor. |
| `remote/AuthApi.kt` | Retrofit: `POST auth/login` con `LoginRequest` → `LoginResponse`. |
| `remote/dto/LoginRequest.kt`, `LoginResponse.kt`, `UserDto.kt` | DTOs serializables. `UserDto.toDomain()` mapea `role` string a enum con fallback `UNKNOWN`. |
| `remote/AuthInterceptor.kt` | Actualizado para leer del `SessionStore`. |
| `repository/AuthRepositoryImpl.kt` | Llama a `AuthApi.login`, persiste la sesión, devuelve `Result<UserSession>`. Wrap con `runCatching` — el ViewModel recibe el throwable raw y delega al `ErrorMapper`. |

### DI · `di/`

| Archivo | Cambio |
|---|---|
| `NetworkModule.kt` | Agrega `provideAuthApi`. |
| `RepositoryModule.kt` | **Nuevo** módulo `@Binds` que cablea `AuthRepository` ← `AuthRepositoryImpl`. Convención que se va a repetir para futuros repositorios (Profile, Jobs, Matching). |

### Presentación · `ui/`

| Archivo | Rol |
|---|---|
| `login/LoginUiState.kt` | Estado del form: `email`, `password`, `isLoading`, `errorMessage`. Computed `canSubmit` evita botón habilitado con campos vacíos. |
| `login/LoginViewModel.kt` | `@HiltViewModel` con `MutableStateFlow<LoginUiState>`. `onSubmit()` dispara la llamada y mapea errores con `ErrorMapper.map()` para mostrar mensaje en español. |
| `login/LoginScreen.kt` | Compose: header, dos `CatchTextField` (email + password), `PrimaryButton`. Sin link a registro — eso entra en F4. Dos `@Preview` (estado limpio + estado con error) para iteración sin emulador. |
| `home/HomeViewModel.kt` | Mínimo: solo expone `onLogout()`. |
| `home/HomePlaceholderScreen.kt` | Pantalla post-login con saludo + rol + `SecondaryButton` "Cerrar sesión". Es throwaway, F6 la reemplaza con el feed real. |
| `root/SessionState.kt` | Sealed: `Loading`, `Unauthenticated`, `Authenticated(session)`. |
| `root/RootViewModel.kt` | Observa `SessionStore.session` y emite `SessionState`. `stateIn(SharingStarted.Eagerly)` para estabilizar el primer valor antes del `setContent`. |

### MainActivity

`MainActivity` ya no monta el showcase. Monta `AppRoot`, que hace `when` sobre `SessionState` y renderiza `LoginScreen` o `HomePlaceholderScreen`. El `Loading` muestra un `CircularProgressIndicator` centrado mientras DataStore lee del disco la primera vez (≪200ms en práctica).

---

## Decisiones documentadas

### Por qué `Result<UserSession>` y no `Either`/sealed propio
`kotlin.Result` es la primitiva del lenguaje, evita arrastrar Arrow u otra dependencia. La capa de error rica (`ApiError` sealed) ya existe; el repository devuelve el throwable y el ViewModel lo mapea con `ErrorMapper`. Mantiene la separación: el repo no sabe de mensajes en español ni de UI.

### Por qué el repo persiste y no devuelve solo el DTO
Atómico: o se guardó la sesión o falló. Si el repo solo devolviera el `UserSession` y la persistencia quedara en el ViewModel, hay riesgo de que la UI navegue antes de que el token esté en disco — y la siguiente request salga sin `Authorization`. Persistir antes de devolver elimina esa carrera.

### Por qué reemplazar `AuthTokenStore` por `SessionStore`
F2 modeló "token" como la unidad de sesión. F3 mostró que necesitamos también `email`/`role`/`fullName` para personalizar la UI sin volver a llamar `/me`. Renombrar fue más honesto que stackear un segundo store paralelo.

### Por qué `UserRole.UNKNOWN`
El backend hoy todavía no expone `/auth/login` real, y el contrato exacto del campo `role` puede cambiar. Si el cliente recibe un valor inesperado, cae en `UNKNOWN` y la app no crashea — pero el ErrorMapper no lo trata como error. Esa es la decisión consciente: **prefiero fallar suave en producción** y loguear el rol crudo que mostrar un toast feo al usuario.

### Por qué `SessionState.Loading` con Eager
`Eagerly` arranca el flow antes de que el primer collector lo pida, así para cuando MainActivity renderiza ya hay un valor disponible y el `Loading` casi nunca se ve. Si DataStore tarda, el spinner aparece — pero solo el primer launch. `WhileSubscribed(5000)` introduciría una race entre el primer collector y la lectura de disco que produce un destello de `Loading`.

---

## Cómo verificar

1. **Sync Gradle.** Si falla, casi seguro es la dep `lifecycle-runtime-compose` que no resolvió — limpiar caches.
2. **Run** en emulador.
3. **Estado inicial:** la app abre en `LoginScreen`. Si ves el showcase de F1, el `SessionStore` tiene un token persistido — limpiar datos de la app desde Settings o reinstalar.
4. **Validación de form:** el botón "Iniciar sesión" queda deshabilitado mientras email o password estén vacíos.
5. **Llamada al backend:**
    - **Sin backend levantado:** el botón muestra spinner y luego "Sin conexión a internet" (mapeado por `ErrorMapper.NoConnection`). Esperado.
    - **Con backend levantado pero `/auth/login` no implementado todavía:** error "Recurso no encontrado" (404 → `ApiError.NotFound`). También esperado — ese es el siguiente bloqueo del backend.
    - **Con backend con `/auth/login` real:** entra al `HomePlaceholderScreen` mostrando el rol y el nombre.
6. **Cerrar sesión:** botón "Cerrar sesión" en home → vuelve al login automáticamente (el `RootViewModel` recoletea `SessionStore.session = null`).

---

## Bloqueador conocido del backend

El `auth-service` actualmente expone solo `POST /auth/register` con un DTO trivial `{ id, name }`. **No existe `POST /auth/login` ni emisión real de JWT.** F3 deja el cliente listo asumiendo el contrato:

```
POST /auth/login
Body: { "email": "string", "password": "string" }
200:  { "token": "jwt", "user": { "id": "...", "email": "...", "role": "WORKER|EMPRESA", "fullName": "..." } }
401:  Spring error standard
```

Si Iturrieta/Arredondo deciden otro shape, el cambio es 1 archivo (`LoginResponse.kt`) y eventualmente `UserDto.kt`. No tocar nada más del cliente.

---

## Próximo paso · F4

Registro con selector de rol Worker/Empresa. Misma estructura: ViewModel con `RegisterUiState`, `AuthRepository.register(...)` que devuelve `UserSession` (auto-login después de registro). Link "Crear cuenta" desde `LoginScreen` aparece acá.
