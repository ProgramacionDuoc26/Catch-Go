# F7 — Detalle de oferta

**Estado:** entregado (local)
**Branch:** `feature/unificar-android-app`
**Objetivo:** pantalla de detalle de una oferta accesible desde el feed. Cierra el flujo "ver oferta" y prepara el botón "Postular" que F8 enchufa a la BottomSheet.

## Lo que incluye

- `JobsRepository.getOffer(id)` agregado a la interface.
- `JobsApi.get(@Path id)` con `@GET("jobs/{id}")`.
- `JobsRepositoryImpl.getOffer()`: en mock busca en cache con `delay(300)`.
- `ui/detail/`: `OfferDetailUiState`, `OfferDetailViewModel` (toma id desde `SavedStateHandle["id"]`), `OfferDetailScreen`.
- Layout: `Scaffold` con `TopAppBar` (back) + `bottomBar` con `PrimaryButton "Postular"` + contenido scrollable (header con badges, descripción, detalles, requisitos).
- Salario formateado en CLP con punto de miles (`$25.000 CLP / día`).
- Nueva ruta `offer/{id}` en el NavHost de `MainScaffold` con `navArgument` tipado String.
- `FeedScreen.onOfferClick` enchufado: `navController.navigate("offer/$id")`.
- Bottom bar de tabs se oculta cuando `currentRoute = "offer/{id}"`.

## Decisiones

- **Sticky bottom button**: la regla "fricción cero" de HABILIDAD.md exige el CTA Postular siempre visible. Si fuera al final del scroll, requeriría una acción extra para llegar.
- **Single NavHost en MainScaffold con visibilidad condicional**: alternativa a NavHosts anidados. Más simple, evita complicaciones con `NavigationBar` selection.
- **`SavedStateHandle["id"]`**: patrón canónico para pasar nav args al ViewModel. Sobrevive recreación gratis.

## Contrato REST propuesto

```
GET /jobs/{id}
200: JobOfferDto (mismo shape que en /jobs)
404: Spring error → cliente cae en NotFound
```

## Próximo paso · F8

Postular: BottomSheet con mensaje opcional + tab Mis postulaciones con StatusBadge por estado.
