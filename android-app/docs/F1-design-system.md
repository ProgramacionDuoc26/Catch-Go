# F1 — Fundación + Design System

**Estado:** entregado
**Branch:** `feature/f1-design-system`
**Objetivo:** dejar tokens de diseño y componentes base listos para que cualquier feature posterior se construya sin reinventar UI.

---

## Lo que incluye

### Tokens de diseño · `ui/theme/`

| Archivo | Contenido |
|---|---|
| `Color.kt` | Paleta corporativa Catch-Go: azul `#1D4ED8` (primary), grises Tailwind (`Gray50`–`Gray900`), semánticos (`Success`, `Warning`, `Error`). Todos cumplen contraste **WCAG AA** sobre blanco. |
| `Type.kt` | Escala tipográfica completa Material 3 con `FontFamily.Default` (Roboto en Android). Comentado el path para migrar a Inter cuando se agreguen los `.ttf` a `res/font/`. |
| `Spacing.kt` | Tokens de grilla 8dp + medio paso 4dp: `xxs`, `xs`, `sm`, `md`, `lg`, `xl`, `xxl`, `xxxl`. |
| `Shape.kt` | Esquinas redondeadas conservadoras (4 / 8 / 12 / 16 / 24 dp). |
| `Theme.kt` | `CatchGoTheme {}` light-only. **Sin** `dynamicColor` — la marca corporativa no se sacrifica al wallpaper del dispositivo. |

### Componentes base · `ui/components/`

| Componente | Descripción |
|---|---|
| `PrimaryButton` | CTA azul. `min-height 48dp` (accesibilidad). Soporta `loading` (`CircularProgressIndicator` interno) y `enabled`. `fullWidth` por defecto. |
| `SecondaryButton` | Outlined neutro. Mismo `min-height 48dp`. |
| `CatchTextField` | Wrapper de `OutlinedTextField` con `label`, `placeholder`, `supportingText`, `isError`, `isPassword`, slots para íconos. |
| `OfferCard` | Tarjeta de oferta: título, empresa, comuna, jornada y badge de score (verde ≥80, amarillo ≥50, gris <50). Borde 1dp, elevación 0 (estética plana). |
| `StatusBadge` | Pill con 5 variantes: `Pending` (amarillo), `Success` (verde), `Error` (rojo), `Info` (azul), `Neutral` (gris). |
| `SkeletonBox` | Caja de carga con pulse animado de 900ms. Usa `Gray200` con alpha animado. |
| `EmptyState` | Estado vacío genérico con título, descripción, slot de icono y slot de acción. |

### Showcase · `ui/showcase/DesignSystemShowcase.kt`

Pantalla scrolleable que muestra **todos** los tokens y componentes en su contexto real. Funciona como:

- QA visual rápido al modificar tokens.
- Referencia viva para quien construye nuevas pantallas.
- Vista de previsualización (`@Preview` incluido) y como pantalla principal de la app durante F1.

### MainActivity

`MainActivity` ahora monta `CatchGoTheme { Scaffold { DesignSystemShowcase() } }`. El `Scaffold` respeta los `WindowInsets` automáticamente, requerido tras `enableEdgeToEdge()` en Android 15+ (`targetSdk 36`).

---

## Decisiones de diseño documentadas

### Por qué Light-only y sin dynamic color

El documento `HABILIDAD.md` exige una identidad corporativa fuerte y consistente. `dynamicColor` (Android 12+) tomaría colores del wallpaper del usuario, rompiendo la marca. Dark theme se considerará en una iteración futura, no en MVP.

### Por qué Roboto y no Inter (todavía)

Inter es la fuente preferida en `HABILIDAD.md`, pero requiere bundlear `.ttf` en `res/font/` (~200KB). Para no inflar el APK en F1 sin necesidad, usamos `FontFamily.Default` (= Roboto en Android), que cumple los mismos requisitos: sans-serif, neutra, alta legibilidad. La migración a Inter está documentada en el comentario de `Type.kt` y se hará cuando aporte valor real.

### Por qué borde 1dp en lugar de elevación

El estilo industrial / transaccional de Catch-Go favorece superficies planas y separación por borde fino antes que sombras. Reduce ruido visual y mejora la legibilidad en pantallas con mucho contenido (feed de ofertas).

### Por qué `StatusBadge` con `bg + fg` derivados de `type`

El componente es opaco al consumidor — no se le pueden pasar colores arbitrarios. Esto fuerza consistencia: cualquier estado de la app cae en una de las 5 variantes, no aparecen amarillos a gusto del programador.

---

## Cómo verificar

1. Abrir el proyecto en Android Studio.
2. Esperar Gradle sync.
3. Run (▶) en emulador.
4. Validar visualmente en `DesignSystemShowcase`:
   - Colores se ven sólidos y diferenciables.
   - Tipografía clara, sin "saltos" raros entre pesos.
   - Botones con altura mínima 48dp (presionables sin esfuerzo).
   - Skeleton anima sin saltos.
   - Empty state legible y con CTA visible.
5. (Opcional) Abrir el panel de Preview en `DesignSystemShowcase.kt` para iteración rápida.

Compilación validada localmente: `./gradlew compileDebugKotlin` → BUILD SUCCESSFUL.

---

## Cómo extender en features siguientes

- **No** crear `Color(0xFF...)` inline en pantallas. Usar tokens de `Color.kt` o `MaterialTheme.colorScheme.*`.
- **No** usar números mágicos de padding. Usar `Spacing.*`.
- **No** copiar el cuerpo de un componente para variarlo en una pantalla. Si falta una variante, **agregar al componente** (parámetro nuevo) y refactorizar el showcase.
- Si una pantalla necesita un componente no contemplado, primero proponerlo en el `DesignSystemShowcase` y luego usarlo en la pantalla.

---

## Próximo paso · F2

Capa de red contra los microservicios Spring Boot (`api-gateway` en `:8080`):

- Retrofit + OkHttp + kotlinx.serialization
- `AuthInterceptor` para JWT desde DataStore
- `ErrorMapper` que traduce `common-exceptions` del backend
- Hilt para DI
- ViewModels con `StateFlow` y `UiState` sellado

Cuando F2 esté listo, F3 (Login) será la primera pantalla real consumiendo backend.
