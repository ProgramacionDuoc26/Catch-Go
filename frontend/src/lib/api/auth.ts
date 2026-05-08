import { api } from './client';
import type { LoginRequest, RegisterRequest, AuthResponse } from './types';

const BASE = process.env.NEXT_PUBLIC_AUTH_SERVICE_URL ?? 'http://localhost:8081';

export const authApi = {
  /**
   * Registrar un nuevo usuario (EMPRESA o TRABAJADOR)
   * POST /auth/register
   */
  register: (body: RegisterRequest) =>
    api.post<AuthResponse>(`${BASE}/auth/register`, body),

  /**
   * Iniciar sesión
   * POST /auth/login  (pendiente implementación backend)
   */
  login: (body: LoginRequest) =>
    api.post<AuthResponse>(`${BASE}/auth/login`, body),

  /**
   * Obtener todos los usuarios (solo desarrollo/admin)
   * GET /auth/register
   */
  listAll: () =>
    api.get<AuthResponse[]>(`${BASE}/auth/register`),

  /**
   * Obtener usuario por ID
   * GET /auth/user/:id
   */
  getUserById: (id: string) =>
    api.get<any>(`${BASE}/auth/user/${id}`),
};
