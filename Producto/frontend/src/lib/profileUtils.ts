import { Profile } from './api/profile';

export function calculateProfileCompletion(profile: Profile | null | undefined): number {
  if (!profile) return 0;
  
  let score = 0;
  let totalWeight = 0;

  const addScore = (condition: boolean, weight: number) => {
    totalWeight += weight;
    if (condition) score += weight;
  };

  // Datos Básicos
  addScore(!!profile.name && profile.name.trim() !== '', 10);
  addScore(!!profile.email && profile.email.trim() !== '', 10);
  addScore(!!profile.phone && profile.phone.trim() !== '' && profile.phone !== '+56 ', 10);
  
  // Archivos
  addScore(!!profile.photoUrl, 10);
  addScore(!!profile.cvUrl, 15);

  // Ubicación
  addScore(!!profile.latitude && !!profile.longitude, 10);

  // Datos Bancarios (Todos o nada)
  const hasBank = !!profile.bankName && !!profile.accountType && !!profile.accountNumber;
  addScore(hasBank, 15);

  // Skills / Preferencias
  let hasSkills = false;
  try {
    if (profile.skills) {
      const parsed = typeof profile.skills === 'string' ? JSON.parse(profile.skills) : profile.skills;
      if (
        (parsed.habilidades && parsed.habilidades.length > 0) ||
        parsed.ambiente || 
        parsed.caracteristica || 
        parsed.preferencia ||
        parsed.giro ||
        parsed.tipoTrabajador ||
        parsed.habilidadValorada ||
        parsed.ritmo
      ) {
        hasSkills = true;
      }
    }
  } catch (e) {
    // ignore
  }
  addScore(hasSkills, 20);

  // Normalizar a 100% (por si los pesos suman diferente)
  return Math.min(100, Math.round((score / totalWeight) * 100));
}
