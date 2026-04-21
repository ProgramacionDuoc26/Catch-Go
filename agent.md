# Agent.md - Guía para Desarrollador Senior

## Match&Go - Plataforma de Trabajos Ocasionales

Este documento guía a un agente de IA para desarrollar el código del proyecto Match&Go.

---

## 1. Contexto del Proyecto

**Match&Go** es una plataforma de matching entre empresas con trabajos ocasionales y trabajadores buscando empleo temporal en Chile (Región Metropolitana, V y VI región).

### Problema que resuelve
- **Empresas:** Necesitan workers temporales (guardias, conserjes, temporeros, carga/descarga, niñeras, aseo)
- **Workers:** Buscan complementar ingresos o empleo temporal

### Modelo de negocio
- Empresas pagan suscripción mensual
- Trial de 30 días gratis
- Matching automático con IA

---

## 2. Stack Tecnológico

| Capa | Tecnología |
|------|-------------|
| **Frontend Web** | Next.js 14+ (App Router), TypeScript, Tailwind CSS |
| **Frontend App** | React Native o Expo (futuro) |
| **Backend** | Next.js API Routes + Edge Functions |
| **ORM** | Prisma |
| **Base de Datos** | Supabase (PostgreSQL) |
| **Auth** | Supabase Auth (Google Provider) |
| **Hosting** | Vercel |
| **Storage** | Supabase Storage |
| **Pagos** | WebPay (Transbank) |

### Repositorios
- **Backend:** https://github.com/MiguelAAV/busqueda_de_trabajo_Match-Go (Edge Functions)
- **Frontend:** Mismo repo o repositorio separado

---

## 3. Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        ENTORNO SUPABASE                                      │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌───────────────┐ │
│  │    Auth     │   │  Database    │   │   Storage   │   │ Edge Functions│ │
│  │ (Google)    │   │ (PostgreSQL)│   │  (Archivos) │   │ (Lógicanegocio)│ │
│  └─────────────┘   └─────────────┘   └─────────────┘   └───────┬───────┘ │
└───────────────────────────────────────────────┬───────────────┴──────────┘
                                                │
                    ┌───────────────────────────┼───────────────────────────┐
                    │                           │                           │
                    ▼                           ▼                           ▼
┌─────────────────────────┐     ┌─────────────────────────┐    ┌─────────────────────────┐
│   Backend Next.js       │     │   Frontend Next.js       │    │   WebPay (Transbank)   │
│   (Edge Functions)      │     │   (Vercel)              │    │   (Pagos)              │
└─────────────────────────┘     └─────────────────────────┘    └─────────────────────────┘
```

### Flujo de datos
1. Frontend → REST API → Edge Functions (Supabase)
2. Edge Functions → Lógica de negocio → Database/Auth/Storage
3. Edge Functions → WebPay → Procesamiento de pagos

---

## 4. Estructura del Proyecto

### Backend (Edge Functions)
```
supabase/
├── functions/
│   ├── auth/
│   │   ├── sign-in.ts
│   │   ├── sign-up.ts
│   │   └── sign-out.ts
│   ├── ofertas/
│   │   ├── create.ts
│   │   ├── list.ts
│   │   ├── get.ts
│   │   ├── update.ts
│   │   └── delete.ts
│   ├── matching/
│   │   ├── run.ts
│   │   └── get-matches.ts
│   ├── postulaciones/
│   │   ├── create.ts
│   │   ├── list.ts
│   │   ├── accept.ts
│   │   └── reject.ts
│   ├── trabajadores/
│   │   ├── profile.ts
│   │   ├── search.ts
│   │   └── update.ts
│   ├── empresas/
│   │   ├── profile.ts
│   │   └── update.ts
│   ├── suscripcion/
│   │   ├── get.ts
│   │   └── upgrade.ts
│   └── pagos/
│       ├── webpay-init.ts
│       └── webpay-confirm.ts
└── config.toml
```

### Frontend (Next.js)
```
src/
├── app/
│   ├── (auth)/
│   │   ├── login/page.tsx
│   │   └── register/page.tsx
│   ├── (dashboard)/
│   │   ├── empresa/
│   │   │   ├── dashboard/page.tsx
│   │   │   ├── ofertas/page.tsx
│   │   │   ├── ofertas/nueva/page.tsx
│   │   │   ├── candidatos/page.tsx
│   │   │   ├── suscripcion/page.tsx
│   │   │   └── perfil/page.tsx
│   │   └── trabajador/
│   │       ├── dashboard/page.tsx
│   │       ├── ofertas/page.tsx
│   │       ├── postulaciones/page.tsx
│   │       ├── perfil/page.tsx
│   │       └── ajustes/page.tsx
│   └── page.tsx
├── components/
│   ├── ui/
│   ├── forms/
│   ├── cards/
│   └── features/
├── lib/
│   ├── supabase.ts
│   ├── auth.ts
│   └── api.ts
├── hooks/
├── store/
└── types/
```

---

## 5. Schema de Prisma

```prisma
generator client {
  provider = "prisma-client-js"
}

datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

enum TipoUsuario {
  EMPRESA
  TRABAJADOR
}

enum TipoContrato {
  PLAZO_FIJO
  HONORARIOS
}

enum EstadoOferta {
  ABIERTA
  CERRADA
  CON_CANDIDATOS
  COMPLETADA
}

enum EstadoPostulacion {
  PENDIENTE
  ACEPTADO
  RECHAZADO
}

enum PlanSuscripcion {
  BASICO
  PROFESIONAL
  ENTERPRISE
  TRIAL
}

enum EstadoSuscripcion {
  ACTIVA
  VENCIDA
  CANCELADA
}

model Usuario {
  id            String    @id @default(uuid())
  email         String    @unique
  nombre        String?
  avatar_url    String?
  tipo          TipoUsuario
  created_at    DateTime  @default(now())
  updated_at    DateTime  @updatedAt
  
  empresa       Empresa?
  trabajador    Trabajador?
}

model Empresa {
  id                  String    @id @default(uuid())
  usuario_id          String    @unique
  usuario             Usuario   @relation(fields: [usuario_id], references: [id])
  razon_social        String
  rut                 String    @unique
  giro                String?
  direccion           String
  telefono            String
  contacto_nombre     String
  region              String
  logo_url            String?
  plan                PlanSuscripcion @default(TRIAL)
  fecha_trial_fin     DateTime?
  publicaciones_usadas Int     @default(0)
  busquedas_usadas    Int      @default(0)
  created_at          DateTime  @default(now())
  updated_at          DateTime  @updatedAt
  
  ofertas             Oferta[]
  suscripcion         Suscripcion?
}

model Trabajador {
  id                  String    @id @default(uuid())
  usuario_id          String    @unique
  usuario             Usuario   @relation(fields: [usuario_id], references: [id])
  nombre_completo     String
  rut                 String    @unique
  fecha_nacimiento    DateTime?
  telefono            String
  region              String
  comuna              String
  movilizacion_propia Boolean   @default(false)
  disponibilidad      Json      // { dias: [], horarios: [] }
  pretension_renta    Json      // { min, max, tipo }
  experiencia         Json      // [{ empresa, cargo, periodo, descripcion }]
  certificaciones     Json      // [{ nombre, fecha_emision, url_certificado }]
  created_at          DateTime  @default(now())
  updated_at          DateTime  @updatedAt
  
  postulaciones       Postulacion[]
}

model Oferta {
  id              String      @id @default(uuid())
  empresa_id      String
  empresa         Empresa     @relation(fields: [empresa_id], references: [id])
  titulo          String
  categoria       String      // Guardia, Conserje, Temporero, etc.
  descripcion     String
  region          String
  comuna          String
  fecha_inicio    DateTime
  fecha_fin       DateTime?
  jornada         String      // parcial, full_time, horas
  horario         String?
  remuneration    Json        // { monto, forma_pago }
  tipo_contrato   TipoContrato
  requisitos      Json        // { certificaciones, experiencia_min, movilizacion }
  estado          EstadoOferta @default(ABIERTA)
  score_promedio  Float?
  created_at      DateTime    @default(now())
  updated_at      DateTime    @updatedAt
  
  postulaciones   Postulacion[]
}

