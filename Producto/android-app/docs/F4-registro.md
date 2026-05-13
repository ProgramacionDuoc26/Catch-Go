# F4 — Registro con selector de rol

**Estado:** entregado (local)
**Branch:** `feature/unificar-android-app`
**Objetivo:** segunda pantalla de auth. El usuario elige rol Worker/Empresa, completa datos y queda auto-logueado. Cierra el flow de auth para que F5 pueda asumir sesión válida.

---

## Lo que incluye

### Dominio · `domain/model/`
- `RegisterInput` — input limpio con email, password, fullName, rut, phone, role.

### Datos · `data/`
- `data/remote/dto/RegisterRequest.kt` — DTO serializable que va al backend (role como String).
- `data/remote/AuthApi.kt` — agrega `POST auth/register` que devuelve `LoginResponse` (mismo shape que login: token + user).
- `data/repository/AuthRepositoryImpl.kt` — implementa `register()` con branch mock/real igual que `login()`. Comparte helper `buildSession()` entre los dos caminos mock.
- `data/remote/ApiConfig.kt` — restablece `USE_MOCK_AUTH = true` (se había perdido en la limpieza del android-app duplicado).

### Dominio · `domain/repository/AuthRepository.kt`
- Agrega `suspend fun register(input: RegisterInput): Result<UserSession>`.

### Presentación · `ui/register/`
- `RoleSelector.kt` — componente nuevo con dos `Card` lado a lado (Worker/Empresa). Visualmente: borde 2dp + fondo `BrandBlue50` + texto azul cuando seleccionado, borde 1dp gris claro + fondo blanco cuando no. Iconos `Icons.Outlined.Work` y `Icons.Outlined.Business`. Toda la lógica de "selected" la maneja el padre, el componente es puramente visual.
- `RegisterUiState.kt` — campos del form + `canSubmit` que valida: rol elegido, password ≥ 6 chars, todos los campos no-blancos, no loading.
- `RegisterViewModel.kt` — patrón idéntico a `LoginViewModel`. `onSubmit` arma `RegisterInput`, llama `authRepository.register()`, mapea error con `ErrorMapper`. La sesión se persiste dentro del repository, así que al éxito el `RootViewModel` recibe el cambio del `SessionStore` y MainActivity navega solo.
- `RegisterScreen.kt` — Compose: header + RoleSelector + 5 `CatchTextField` (nombre/razón social, RUT, teléfono, email, password) + botón + link "¿Ya tienes cuenta? Inicia sesión". Label dinámica: "Razón social" si rol = EMPRESA, "Nombre completo" si Worker.

### Login modificado
- `LoginScreen` ahora recibe `onRegisterClick: () -> Unit` y muestra link "¿No tienes cuenta? Crear una" debajo del botón.

### Navegación · `ui/auth/AuthNavGraph.kt`
- Nuevo `NavHost` con dos rutas: `login` y `register`.
- `LoginScreen.onRegisterClick` → navega a register.
- `RegisterScreen.onLoginClick` → `popBackStack()`.
- Reemplaza la llamada directa a `LoginScreen` desde `MainActivity` cuando el estado es `Unauthenticated`.

### Dependencias nuevas
- `androidx.navigation:navigation-compose:2.8.4` — para el NavHost.
- `androidx.compose.material:material-icons-extended` (sin versión, parte del Compose BOM) — para íconos de roles.

---

## Decisiones documentadas

### Por qué password mínimo 6 caracteres en cliente
El backend probablemente va a tener su propia validación más estricta. El cliente impone un mínimo razonable para evitar requests inútiles a 4 caracteres. Si el backend exige 8 o reglas más complejas, el `ErrorMapper` traduce el 400 con su mensaje y el usuario lo ve.

### Por qué la pantalla de Register tiene 5 campos en vez de 2
Los datos solicitados (nombre, RUT, teléfono, email, password) están alineados al schema del informe Entrega 1 (sección 4.1, RF-01). RUT y teléfono son críticos para empleos temporales en Chile (verificación de identidad, contacto fuera de la app). Pedir todo de una vez evita un onboarding multi-paso que rompería el principio "fricción cero" si tiene más de un screen.

### Por qué `UserRole.UNKNOWN` no aparece en el selector
`UNKNOWN` solo existe como fallback de deserialización cuando el backend devuelve un rol inesperado. El usuario no debe poder elegirlo. El selector solo muestra Worker y Empresa.

### Por qué auto-login después de registro
Pedirle al usuario que se loguee inmediatamente después de registrarse es fricción innecesaria. El backend devuelve token + user en la response, igual que login. Una sola persistencia, una sola navegación.

---

## Cómo probar (con mock activo)

1. App arranca → Login.
2. Tap "Crear una" → Register.
3. Elegí rol, completá los 5 campos (cualquier valor sirve, password ≥ 6).
4. Tap "Crear cuenta y empezar".
5. Spinner ~900ms → entras al `MainScaffold` con bottom nav.
6. Cerrar sesión desde tab Perfil → vuelve al Login.
7. Tap "Crear una" otra vez → vuelve a Register.

---

## Próximo paso · F6

Feed de ofertas con datos chilenos creíbles. Tab "Ofertas" deja de ser placeholder.
