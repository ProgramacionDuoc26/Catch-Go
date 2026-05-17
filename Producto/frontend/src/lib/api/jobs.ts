import { api } from './client';
import type { Oferta, CreateOfertaRequest } from './types';

const BASE = process.env.NEXT_PUBLIC_JOBS_SERVICE_URL ?? 'http://localhost:8083';

export const jobsApi = {
  /**
   * Listar todas las ofertas
   * GET /jobs
   */
  list: () =>
    api.get<Oferta[]>(`${BASE}/jobs`),

  /**
   * Crear una nueva oferta
   * POST /jobs
   */
  create: (body: CreateOfertaRequest) =>
    api.post<Oferta>(`${BASE}/jobs`, body),

  /**
   * Obtener oferta por ID
   * GET /jobs/:id  (pendiente implementación backend)
   */
  getById: (id: string) =>
    api.get<Oferta>(`${BASE}/jobs/${id}`),

  /**
   * Postular a una oferta
   * POST /jobs/:id/apply?userId=:userId
   */
  apply: (jobId: string, userId: string) =>
    api.post<void>(`${BASE}/jobs/${jobId}/apply?userId=${userId}`, {}),

  /**
   * Obtener postulaciones por usuario
   * GET /jobs/applications/user/:userId
   */
  getApplicationsByUserId: (userId: string) =>
    api.get<any[]>(`${BASE}/jobs/applications/user/${userId}`),

  /**
   * Obtener postulaciones por empresa (empleador)
   * GET /jobs/applications/employer/:employerId
   */
  getApplicationsByEmployerId: (employerId: string) =>
    api.get<any[]>(`${BASE}/jobs/applications/employer/${employerId}`),

  /**
   * Actualizar una oferta
   * PUT /jobs/:id
   */
  update: (id: string, data: Partial<CreateOfertaRequest>) =>
    api.put<Oferta>(`${BASE}/jobs/${id}`, data),

  /**
   * Eliminar una oferta
   * DELETE /jobs/:id
   */
  delete: (id: string) =>
    api.delete<void>(`${BASE}/jobs/${id}`),

  /**
   * Eliminar una postulación
   * DELETE /jobs/applications/:id
   */
  deleteApplication: (id: string) =>
    api.delete<void>(`${BASE}/jobs/applications/${id}`),

  /**
   * Actualizar estado de una postulación
   * PUT /jobs/applications/:id/status
   */
  updateApplicationStatus: (id: string, status: string) =>
    api.put<void>(`${BASE}/jobs/applications/${id}/status`, { status }),

  /**
   * Actualizar estado de una oferta (ABIERTA, PAUSADA, COMPLETADA)
   * PUT /jobs/:id/status
   */
  updateStatus: (id: string, status: string) =>
    api.put<void>(`${BASE}/jobs/${id}/status`, { status }),

  /**
   * Obtener todas las postulaciones del sistema (para el admin)
   * GET /jobs/applications
   */
  getAllApplications: () =>
    api.get<any[]>(`${BASE}/jobs/applications`),
};