model Postulacion {
  id              String              @id @default(uuid())
  oferta_id       String
  oferta          Oferta              @relation(fields: [oferta_id], references: [id])
  trabajador_id  String
  trabajador      Trabajador          @relation(fields: [trabajador_id], references: [id])
  score_match     Float
  mensaje         String?
  estado          EstadoPostulacion   @default(PENDIENTE)
  created_at      DateTime            @default(now())
  updated_at      DateTime            @updatedAt
  
  @@unique([oferta_id, trabajador_id])
}

model Suscripcion {
  id              String            @id @default(uuid())
  empresa_id      String            @unique
  empresa         Empresa           @relation(fields: [empresa_id], references: [id])
  plan            PlanSuscripcion
  fecha_inicio    DateTime
  fecha_fin       DateTime
  estado          EstadoSuscripcion @default(ACTIVA)
  transacciones   Transaccion[]
  created_at      DateTime          @default(now())
}

model Transaccion {
  id              String    @id @default(uuid())
  suscripcion_id  String
  suscripcion     Suscripcion @relation(fields: [suscripcion_id], references: [id])
  monto           Int
  metodo_pago     String    // webpay, transferencia
  estado          String    // pendiente, completada, fallida
  external_id     String?   // ID de WebPay
  created_at      DateTime  @default(now())
}
```

---

## 6. Algoritmo de Matching

```typescript
interface MatchInput {
  oferta: Oferta
  trabajador: Trabajador
}

interface MatchOutput {
  score: number
  certificacionScore: number
  disponibilidadScore: number
  ubicacionScore: number
  rentaScore: number
}

function calculateMatch(oferta: Oferta, trabajador: Trabajador): MatchOutput {
  const requisitos = oferta.requisitos as any
  const disponibilidad = trabajador.disponibilidad as any
  const pretension = trabajador.pretension_renta as any
  
  // Ponderación: 40% cert, 30% disp, 20% ubi, 10% renta
  const certificacionScore = calculateCertificacionScore(requisitos.certificaciones, trabajador.certificaciones)
  const disponibilidadScore = calculateDisponibilidadScore(oferta.jornada, disponibilidad)
  const ubicacionScore = calculateUbicacionScore(oferta.region, oferta.comuna, trabajador.region, trabajador.comuna)
  const rentaScore = calculateRentaScore(requisitos.renta_max, pretension)
  
  const score = (
    certificacionScore * 0.40 +
    disponibilidadScore * 0.30 +
    ubicacionScore * 0.20 +
    rentaScore * 0.10
  )
  
  return { score, certificacionScore, disponibilidadScore, ubicacionScore, rentaScore }
}

function calculateCertificacionScore(reqCerts: string[], workerCerts: any[]): number {
  if (!reqCerts || reqCerts.length === 0) return 100
  if (!workerCerts || workerCerts.length === 0) return 0
  
  const matches = reqCerts.filter(rc => workerCerts.some(wc => wc.nombre === rc))
  return (matches.length / reqCerts.length) * 100
}

function calculateDisponibilidadScore(jornada: string, disponibilidad: any): number {
  // Lógica de comparación de disponibilidad
  // Retorna 0-100
  return 100
}

function calculateUbicacionScore(region: string, comuna: string, workerRegion: string, workerComuna: string): number {
  if (region !== workerRegion) return 0
  if (comuna === workerComuna) return 100
  return 70
}

