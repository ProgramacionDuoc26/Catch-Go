import { api } from './client';

const BASE = (process.env.NEXT_PUBLIC_PROFILE_SERVICE_URL || 'http://localhost:8082') + '/profiles';

export interface Profile {
  id?: number;
  userId: string;
  name: string;
  email: string;
  phone: string;
  birthDate: string;
  photoUrl?: string;
  cvUrl?: string;
  description?: string;
  bankName?: string;
  accountType?: string;
  accountNumber?: string;
  type: 'TRABAJADOR' | 'EMPRESA';
}

export const profileApi = {
  /**
   * Obtener perfil por ID de usuario
   */
  getByUserId: (userId: string) =>
    api.get<Profile>(`${BASE}/user/${userId}`),

  /**
   * Guardar o actualizar perfil
   */
  save: (data: Profile) =>
    api.post<Profile>(BASE, data),
};
