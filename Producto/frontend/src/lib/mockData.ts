// src/lib/mockData.ts

export const mockOfertas = [
  {
    id: '1',
    titulo: 'Guardia de Seguridad Turno Noche',
    empresa: 'Seguridad Integral SpA',
    categoria: 'Guardia',
    ubicacion: 'Providencia, RM',
    fechaInicio: '2026-05-01',
    fechaFin: '2026-05-05',
    remuneracion: 35000,
    estado: 'ABIERTA',
    postulantes: 12,
    descripcion: 'Se necesita guardia de seguridad con curso OS10 al día para cubrir turno de noche en condominio residencial.',
    requisitos: ['Curso OS10 Vigente', 'Experiencia previa de 1 año', 'Zapatos de seguridad']
  },
  {
    id: '2',
    titulo: 'Garzón para Evento Corporativo',
    empresa: 'Eventos Premium',
    categoria: 'Garzón',
    ubicacion: 'Las Condes, RM',
    fechaInicio: '2026-05-10',
    fechaFin: '2026-05-10',
    remuneracion: 45000,
    estado: 'ABIERTA',
    postulantes: 5,
    descripcion: 'Se requiere garzón para evento corporativo de 6 horas. Se entrega uniforme (excepto pantalón negro y zapatos negros).',
    requisitos: ['Experiencia en atención a mesas', 'Excelente presentación personal']
  },
  {
    id: '3',
    titulo: 'Operario de Bodega Fin de Semana',
    empresa: 'Logística Rápida',
    categoria: 'Carga/Descarga',
    ubicacion: 'Quilicura, RM',
    fechaInicio: '2026-05-15',
    fechaFin: '2026-05-16',
    remuneracion: 40000,
    estado: 'CON_CANDIDATOS',
    postulantes: 28,
    descripcion: 'Apoyo en armado de pallets y descarga de camiones durante el fin de semana.',
    requisitos: ['Zapatos de seguridad', 'Salud física para trabajo pesado']
  }
];

export const mockCandidatos = [
  {
    id: 'c1',
    nombre: 'Juan Pérez',
    score: 95,
    estado: 'PENDIENTE',
    certificaciones: ['OS10 Vigente', 'Primeros Auxilios'],
    experiencia: '3 años',
    ofertaPostulada: 'Guardia de Seguridad Turno Noche'
  },
  {
    id: 'c2',
    nombre: 'María González',
    score: 88,
    estado: 'ACEPTADO',
    certificaciones: ['Manipulación de Alimentos'],
    experiencia: '5 años',
    ofertaPostulada: 'Garzón para Evento Corporativo'
  },
  {
    id: 'c3',
    nombre: 'Carlos Rojas',
    score: 72,
    estado: 'RECHAZADO',
    certificaciones: [],
    experiencia: '1 año',
    ofertaPostulada: 'Operario de Bodega Fin de Semana'
  }
];

export const mockPerfilEmpresa = {
  razonSocial: 'Seguridad Integral SpA',
  rut: '76.543.210-K',
  giro: 'Servicios de Seguridad y Vigilancia',
  direccion: 'Av. Providencia 1234, Of 56',
  telefono: '+56 9 1234 5678',
  contactoNombre: 'Roberto Sánchez',
  planActual: 'PROFESIONAL',
  publicacionesRestantes: 8,
};

export const mockPerfilTrabajador = {
  nombre: 'Juan Pérez',
  rut: '15.123.456-7',
  telefono: '+56 9 8765 4321',
  region: 'Metropolitana',
  comuna: 'Maipú',
  certificaciones: ['OS10 Vigente', 'Primeros Auxilios'],
  experiencia: '3 años como guardia en condominios y eventos masivos.',
  disponibilidad: 'Turnos de noche, Fines de semana',
};
