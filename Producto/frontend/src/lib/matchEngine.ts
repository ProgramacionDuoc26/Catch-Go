// matchEngine.ts

export interface MatchScore {
  total: number;
  breakdown: {
    habilidades: number; // Max 40
    experiencia: number; // Max 20
    distancia: number;   // Max 20
    disponibilidad: number; // Max 20
    calificacion: number; // Bonus Max 10
  };
}

// Helpers
function calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
  if (!lat1 || !lon1 || !lat2 || !lon2) return -1;
  const R = 6371; // km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
    Math.sin(dLon/2) * Math.sin(dLon/2); 
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
  return R * c;
}

/**
 * Calcula el puntaje de compatibilidad entre un trabajador y una oferta/empresa.
 * @param workerProfile  Perfil del trabajador
 * @param companyProfile Perfil de la empresa
 * @param jobOffer       Oferta de trabajo
 * @param callerPlan     Plan del usuario que realiza la búsqueda ('FREE' | 'PREMIUM' | 'ENTERPRISE')
 */
export function calculateMatchScore(workerProfile: any, companyProfile: any, jobOffer: any, callerPlan: string = 'FREE'): MatchScore {
  const isPremium = callerPlan === 'PREMIUM' || callerPlan === 'ENTERPRISE';

  let score: MatchScore = {
    total: 0,
    breakdown: { habilidades: 0, experiencia: 0, distancia: 0, disponibilidad: 0, calificacion: 0 }
  };

  // Safe parsing of skills JSON
  const wSkills = workerProfile?.skills ? (typeof workerProfile.skills === 'string' ? JSON.parse(workerProfile.skills) : workerProfile.skills) : {};
  const cSkills = companyProfile?.skills ? (typeof companyProfile.skills === 'string' ? JSON.parse(companyProfile.skills) : companyProfile.skills) : {};

  // 1. HABILIDADES (40%)
  let habScore = 0;
  
  // A. Característica (Trabajador) vs Habilidad Valorada (Empresa) [Max 20%]
  const wCaract = wSkills.caracteristica || '';
  const cHabil = cSkills.habilidadValorada || '';
  
  const charactMap: Record<string, string> = {
    'Responsable': 'Responsabilidad',
    'Rápido': 'Rapidez',
    'Líder': 'Liderazgo',
    'Comunicativo': 'Comunicación',
    'Analítico': 'Resolución de problemas' // Proxy
  };

  if (wCaract && cHabil) {
    if (charactMap[wCaract] === cHabil || wCaract === cHabil) {
      habScore += 20;
    } else {
      habScore += 10; // Partial match just for having something
    }
  }

  // B. Ambiente (Trabajador) vs Ritmo (Empresa) [Max 20%]
  const wAmbiente = wSkills.ambiente || '';
  const cRitmo = cSkills.ritmo || '';
  
  if (wAmbiente && cRitmo) {
    if (wAmbiente === cRitmo) {
      habScore += 20;
    } else if (
      (wAmbiente === 'Alta presión' && cRitmo === 'Muy dinámico') ||
      (wAmbiente === 'Trabajo en equipo' && cRitmo === 'Estructurado')
    ) {
      habScore += 15;
    } else {
      habScore += 5;
    }
  } else if (wSkills.habilidades && wSkills.habilidades.length > 0) {
     // Fallback: If they selected skills, give them some points
     habScore += 10;
  }
  
  score.breakdown.habilidades = Math.min(40, habScore);

  // 2. EXPERIENCIA (20%)
  // Since we don't have an explicit experience field, we check description length/keywords
  let expScore = 0;
  const desc = (workerProfile?.description || '').toLowerCase();
  if (desc.length > 10) expScore += 10;
  if (desc.includes('experiencia') || desc.includes('años') || desc.includes('trabajé')) {
    expScore += 10;
  }
  score.breakdown.experiencia = expScore;

  // 3. DISTANCIA (20%) — Diferenciada por plan
  // FREE:    ≤5 km = 20 pts | >5 km = 0 pts (fuera de rango gratuito)
  // PREMIUM: ≤5 km = 20 pts | ≤10 km = 15 pts | ≤15 km = 10 pts | >15 km = 5 pts
  let distScore = 0;
  const targetLat = jobOffer?.latitude || companyProfile?.latitude;
  const targetLng = jobOffer?.longitude || companyProfile?.longitude;
  
  const dist = calculateDistance(workerProfile?.latitude, workerProfile?.longitude, targetLat, targetLng);
  
  if (dist === -1) {
    distScore = 10; // Default if no coords available
  } else if (isPremium) {
    // PREMIUM: escalonado completo hasta 15 km
    if (dist <= 5) {
      distScore = 20;
    } else if (dist <= 10) {
      distScore = 15;
    } else if (dist <= 15) {
      distScore = 10;
    } else {
      distScore = 5;
    }
  } else {
    // FREE: solo puntaje dentro de 5 km, fuera = 0
    if (dist <= 5) {
      distScore = 20;
    } else {
      distScore = 0;
    }
  }
  score.breakdown.distancia = distScore;

  // 4. DISPONIBILIDAD (20%)
  let dispScore = 0;
  const wPref = wSkills.preferencia || '';
  // If the job description or title implies part time / full time, we can match
  const jobTitleDesc = ((jobOffer?.titulo || '') + ' ' + (jobOffer?.descripcion || '')).toLowerCase();
  
  if (wPref) {
    if (jobTitleDesc.includes(wPref.toLowerCase())) {
      dispScore = 20;
    } else {
      dispScore = 15; // They provided availability, assume partial match
    }
  } else {
    dispScore = 10; // Default
  }
  score.breakdown.disponibilidad = dispScore;

  // 5. CALIFICACIÓN (BONUS +10)
  // Utilizamos calificación real del perfil
  const workerRating = workerProfile?.rating || 0;
  let califScore = 0;
  if (workerRating >= 4.8) {
    califScore = 10;
  } else if (workerRating >= 4.5) {
    califScore = 7;
  } else if (workerRating >= 4.0) {
    califScore = 4;
  }
  score.breakdown.calificacion = califScore;

  // Total base
  score.total = score.breakdown.habilidades + score.breakdown.experiencia + score.breakdown.distancia + score.breakdown.disponibilidad + score.breakdown.calificacion;
  
  // PREMIUM BONUS: +20% al puntaje total
  if (isPremium) {
    score.total = Math.round(score.total * 1.20);
  }

  // Ensure it doesn't exceed 100
  score.total = Math.min(100, Math.round(score.total));

  return score;
}
