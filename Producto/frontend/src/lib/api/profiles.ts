import { api } from './client';
import type { Trabajador, Empresa } from './types';

const BASE = process.env.NEXT_PUBLIC_PROFILE_SERVICE_URL ?? 'http://localhost:8082';

export const profilesApi = {
  /**
   * Listar todos los perfiles
   * GET /profiles
   */
  list: () =>
    api.get<(Trabajador | Empresa)[]>(`${BASE}/profiles`),

  /**
   * Crear perfil de empresa
   * POST /profiles
   */
  createEmpresa: (body: Omit<Empresa, 'id'>) =>
    api.post<Empresa>(`${BASE}/profiles`, { ...body, tipo: 'EMPRESA' }),

  /**
   * Crear perfil de trabajador
   * POST /profiles
   */
  createTrabajador: (body: Omit<Trabajador, 'id'>) =>
    api.post<Trabajador>(`${BASE}/profiles`, { ...body, tipo: 'TRABAJADOR' }),

  /**
   * Obtener perfil por ID
   * GET /profiles/:id  (pendiente implementación backend)
   */
  getById: (id: string) =>
    api.get<Trabajador | Empresa>(`${BASE}/profiles/${id}`),
};
