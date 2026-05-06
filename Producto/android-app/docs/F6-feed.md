# F6 — Feed de ofertas

**Estado:** entregado (local)
**Branch:** `feature/unificar-android-app` (commit pendiente)
**Objetivo:** llenar la tab "Ofertas" con un feed real para el rol Worker. Lista de ofertas chilenas creíbles, filtros por categoría, pull-to-refresh, skeleton de carga, estado vacío. Empresa mantiene placeholder por ahora (publicar ofertas es feature post-MVP).

---

## Lo que incluye

### Dominio · `domain/model/`

| Archivo | Contenido |
|---|---|
| `JobCategory.kt` | Enum: GUARDIA, CONSERJE, TEMPORERO, CARGA, NINERA, ASEO, OTRO. Cada uno con `display` en español. Refleja las categorías del informe Entrega 1. |
| `JobOffer.kt` | Modelo con id, title, company, category, region, comuna, jornada, scheduleText, salaryClp, salaryUnit, description, requirements, score. |
| `JobFilter.kt` | Solo `category: JobCategory?` por ahora. Listo para agregar comuna/radius en F-Geo. |

### Dominio · `domain/repository/JobsRepository.kt`
Interface con `observeOffers(filter): Flow<List<JobOffer>>` y `refresh(): Result<Unit>`. La separación entre "observar" y "refrescar" es deliberada: la UI siempre observa el cache local; el refresh dispara la llamada de red y el StateFlow del cache emite los nuevos datos. Esto da pull-to-refresh sin que la lista parpadee.

### Datos · `data/`

| Archivo | Rol |
|---|---|
| `data/remote/dto/JobOfferDto.kt` | DTO serializable. `toDomain()` mapea `category` string a enum con fallback `OTRO` para compat. |
| `data/remote/JobsApi.kt` | Retrofit `GET jobs?category=&comuna=&radius=`. Listo para el día que el backend lo implemente. |
| `data/repository/MockJobs.kt` | 12 ofertas chilenas: Walmart Las Condes, Líder Maipú, Hotel Sheraton, Edificio La Reina, Securitas Recoleta (OS10), G4S Providencia, Concha y Toro Pirque, Frutera La Calera, Familia Vitacura (niñera), Estadio Nacional Ñuñoa, Aseo Total Sur Las Condes, Cencosud Quilicura. Sueldos en CLP coherentes con cada rubro. |
| `data/repository/JobsRepositoryImpl.kt` | Singleton con `MutableStateFlow` interno como cache. `observeOffers` filtra y ordena por score descendente. `refresh()` con flag `USE_MOCK_JOBS`: en mock, hace `delay(700)` y emite `MockJobs.shuffled()` para que se vea que pasó algo. |

### DI
- `NetworkModule.provideJobsApi` agregado.
- `RepositoryModule.bindJobsRepository` agregado (mismo patrón que Auth).

### ApiConfig
- Nuevo flag `USE_MOCK_JOBS = true`. Backend hoy expone `/jobs` con DTO trivial `(id, name)` — bajar a false cuando se implementen los campos reales.

### Presentación · `ui/feed/`

| Archivo | Rol |
|---|---|
| `FeedUiState.kt` | `isLoading`, `isRefreshing`, `offers`, `selectedCategory`, `errorMessage`. |
| `FeedViewModel.kt` | Combina 3 flows: la categoría seleccionada (filtra el repo), el flag de refresh, y los offers del repo (mediante `flatMapLatest` sobre la categoría). `init` dispara `refresh()` para poblar la lista al abrir la tab. |
| `FeedScreen.kt` | Punto de entrada que routea por `UserRole`. Worker → `WorkerFeedRoute` (real). Empresa/Unknown → `FeedPlaceholderScreen` (existente de F5). |

### `WorkerFeedRoute`
- `PullToRefreshBox` (Material 3) envuelve todo el feed.
- `FeedContent` arma: header con título + count, fila scrollable de chips, lista de `OfferCard` o estado de carga/vacío.

### `CategoryChipsRow`
- `LazyRow` con `FilterChip` (Material 3).
- Primer chip "Todas" deselecciona el filtro.
- Tap a un chip ya seleccionado lo deselecciona — UX cómoda, evita que el usuario quede atrapado en una categoría.

