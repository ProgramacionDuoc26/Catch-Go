import { api } from './client';
import { getServiceBaseUrl } from './base';
import type { MatchSuggestion } from './types';

const BASE = getServiceBaseUrl('NEXT_PUBLIC_MATCHING_SERVICE_URL', 'http://localhost:8084');

export const matchingApi = {
  /**
   * Obtener todas las sugerencias de match
   * GET /matching
   */
  list: () =>
    api.get<MatchSuggestion[]>(`${BASE}/matching`),

  /**
   * Crear/ejecutar matching para una oferta
   * POST /matching
   */
  run: (body: { oferta_id: string }) =>
    api.post<MatchSuggestion>(`${BASE}/matching`, body),
};
