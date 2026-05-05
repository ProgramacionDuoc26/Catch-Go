# Catch-Go · Android (Kotlin)

App Android nativa de **Catch-Go (Match&Go)**, plataforma de matching de empleos ocasionales en Chile (RM, V y VI región). Consume los microservicios Spring Boot del backend a través del `api-gateway`.

## Stack

- **Kotlin** 2.0.21 + **Jetpack Compose** (Compose BOM 2024.09.00)
- **Material 3**
- minSdk **24** · compileSdk **36** · targetSdk **36**
- Java 11
- Gradle Kotlin DSL

## Estructura

```
app/src/main/java/cl/catchgo/app/
├── MainActivity.kt
└── ui/
    ├── theme/         Tokens de diseño (Color, Type, Spacing, Shape, Theme)
    ├── components/    Componentes reutilizables del design system
    └── showcase/      DesignSystemShowcase — galería viva del DS
```

## Cómo correr

Abre el proyecto en Android Studio (Hedgehog o superior), espera el Gradle sync y pulsa **Run** (▶) con un emulador o dispositivo conectado. La pantalla inicial es el `DesignSystemShowcase` (se reemplazará por la pantalla de Login en F3).

## Backend

El backend Spring Boot vive en repo aparte (`https://github.com/Nicolasiturrieta/Catch-Go`). El `api-gateway` expone la API REST en el puerto `8080`. Desde el emulador, el host del PC se accede como `http://10.0.2.2:8080`.

## Roadmap por features

- [x] **F1** — Fundación + Design System ([docs/F1-design-system.md](docs/F1-design-system.md))
- [ ] F2 — Capa de red (Retrofit + JWT) y arquitectura (ViewModels + Hilt)
- [ ] F3 — Login
- [ ] F4 — Registro con selector de rol
- [ ] F5 — Onboarding del trabajador
- [ ] F6 — Feed de ofertas compatibles
- [ ] F7 — Detalle de oferta
- [ ] F8 — Postular (acción crítica)
- [ ] F9 — Mis postulaciones
- [ ] F10 — Perfil, ajustes, logout

## Reglas de UI

El diseño sigue el documento `HABILIDAD.md` del proyecto: estética transaccional, mobile-first, fricción cero, WCAG AA mínimo, grilla 8dp, áreas táctiles ≥48dp, animaciones <200ms solo para feedback funcional. Sin estética decorativa ni efectos vistosos.
