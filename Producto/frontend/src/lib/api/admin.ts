import { api } from './client';
import { Profile } from './profile';
import { Oferta } from './types';

const PROFILE_BASE = process.env.NEXT_PUBLIC_PROFILE_SERVICE_URL || 'http://localhost:8082';
const JOBS_BASE = process.env.NEXT_PUBLIC_JOBS_SERVICE_URL || 'http://localhost:8083';

export interface AdminStats {
  totalWorkers: number;
  totalCompanies: number;
  totalOffers: number;
  totalApplications: number;
  pendingApplications: number;
  premiumWorkers: number;
  premiumCompanies: number;
  recentProfiles: Profile[];
}

export const adminApi = {
  getStats: async (): Promise<AdminStats> => {
    // Fetch all profiles
    const profilesRes = await api.get<Profile[]>(`${PROFILE_BASE}/profiles`);
    const profiles = profilesRes.data || [];
    
    // Fetch all jobs
    const jobsRes = await api.get<Oferta[]>(`${JOBS_BASE}/jobs`);
    const jobs = jobsRes.data || [];

    // Fetch all applications
    const appsRes = await api.get<any[]>(`${JOBS_BASE}/jobs/applications`);
    const applications = appsRes.data || [];

    const workers = profiles.filter(p => p.type === 'TRABAJADOR');
    const companies = profiles.filter(p => p.type === 'EMPRESA');
    
    const premiumWorkers = workers.filter(p => p.plan === 'PREMIUM' || p.plan === 'ENTERPRISE').length;
    const premiumCompanies = companies.filter(p => p.plan === 'PREMIUM' || p.plan === 'ENTERPRISE').length;

    return {
      totalWorkers: workers.length,
      totalCompanies: companies.length,
      totalOffers: jobs.length,
      totalApplications: applications.length,
      pendingApplications: applications.filter(a => a.estado === 'PENDIENTE').length,
      premiumWorkers,
      premiumCompanies,
      recentProfiles: profiles.slice(-5).reverse()
    };
  },

  getAdmins: async (): Promise<Profile[]> => {
    const res = await api.get<Profile[]>(`${PROFILE_BASE}/profiles`);
    return (res.data || []).filter(p => p.type === 'ADMIN' || p.type === 'SUB_ADMIN' || p.type === 'FULL_ADMIN');
  },

  createAdmin: async (adminData: any) => {
    // 1. Create in Auth Service
    const authRes = await api.post<any>(`http://localhost:8081/auth/register`, {
      email: adminData.email,
      password: adminData.password,
      nombre: adminData.name,
      tipo: adminData.tipo || 'ADMIN',
      telefono: adminData.phone || ''
    });

    if (!authRes.data || !authRes.data.usuario) {
      throw new Error(authRes.error || 'Error al crear cuenta de autenticación');
    }

    // 2. Profile
    const profileData = {
      userId: authRes.data.usuario.id.toString(),
      name: adminData.name,
      email: adminData.email,
      phone: adminData.phone || '',
      birthDate: '1990-01-01',
      type: adminData.tipo || 'ADMIN',
      description: adminData.description || 'Administrador del Sistema',
      latitude: -33.4489,
      longitude: -70.6693
    };

    return api.post(`${PROFILE_BASE}/profiles`, profileData);
  },

  deleteAdmin: async (userId: string) => {
    // We should ideally delete from both Auth and Profile
    // but most systems prioritize Auth
    await api.delete(`http://localhost:8081/auth/user/${userId}`);
    return api.delete(`${PROFILE_BASE}/profiles/${userId}`);
  }
};
