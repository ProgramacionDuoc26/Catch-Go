# 🚀 Guía de Deploy: Catch&Go en Railway

## Estructura de servicios desplegados

```
Railway Project: "catch-go"
├── 📦 frontend          → catch-go-production.up.railway.app
├── 📦 api-gateway       → api-gateway-production.up.railway.app (público)
├── 📦 auth-service      → internal only
├── 📦 profile-service   → internal only
├── 📦 jobs-service      → internal only
├── 📦 matching-service  → internal only
├── 📦 notification-service → internal only
├── 🗄️ PostgreSQL        → plugin Railway
└── 🗄️ Redis             → plugin Railway
```

---

## Paso 1: Crear el proyecto en Railway

1. Ir a [railway.app](https://railway.app) → **New Project**
2. Seleccionar **Deploy from GitHub repo**
3. Conectar el repositorio: `Nicolasiturrieta/Catch-Go`
4. Seleccionar la rama: `feature/init-frontend`

---

## Paso 2: Agregar Plugins (base de datos)

Dentro del proyecto Railway:

1. **+ New** → **Database** → **PostgreSQL**
   - Railway lo provisiona automáticamente
   - Guarda las variables que aparecen (PGHOST, PGPORT, etc.)

2. **+ New** → **Database** → **Redis**
   - Railway lo provisiona automáticamente
   - Guarda las variables (REDIS_HOST, REDIS_PORT, etc.)

---

## Paso 3: Crear los servicios Java

Para **cada microservicio** hacer lo mismo:

1. **+ New** → **GitHub Repo** → Seleccionar el repo
2. En **Settings → Build**:
   - **Root Directory:** `Producto/backend`
   - **Dockerfile Path:** `nombre-servicio/Dockerfile`
     - Ejemplo para auth: `auth-service/Dockerfile`
3. En **Settings → Variables**: pegar las variables del archivo `.env.railway.example`
4. En **Settings → Networking** (solo api-gateway): **Generate Domain** para obtener la URL pública
5. Verificar que **Dockerfile Path** apunte al archivo del servicio dentro de `Producto/backend`
   - Ejemplo: `auth-service/Dockerfile`
   - No usar el `Dockerfile` raíz de `Producto/backend` en servicios individuales

**Orden recomendado de creación:**
1. `auth-service`
2. `profile-service`
3. `jobs-service`
4. `matching-service`
5. `notification-service`
6. `api-gateway` (último, depende de todos)

---

## Paso 4: Crear el servicio Frontend

1. **+ New** → **GitHub Repo** → Seleccionar el repo
2. En **Settings → Build**:
   - **Root Directory:** `Producto/frontend`
   - **Dockerfile Path:** `Dockerfile`
3. En **Settings → Variables**: pegar las variables del frontend del `.env.railway.example`
   - Asegurarse de que `NEXT_PUBLIC_API_URL` apunte al dominio público del `api-gateway`
   - Dejar `NEXT_PUBLIC_NOTIFICATION_SERVICE_URL` con la misma URL del gateway salvo que decidas exponer `notification-service` por separado
4. En **Settings → Networking**: **Generate Domain** para obtener la URL pública del frontend

---

## Paso 5: Actualizar Supabase

En el dashboard de Supabase → **Authentication** → **URL Configuration**:

- **Site URL:** `https://catch-go-production.up.railway.app`
- **Redirect URLs** → agregar:
  - `https://catch-go-production.up.railway.app/auth/callback`
  - `https://catch-go-production.up.railway.app/auth/role-selection`

---

## Paso 6: Verificación

```bash
# Instalar Railway CLI
npm install -g @railway/cli

# Login
railway login

# Ver logs de un servicio
railway logs --service frontend
railway logs --service auth-service
```

Verificar que todos los servicios aparezcan en estado **🟢 Running** en el dashboard de Railway.

---

## Variables de entorno por servicio

Ver el archivo [.env.railway.example](/.env.railway.example) en la raíz del repositorio.

---

## Costos estimados Railway (Hobby Plan - $5/mes)

| Servicio | RAM estimada | CPU |
|---|---|---|
| frontend (Next.js) | ~256MB | Bajo |
| api-gateway | ~512MB | Bajo-Medio |
| auth-service | ~512MB | Bajo |
| profile-service | ~512MB | Bajo |
| jobs-service | ~512MB | Bajo |
| matching-service | ~512MB | Bajo-Medio |
| notification-service | ~256MB | Bajo |
| PostgreSQL | ~256MB | Bajo |
| Redis | ~64MB | Bajo |

> El plan Hobby de $5/mes incluye 8GB RAM y 100GB de transferencia, suficiente para esta arquitectura en desarrollo/staging.
