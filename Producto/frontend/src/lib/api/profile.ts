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
  type: 'TRABAJADOR' | 'EMPRESA' | 'ADMIN' | 'SUB_ADMIN' | 'FULL_ADMIN';
  latitude?: number;
  longitude?: number;
  skills?: string;
  rut?: string;
  address?: string;
  representativeName?: string;
  bankAddress?: string;
  rating?: number;
  ratingCount?: number;
  plan?: string;
  planExpiry?: string;
  certificateUrl?: string;
}

export function sanitizeUrl(url: string | undefined): string | undefined {
  if (!url) return url;
  
  // Reemplazar la IP del emulador Android (10.0.2.2) por el host del servicio de perfil
  // (por defecto, http://localhost:8082 o el configurado en variables de entorno)
  const targetHost = process.env.NEXT_PUBLIC_PROFILE_SERVICE_URL 
    ? process.env.NEXT_PUBLIC_PROFILE_SERVICE_URL.replace(/\/$/, '') 
    : 'http://localhost:8082';

  if (url.includes('10.0.2.2:8082')) {
    return url.replace(/http:\/\/10\.0\.2\.2:8082/g, targetHost);
  }
  return url.replace(/10\.0\.2\.2/g, 'localhost');
}

export function sanitizeProfile(profile: Profile | null): Profile | null {
  if (!profile) return null;
  
  let sanitizedSkills = profile.skills;
  if (profile.skills && profile.skills.startsWith('{')) {
    try {
      const skillsObj = JSON.parse(profile.skills);
      if (skillsObj.certificateUrl) {
        skillsObj.certificateUrl = sanitizeUrl(skillsObj.certificateUrl);
      }
      sanitizedSkills = JSON.stringify(skillsObj);
    } catch (e) {
      console.error('Error sanitizing skills JSON:', e);
    }
  }

  return {
    ...profile,
    photoUrl: sanitizeUrl(profile.photoUrl),
    cvUrl: sanitizeUrl(profile.cvUrl),
    certificateUrl: sanitizeUrl(profile.certificateUrl),
    skills: sanitizedSkills,
  };
}

export const profileApi = {
  /**
   * Obtener perfil por ID de usuario
   */
  getByUserId: async (userId: string) => {
    const res = await api.get<Profile>(`${BASE}/user/${userId}`);
    if (res.data) {
      res.data = sanitizeProfile(res.data) as Profile;
    }
    return res;
  },

  /**
   * Guardar o actualizar perfil
   */
  save: async (data: Profile) => {
    const res = await api.post<Profile>(BASE, data);
    if (res.data) {
      res.data = sanitizeProfile(res.data) as Profile;
    }
    return res;
  },

  /**
   * Eliminar perfil por ID de usuario
   */
  deleteProfile: (userId: string) =>
    api.delete<void>(`${BASE}/user/${userId}`),
};
