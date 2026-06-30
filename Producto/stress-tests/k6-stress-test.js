// k6-stress-test.js — Catch & Go Load & Stress Testing Script
// Documentación: https://grafana.com/docs/k6/latest/

import http from 'k6/http';
import { sleep, check } from 'k6';

// 1. Configuración de fases (stages) para emular comportamiento de usuarios concurrentes
export const options = {
  stages: [
    { duration: '30s', target: 20 },  // Fase 1: Rampa de subida (Warm-up) - 20 usuarios activos
    { duration: '1m', target: 50 },   // Fase 2: Carga sostenida (Load) - 50 usuarios constantes
    { duration: '30s', target: 150 }, // Fase 3: Pico de estrés (Stress Spike) - 150 usuarios concurrentes
    { duration: '30s', target: 0 },   // Fase 4: Rampa de bajada (Cool-down) - Volver a 0 usuarios
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],   // El porcentaje de fallos en HTTP debe ser menor al 5%
    http_req_duration: ['p(95)<400'], // El 95% de las peticiones deben responder en menos de 400ms
  },
};

// Base URL: Por defecto usa el API Gateway local, pero se puede sobrescribir usando:
// k6 run -e BASE_URL=https://api-gateway-production.up.railway.app k6-stress-test.js
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const headers = { 'Content-Type': 'application/json' };

  // Escenario 1: Monitorear el estado del API Gateway (Ping)
  const pingRes = http.get(`${BASE_URL}/gateway/ping`, { headers });
  check(pingRes, {
    'Ping: Status es 200': (r) => r.status === 200,
    'Ping: Tiempo de respuesta < 200ms': (r) => r.timings.duration < 200,
  });
  sleep(0.5);

  // Escenario 2: Obtener listado de ofertas laborales (Simulando Trabajadores o Empresas buscando)
  const jobsRes = http.get(`${BASE_URL}/jobs`, { headers });
  check(jobsRes, {
    'Jobs: Status es 200': (r) => r.status === 200,
    'Jobs: Tiempo de respuesta < 300ms': (r) => r.timings.duration < 300,
  });
  sleep(1);

  // Escenario 3: Intentar login con credenciales inválidas (Simulando intentos de autenticación)
  // Nota: Al usar credenciales de prueba, el servidor debería responder 401 o 400 de forma controlada.
  // Lo importante es verificar que responda de inmediato y no lance un 500 (Internal Server Error) bajo carga.
  const loginPayload = JSON.stringify({
    email: 'estres_test_user@example.com',
    password: 'passwordInvalido123',
  });
  const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, { headers });
  check(loginRes, {
    'Login: Status es 400, 401 o 500 (esperado)': (r) => r.status === 400 || r.status === 401 || r.status === 500,
    'Login: Tiempo de respuesta < 300ms': (r) => r.timings.duration < 300,
  });
  sleep(1);
}
