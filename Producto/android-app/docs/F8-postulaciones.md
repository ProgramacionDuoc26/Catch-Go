# F8 — Postular + Mis postulaciones

**Estado:** entregado (local)
**Branch:** `feature/unificar-android-app`
**Objetivo:** cerrar el flujo crítico del worker. Postular a una oferta vía BottomSheet, ver el listado de postulaciones agrupadas por estado, navegar de vuelta al detalle de la oferta postulada.

---

## Lo que incluye

### Dominio · `domain/`

| Archivo | Contenido |
|---|---|
| `model/ApplicationStatus.kt` | Enum: PENDING, ACCEPTED, REJECTED. Cada uno con `display` en español. |
| `model/JobApplication.kt` | id, offerId, **offerTitle**, **company**, **comuna** (denormalizados, evitan look-up al renderizar la lista), message?, status, createdAtIso. |
| `repository/ApplicationsRepository.kt` | Interface con `observeMyApplications()`, `observeAppliedOfferIds()` (derivado para feedback en feed/detalle), `apply(offerId, message): Result<JobApplication>`. |

### Datos · `data/`

| Archivo | Rol |
|---|---|
| `remote/dto/ApplicationDto.kt` | DTO con `toDomain()` que mapea string → enum con fallback PENDING. |
| `remote/dto/CreateApplicationRequest.kt` | Body del POST: offerId + message?. |
| `remote/ApplicationsApi.kt` | Retrofit: `GET postulaciones/me`, `POST postulaciones`. |
| `repository/MockApplications.kt` | 3 postulaciones pre-seedeadas: 1 ACCEPTED (Sheraton hace 2 días), 1 PENDING (Concha y Toro hace 5h), 1 REJECTED (Estadio Nacional hace 7 días). Permite que el tab no esté vacío al primer login. |
| `repository/ApplicationsRepositoryImpl.kt` | Singleton con `MutableStateFlow<List<JobApplication>>`. `apply()` con flag `USE_MOCK_APPLICATIONS`: en mock hace `delay(600)`, mira el offer en `JobsRepository.getOffer()` para denormalizar título/empresa/comuna, y antepone la nueva postulación. Rechaza con `IllegalStateException` si ya hay una para esa oferta. |

### DI
- `NetworkModule.provideApplicationsApi`.
- `RepositoryModule.bindApplicationsRepository`.

### Util · `core/util/TimeAgo.kt`
Helper `timeAgo(iso: String)` que formatea distancia temporal en español: "hace un momento" / "hace 5 min" / "hace 2 h" / "hace 3 d" / "hace 2 sem". Usa `java.time.Instant.parse` y `Duration.between`. Tolera ISO inválido devolviendo string vacío.

### Presentación · postular

| Archivo | Rol |
|---|---|
| `ui/apply/ApplyBottomSheet.kt` | Composable puro (sin VM propio). Muestra título, subtitulo "$offer · $company", `CatchTextField` para mensaje (multiline, max 240 chars con contador `0 / 240`), botón "Cancelar" + "Postular ahora" en una fila 50/50. La función recibe `isSubmitting`, `errorMessage`, callbacks. La oferta y el VM viven en el caller (OfferDetailScreen). |

### Presentación · detalle (extendido)
- `OfferDetailUiState` agrega: `isApplied`, `isApplying`, `applyError`.
- `OfferDetailViewModel` ahora inyecta `ApplicationsRepository` además de `JobsRepository`. Combina `internalState` con `observeAppliedOfferIds()` para derivar `isApplied` reactivamente. Métodos: `submitApplication(message)`, `clearApplyError()`. Expone un `Channel<DetailEvent>` para notificar `ApplicationSucceeded` (event-style) — el screen lo collecta en `LaunchedEffect`.
- `OfferDetailScreen`:
    - Si `isApplied` → bottom bar muestra `SecondaryButton "Ya postulaste"` deshabilitado.
    - Si no → `PrimaryButton "Postular a esta oferta"` que abre la BottomSheet (`showSheet = true`).
    - Al recibir `ApplicationSucceeded` → cierra la sheet, muestra `Snackbar("Postulación enviada")`. La derivación de `isApplied` actualiza el botón solo.
    - Dismiss del sheet bloqueado mientras `isApplying = true`.

### Presentación · tab postulaciones

| Archivo | Rol |
|---|---|
| `ui/applications/ApplicationsUiState.kt` | `isLoading`, `groups: Map<ApplicationStatus, List<JobApplication>>`, `isEmpty` derivado. |
| `ui/applications/ApplicationsViewModel.kt` | Observa `observeMyApplications()`, agrupa por status iterando `ApplicationStatus.entries`. |
| `ui/applications/ApplicationsScreen.kt` | Routea por rol: WORKER ve la lista real, EMPRESA mantiene el placeholder de F5 (la vista "Candidatos" para empresa queda fuera de scope MVP). |

