# F5 — Navegación bottom + scaffold de tabs

**Estado:** entregado (local)
**Branch:** `feature/unificar-android-app`
**Objetivo:** estructura definitiva de navegación post-login. Reemplaza el `HomePlaceholderScreen` de F3 por un Scaffold con BottomNavigation y 4 tabs. Las features F6–F9 cuelgan de esta arquitectura.

---

## Lo que incluye

### `ui/main/MainTab.kt`
Sealed class con 4 destinos: `Feed`, `Applications`, `Messages`, `Profile`. Cada tab tiene:
- `route` (string para Compose Navigation)
- `icon` (Material Icons Outlined)
- `workerLabel` y `empresaLabel` privados, expuestos vía `label(role: UserRole)` que retorna el correcto.

Un solo composable padre, dos labels distintos según rol — sin duplicar pantallas.

### `ui/main/MainScaffold.kt`
- `Scaffold` con `bottomBar`.
- `NavigationBar` con 4 `NavigationBarItem`, colores forzados a la paleta corporativa (`BrandBlue700` selected, `Gray500` unselected, `White` indicator/container — sin tinte tonal de M3).
- Top de la BottomBar tiene un `HorizontalDivider` 1dp `Gray200` — refuerza la estética industrial-plana de HABILIDAD.md.
- `NavHost` interno con un `composable` por tab.
- Lógica de re-tap correcta: `popUpTo(graph.startDestinationId) { saveState = true }` + `launchSingleTop = true` + `restoreState = true`. Esto evita acumular destinations en el back stack al saltar entre tabs y preserva scroll/state de cada tab.

### Pantallas placeholder por tab

| Tab | Archivo | Renderizado |
|---|---|---|
| Feed | `ui/feed/FeedPlaceholderScreen.kt` | `EmptyState` con icono Search, mensaje adaptado por rol (worker ve "Ofertas cerca tuyo", empresa ve "Mis ofertas") + nota "Disponible en F6". |
| Applications | `ui/applications/ApplicationsPlaceholderScreen.kt` | EmptyState icono Assignment, worker ve "Mis postulaciones", empresa ve "Candidatos", "Disponible en F8". |
| Messages | `ui/messages/MessagesPlaceholderScreen.kt` | EmptyState icono Mail, "Próximamente" — fuera del MVP base. |
| Profile | `ui/profile/ProfilePlaceholderScreen.kt` | Avatar circular con inicial del nombre, nombre/email, `StatusBadge` con rol traducido al español, sección "Mi información" con texto stub, y `SecondaryButton` "Cerrar sesión" enchufado a `ProfileViewModel.onLogout()`. |

### `ui/profile/ProfileViewModel.kt`
Mínimo. Solo `onLogout()` que llama a `authRepository.logout()`. Reemplaza al viejo `HomeViewModel` (que se eliminó junto con `HomePlaceholderScreen`).

### `MainActivity.kt`
- `AppRoot` ahora hace `when` sobre `SessionState`:
    - `Loading` → spinner.
    - `Unauthenticated` → `AuthNavGraph` (introducido en F4).
    - `Authenticated` → `MainScaffold(session = state.session)`.
- Ya no importa `HomePlaceholderScreen` ni `LoginScreen` directamente — todo va vía los grafos.

### Borrado
- `ui/home/HomePlaceholderScreen.kt` y `ui/home/HomeViewModel.kt` eliminados. La carpeta `ui/home/` se borra. Funcionalidad reemplazada por `MainScaffold` + `ProfilePlaceholderScreen`.

---

## Decisiones documentadas

### Por qué un solo `NavHost` interno en `MainScaffold`, no dos NavHosts anidados separados por feature
M3's `NavigationBar` espera operar sobre un mismo `navController`. Anidar grafos por feature complicaría sin valor — y el patrón de `popUpTo(graph.startDestinationId)` solo funciona limpio con un controller raíz por scaffold.

### Por qué cada tab muestra texto distinto según rol pero el composable es el mismo
Worker y Empresa ven la misma estructura (4 tabs, mismas posiciones, mismos íconos). Solo cambia el label y el contenido de cada pantalla. Tener dos `MainTab` enums separados o dos `MainScaffold` distintos sería duplicación injustificada — el rol es un dato de presentación, no un punto de variación de arquitectura.

### Por qué iconos `Outlined` en vez de `Filled`
Outlined transmite la estética industrial/transaccional de HABILIDAD.md (líneas finas, sin "peso visual"). Filled da una vibe más infantil/marketing que el producto no busca.

### Por qué `MessagesPlaceholderScreen` existe ya si la feature de mensajes está fuera del MVP MVP
Reservar el espacio en la nav desde el inicio evita que cuando se sume la feature haya que reorganizar tabs y entrenar al usuario. Mejor placeholder honesto ("Próximamente") que reorg tardío.

### Por qué reemplazar el `HomePlaceholderScreen` y no co-existir
Tener dos formas de ver la sesión post-login (placeholder simple + scaffold completo) confunde. F5 establece el contrato: post-login → MainScaffold. Punto.

---

## Cómo probar

1. Login o registro → entrás al MainScaffold.
2. La tab activa por default es **Ofertas/Mis ofertas** (Feed). Ves el `EmptyState` con icono.
3. Tap entre las 4 tabs — cambia el contenido sin lag, sin re-crear ViewModels (cada tab mantiene su state).
4. Tab "Perfil" → ves tu avatar (inicial), nombre, email, badge con rol, botón cerrar sesión.
5. Cerrar sesión → vuelve al login. Loguearte de nuevo → vuelve a la tab Feed (default), no a la última que tenías.

---

## Próximo paso · F6

Llenar la tab Feed con ofertas reales (mock data chileno realista, pull-to-refresh, filtros por categoría).