function calculateRentaScore(rentaMax: number, pretension: any): number {
  if (!pretension || !rentaMax) return 50
  if (pretension.min <= rentaMax) return 100
  if (pretension.min <= rentaMax * 1.2) return 50
  return 0
}
```

---

## 7. Estándares de Código

### TypeScript
- Usar TypeScript strict mode
- Tipar todas las funciones y variables
- Usar interfaces para tipos de dominio
- Evitar `any`, usar `unknown` si es necesario

### Naming
- **Variables/functions:** camelCase (`getOfertas`, `createPostulacion`)
- **Constants:** UPPER_SNAKE_CASE (`MAX_POSTULACIONES`, `API_URL`)
- **Interfaces/Types:** PascalCase (`IOferta`, `MatchResult`)
- **Archivos:** kebab-case (`auth.service.ts`, `ofertas.controller.ts`)

### Componentes React
- Usar functional components con hooks
- Nombrar componentes en PascalCase
- Props con interfaz explícita
- Usar `tsx` para archivos con JSX

### Prisma
- Usar migrations para cambios de schema
- Nombrar models en PascalCase
- Relations con nombres claros

### Git
- Commits en español o inglés técnico
- Formato: `feat:`, `fix:`, `refactor:`, `docs:`
-Branch: `feature/`, `fix/`, `hotfix/`

---

## 8. Reglas de Negocio

| Regla | Descripción |
|-------|-------------|
| R1 | Empresa solo puede ver workers que cumplen requisitos mínimos |
| R2 | Worker solo ve ofertas compatibles con su perfil (score > 50%) |
| R3 | Una oferta puede tener máximo 50 postulaciones |
| R4 | Empresa tiene 7 días para responder postulación |
| R5 | Si oferta expira sin selección, se notifica a postulantes |
| R6 | Worker puede retractar postulación antes de aceptación |
| R7 | Certificación OS10 solo aplica a categoría "Guardia" |
| R8 | Certificación SEC solo aplica a categoría "Electricista" |

---

## 9. API Endpoints

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/auth/sign-in` | Login con Google | Público |
| POST | `/auth/sign-up` | Registro usuario | Público |
| POST | `/auth/sign-out` | Logout | Requiere Auth |
| GET | `/auth/me` | Obtener usuario actual | Requiere Auth |
| POST | `/empresas` | Crear perfil empresa | Requiere Auth |
| GET | `/empresas/:id` | Ver perfil empresa | Requiere Auth |
| PUT | `/empresas/:id` | Actualizar perfil | Requiere Auth |
| POST | `/trabajadores` | Crear perfil worker | Requiere Auth |
| GET | `/trabajadores/:id` | Ver perfil worker | Requiere Auth |
| PUT | `/trabajadores/:id` | Actualizar perfil | Requiere Auth |
| GET | `/trabajadores/compatibles` | Ver workers compatibles | Requiere Auth |
| POST | `/ofertas` | Crear oferta | Requiere Auth (Empresa) |
| GET | `/ofertas` | Listar ofertas (filtros) | Requiere Auth |
| GET | `/ofertas/:id` | Ver oferta específica | Requiere Auth |
| PUT | `/ofertas/:id` | Actualizar oferta | Requiere Auth |
| DELETE | `/ofertas/:id` | Eliminar oferta | Requiere Auth |
| POST | `/ofertas/:id/matching` | Ejecutar matching | Sistema |
| GET | `/ofertas/:id/matches` | Ver matches de oferta | Requiere Auth |
| POST | `/postulaciones` | Postular a oferta | Requiere Auth (Worker) |
| GET | `/postulaciones` | Ver mis postulaciones | Requiere Auth |
| PUT | `/postulaciones/:id/accept` | Aceptar worker | Requiere Auth (Empresa) |
| PUT | `/postulaciones/:id/reject` | Rechazar worker | Requiere Auth (Empresa) |
| GET | `/suscripcion` | Ver suscripción actual | Requiere Auth |
| POST | `/suscripcion/upgrade` | Cambiar plan | Requiere Auth |
| POST | `/pagos/webpay/init` | Iniciar pago WebPay | Requiere Auth |
| POST | `/pagos/webpay/confirm` | Confirmar pago WebPay | WebPay Callback |

---

## 10. Commands Útiles

```bash
# Desarrollo local (Frontend)
npm run dev

# Prisma
npx prisma generate
npx prisma migrate dev
npx prisma studio

# Build
npm run build

# Linting
npm run lint

# Tests
npm run test
```

---

## 11. Definition of Done

Una tarea se considera completada cuando:
- [ ] Código desarrollado
- [ ] Tests unitarios написаны
- [ ] Peer review aprobado
- [ ] Desplegado a staging
- [ ] QA aprobado

---

## 12. Contacto / Contexto

**Product Owner:**
**Proyecto:** Match&Go - Plataforma de trabajos ocasionales Chile
**Stack:** Next.js + Supabase + Prisma + Vercel

Para dudas o decisiones de arquitectura, consultar este documento o al Product Owner.