### Estructura de la lista
- `LazyColumn` con secciones por status (Pendiente, Aceptada, Rechazada). Cada sección tiene un `SectionHeader` ("Pendiente · 2") y debajo los items.
- `ApplicationCard`: título de la oferta + empresa+comuna en gris, `StatusBadge` a la derecha (Pending=amarillo, Success=verde, Error=rojo), `timeAgo` debajo, y si hay mensaje del usuario lo muestra entre comillas.
- Tap → callback `onApplicationClick(offerId)` que `MainScaffold` enchufa a `navigate("offer/$id")`. Reusa la pantalla de detalle de F7.
- Empty state con icono Assignment cuando no hay postulaciones.

### Wire en `MainScaffold`
- Tab `Applications` ahora monta `ApplicationsScreen` en vez del placeholder de F5.
- Pasa `onApplicationClick` que navega al detail.

---

## Decisiones documentadas

### Por qué denormalizar offerTitle/company/comuna en `JobApplication`
Si solo guardáramos `offerId`, renderizar la lista de postulaciones requeriría traer cada oferta del cache de jobs en runtime. Eso introduce un punto de fallo (¿qué pasa si la oferta fue eliminada por el backend mientras el worker tenía la postulación pendiente?). Denormalizar refleja también lo que el backend probablemente va a hacer en el `GET /postulaciones/me` — devolver el shape enriquecido para ahorrarle round-trips al cliente.

### Por qué la BottomSheet no tiene su propio ViewModel
Considerado: `ApplyViewModel` aparte. Descartado: el ciclo de vida de la sheet es muy corto, los datos (mensaje) son string puro, y la operación (apply) ya pertenece conceptualmente al detalle. Tener dos VMs sincronizados sería más complejo y aportaría cero. La sheet es un componente de UI puro — el VM que ya existe (OfferDetailViewModel) hace el submit.

### Por qué `Channel` para `ApplicationSucceeded` y no estado
`isApplied` ya es estado reactivo (derivado del repo). Pero "post-apply, mostrar snackbar y cerrar sheet" es un evento de **una sola vez** — si lo modeláramos como state, después de la primera emisión seguiría siendo true y mostraría el snackbar en cada recomposición. Channel + receiveAsFlow es el patrón canónico de Compose para one-shot events sin re-emisión.

### Por qué pre-seed de 3 postulaciones
Para que el tab "Postulaciones" no esté vacío al primer login con un usuario nuevo. Hace que la demo se vea más real. Los pre-seed cubren los 3 estados (PENDING, ACCEPTED, REJECTED) para que se aprecie el agrupamiento. Cuando entre el backend real, los pre-seed desaparecen (cache se llena del API).

### Por qué tap en ApplicationCard navega al detail (en vez de mostrar info inline)
Reusa la pantalla de detalle de F7 sin duplicar UI. Además le da al worker contexto completo de la oferta a la que postuló (reqs, sueldo, descripción), no solo título y empresa. Además F7 ya tiene la lógica `isApplied` que muestra "Ya postulaste" en lugar del CTA — el worker puede entrar al detail desde "Postulaciones" y entender que está en flujo.

### Por qué el botón post-apply es `SecondaryButton "Ya postulaste"` deshabilitado y no oculto
Si lo escondiéramos, el bottom bar quedaría vacío y la pantalla se vería rara. Mostrarlo deshabilitado con texto explícito es feedback claro: "esto está hecho". Patrón consistente con cualquier app que muestra "Pedido enviado" en lugar de "Realizar pedido" cuando el estado cambia.

### Por qué el textarea de mensaje permite multilínea pero no tiene altura fija grande
`singleLine = false` en `CatchTextField`. La altura se ajusta sola al contenido. Empieza compacta (1 línea visible), crece a medida que el usuario tipea. Deja la sheet usable en pantallas chicas.

---

## Contratos REST propuestos

```
POST /postulaciones
Body: { "offerId": "j-001", "message": "opcional" }
201: ApplicationDto enriquecido con offerTitle/company/comuna/status=PENDING/createdAt
409: ya existe postulación para ese par worker+offer

GET /postulaciones/me
200: List<ApplicationDto>
   ordenado por createdAt desc, todas las del usuario actual (auth via JWT)
```

Nota para el backend: `status` viene del lado de la empresa (cuando acepta/rechaza). Cliente nunca cambia el status — solo crea con PENDING y lee.

---

## Cómo probar

1. Login como worker → tab Postulaciones ya tiene 3 items (1 aceptada, 1 pendiente, 1 rechazada) agrupados.
2. Tap en una postulación → te lleva al detalle de la oferta correspondiente (botón muestra "Ya postulaste" deshabilitado).
3. Volver, ir a Ofertas, tap en una oferta nueva → botón muestra "Postular".
4. Tap "Postular" → se abre BottomSheet con título de la oferta.
5. Escribir mensaje (opcional, contador de caracteres) → tap "Postular ahora".
6. Spinner ~600ms → sheet se cierra, snackbar "Postulación enviada".
7. Tab Postulaciones → la nueva está al tope de la sección "Pendiente · X".
8. Volver al detalle de la oferta postulada → ahora muestra "Ya postulaste".
9. Postular dos veces a la misma oferta no es posible — el botón ya está deshabilitado.

---

## Próximo paso · F9

Perfil editable adaptado al rol. Worker ve experiencia, certificaciones, disponibilidad. Empresa ve razón social, RUT, contacto.
