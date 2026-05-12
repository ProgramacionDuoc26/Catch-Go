/**
 * Tipos TypeScript alineados con el modelo de negocio de agent.md.
 * Se actualizarán cuando los DTOs Java sean completados por el equipo backend.
 */

// ── Auth ──────────────────────────────────────────────
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  nombre: string;
  tipo: 'EMPRESA' | 'TRABAJADOR';
  telefono: string;
}

export interface AuthResponse {
  token: string;
  usuario: Usuario;
}

// ── Usuario ───────────────────────────────────────────
export interface Usuario {
  id: string | number;
  email: string;
  nombre: string;
  tipo: 'EMPRESA' | 'TRABAJADOR';
  telefono?: string;
  avatar_url?: string;
  created_at: string;
}

// ── Empresa ───────────────────────────────────────────
export interface Empresa {
  id: string;
  usuario_id: string;
  razon_social: string;
  rut: string;
  giro?: string;
  direccion: string;
  telefono: string;
  region: string;
  logo_url?: string;
  plan: 'TRIAL' | 'BASICO' | 'PROFESIONAL' | 'ENTERPRISE';
}

// ── Trabajador ────────────────────────────────────────
export interface Trabajador {
  id: string;
  usuario_id: string;
  nombre_completo: string;
  rut: string;
  telefono: string;
  region: string;
  comuna: string;
  movilizacion_propia: boolean;
  disponibilidad: { dias: string[]; horarios: string[] };
  pretension_renta: { min: number; max: number; tipo: string };
}

// ── Oferta (Jobs) ─────────────────────────────────────
export interface Oferta {
  id: string;
  empresaId: string;
  titulo: string;
  categoria: string;
  descripcion: string;
  ubicacion: string;
  fechaInicio: string;
  fechaFin?: string;
  remuneracion: number;
  estado: 'ABIERTA' | 'CERRADA' | 'CON_CANDIDATOS' | 'COMPLETADA';
  latitude?: number;
  longitude?: number;
}

export interface CreateOfertaRequest {
  titulo: string;
  categoria: string;
  descripcion: string;
  ubicacion: string;
  fechaInicio: string;
  fechaFin?: string;
  remuneracion: number;
  empresaId: string;
  latitude?: number;
  longitude?: number;
}

// ── Matching ──────────────────────────────────────────
export interface MatchSuggestion {
  id: string;
  oferta_id: string;
  trabajador_id: string;
  score: number;
  certificacion_score: number;
  disponibilidad_score: number;
  ubicacion_score: number;
  renta_score: number;
}

// ── Postulacion ───────────────────────────────────────
export interface Postulacion {
  id: string;
  oferta_id: string;
  trabajador_id: string;
  score_match: number;
  mensaje?: string;
  estado: 'PENDIENTE' | 'ACEPTADO' | 'RECHAZADO';
  created_at: string;
}