### `OffersList`
- `LazyColumn` con `OfferCard` por item. Reusa el componente F1 con su badge de score (verde ≥80, amarillo ≥50). `key = { it.id }` para que la animación de cambio de filtro sea estable.

### `SkeletonList`
- 4 `SkeletonBox` de 112dp simulando la altura aproximada de un OfferCard. Solo aparece en la primera carga (cuando `isLoading = true`). Al refrescar no aparece — la lista anterior se mantiene.

### `EmptyFeed`
- `EmptyState` con `Icons.Outlined.SearchOff`. Mensaje contextual: si hay filtro activo, sugiere quitarlo; si no, dice que no hay ofertas y que pull-to-refresh.

### Wire en `MainScaffold`
- `composable(MainTab.Feed.route) { FeedScreen(role = session.user.role) }`. El click en `OfferCard` se queda en `onOfferClick = {}` por ahora — F7 lo conecta a la pantalla de detalle.

---

## Decisiones documentadas

### Por qué `Flow<List<JobOffer>>` y no `suspend fun list()` directo
Porque mañana cuando entren websockets/SSE para ofertas en tiempo real (parte del diferenciador "geolocalización en tiempo real" del informe), el cliente ya está observando un Flow. El refactor sería invisible para los ViewModels.

### Por qué `flatMapLatest` sobre la categoría seleccionada
Cuando el usuario cambia de filtro, queremos cancelar la suscripción a la lista anterior y suscribirnos a la nueva. `flatMapLatest` hace exactamente eso. Si usáramos `combine` con la lista directamente, se acumularían fragmentos de filtros viejos.

### Por qué orden por score descendente y no por fecha
Coherente con la regla R2 del informe (worker solo ve ofertas con score > 50%) y con el valor central del producto (matching). El usuario que abre el feed tiene que ver primero las ofertas mejor matcheadas para él, no las más recientes.

### Por qué shuffle en el mock refresh
Para que pull-to-refresh tenga **algún feedback visible** durante demos. Con datos hardcoded estáticos, refrescar no cambiaría nada y daría la sensación de que está roto. Cuando entre el backend real, esto se reemplaza por la lista que devuelva el endpoint.

### Por qué empresa no tiene feed
Construir el lado de empresa (publicar oferta + ver candidatos) duplica el alcance del MVP. El informe identifica como prioridad alta los 4 módulos core, pero el flow del worker es el que atrae a los workers, que son la masa crítica del producto. Sin workers no hay producto. Sin oferta de trabajos al worker, no hay app. La publicación de ofertas se atiende cuando esté listo el flujo del worker punta a punta.

---

## Contrato REST que el backend debería implementar

```
GET /jobs?category=GUARDIA&comuna=Las%20Condes&radius=5
Response 200:
[
  {
    "id": "j-001",
    "title": "Reponedor nocturno",
    "company": "Walmart Chile",
    "category": "OTRO",
    "region": "Metropolitana",
    "comuna": "Las Condes",
    "jornada": "Turno noche",
    "scheduleText": "22:00 - 06:00",
    "salaryClp": 18000,
    "salaryUnit": "turno",
    "description": "Reposición de góndolas...",
    "requirements": ["Mayor de 18", "Disponibilidad inmediata"],
    "score": 92
  },
  ...
]
```

`category` puede venir en cualquier case (el cliente normaliza con `uppercase()`). Valores fuera de la enum caen en `OTRO`. `score` se calcula en el `matching-service` y viene precalculado en cada job para el worker que hace el GET (idempotente, cacheable per-user en Redis).

---

## Cómo probar

1. Login como worker (cualquier mail sin "empresa").
2. Tab Ofertas se abre con skeleton breve → 12 ofertas ordenadas por score.
3. Tap "Guardia" → quedan 2 ofertas.
4. Tap "Guardia" otra vez → vuelven las 12 (deselect implícito).
5. Pull-to-refresh → spinner ~700ms, lista se reordena (shuffle simulado).
6. Login como empresa (mail con "empresa"): tab Ofertas muestra placeholder de F5.

---

## Próximo paso · F7

Detalle de oferta. Tap en `OfferCard` → `OfferDetailScreen` con descripción completa, requisitos como chips, ubicación, sueldo, score, y botón gigante "Postular". Navegación con argumentos tipados.
