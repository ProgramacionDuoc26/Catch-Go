"use client";

import React, { useState, useEffect } from 'react';
import { Card, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { User, Check, X, Loader2, Briefcase, Map as MapIcon, X as CloseIcon, MapPin } from 'lucide-react';
import Link from 'next/link';
import { jobsApi } from '@/lib/api/jobs';
import { profileApi, Profile } from '@/lib/api/profile';
import JobDistanceMap from '@/components/maps/JobDistanceMap';

const GOOGLE_MAPS_API_KEY = process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || '';

export default function EmpresaCandidatosPage() {
  const [candidatos, setCandidatos] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [employerProfile, setEmployerProfile] = useState<Profile | null>(null);
  const [selectedCandidateForMap, setSelectedCandidateForMap] = useState<any | null>(null);
  const [candidateProfileForMap, setCandidateProfileForMap] = useState<Profile | null>(null);

  useEffect(() => {
    const fetchInitialData = async () => {
      setLoading(true);
      try {
        let realEmpresaId = '';
        const storedUser = localStorage.getItem('user_info');
        if (storedUser) {
          const parsed = JSON.parse(storedUser);
          realEmpresaId = parsed.id?.toString() || '';
          
          // Cargar perfil del empleador para coordenadas
          const empProfRes = await profileApi.getByUserId(realEmpresaId);
          if (empProfRes.data) setEmployerProfile(empProfRes.data);
        }

        if (!realEmpresaId) {
          setLoading(false);
          return;
        }

        const response = await jobsApi.getApplicationsByEmployerId(realEmpresaId);
        if (response.data) {
          const mapped = response.data.map((app: any) => ({
            id: app.id,
            userId: app.userId,
            nombre: `Trabajador ID: ${app.userId.substring(0, 8)}...`,
            score: 85,
            ofertaPostulada: `Turno ID: ${app.jobId}`,
            experiencia: 'Información en perfil',
            certificaciones: [],
            estado: app.estado,
            jobId: app.jobId // Necesario para buscar la ubicación del trabajo
          }));
          setCandidatos(mapped);
        }
      } catch (error) {
        console.error('Error fetching initial data:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchInitialData();
  }, []);

  const handleAction = async (applicationId: string, status: string) => {
    try {
      await jobsApi.updateApplicationStatus(applicationId, status);
      // Actualizar estado local
      setCandidatos(prev => prev.map(c => 
        c.id === applicationId ? { ...c, estado: status } : c
      ));
    } catch (error) {
      console.error('Error updating application status:', error);
      alert('Error al actualizar el estado de la postulación');
    }
  };

  const handleViewMap = async (candidato: any) => {
    setSelectedCandidateForMap(candidato);
    setCandidateProfileForMap(null); // Reset
    try {
      const res = await profileApi.getByUserId(candidato.userId);
      if (res.data) setCandidateProfileForMap(res.data);
    } catch (error) {
      console.error('Error fetching candidate profile:', error);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-gray-500 animate-pulse">Buscando candidatos interesados...</p>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Candidatos (Matches)</h1>
        <p className="text-gray-500 text-sm mt-1">Revisa los trabajadores que hacen match con tus ofertas y selecciónalos.</p>
      </div>

      <div className="space-y-4">
        {candidatos.length > 0 ? candidatos.map((candidato) => (
          <Card key={candidato.id} className={`hover:border-primary/50 transition-all ${candidato.estado === 'RECHAZADO' ? 'opacity-60 grayscale' : ''}`}>
            <CardContent className="p-4 sm:p-6 flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
              <div className="flex-1 space-y-2">
                <div className="flex items-center gap-3">
                  <h3 className="font-bold text-lg text-gray-900">{candidato.nombre}</h3>
                  <Badge variant={candidato.score >= 90 ? 'success' : candidato.score >= 70 ? 'info' : 'warning'}>
                    Match: {candidato.score}%
                  </Badge>
                  {candidato.estado === 'ACEPTADO' && <Badge variant="success" className="gap-1"><Check size={12}/> Seleccionado</Badge>}
                  {candidato.estado === 'RECHAZADO' && <Badge variant="warning" className="bg-red-100 text-red-700 border-red-200">Rechazado</Badge>}
                </div>
                
                <p className="text-sm text-gray-600 font-medium">Postula a: {candidato.ofertaPostulada}</p>
                
                <div className="text-sm text-gray-500">
                  <span className="font-medium">Experiencia:</span> {candidato.experiencia}
                </div>
              </div>
              
              <div className="flex flex-col sm:flex-row gap-2 w-full sm:w-auto">
                <Button 
                  variant="outline" 
                  size="sm" 
                  className="gap-2"
                  onClick={() => handleViewMap(candidato)}
                >
                  <MapIcon size={16} />
                  Ver Mapa
                </Button>
                <Link href={`/empresa/perfil-candidato/${candidato.userId}`}>
                  <Button variant="outline" size="sm" className="gap-2 w-full">
                    <User size={16} />
                    Ver Perfil
                  </Button>
                </Link>
                {candidato.estado === 'PENDIENTE' && (
                  <>
                    <Button 
                      variant="primary" 
                      size="sm" 
                      className="gap-2"
                      onClick={() => handleAction(candidato.id, 'ACEPTADO')}
                    >
                      <Check size={16} />
                      Aceptar
                    </Button>
                    <Button 
                      variant="ghost" 
                      size="sm" 
                      className="text-red-600 hover:text-red-700 hover:bg-red-50 gap-2"
                      onClick={() => handleAction(candidato.id, 'RECHAZADO')}
                    >
                      <X size={16} />
                      Rechazar
                    </Button>
                  </>
                )}
              </div>
            </CardContent>
          </Card>
        )) : (
          <div className="text-center py-20 bg-gray-50 rounded-xl border-2 border-dashed border-gray-200">
            <div className="max-w-xs mx-auto space-y-4">
              <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto text-gray-400">
                <Briefcase size={32} />
              </div>
              <h3 className="text-lg font-medium text-gray-900">Aún no hay candidatos</h3>
              <p className="text-gray-500 text-sm">Las postulaciones aparecerán aquí a medida que los trabajadores se interesen.</p>
            </div>
          </div>
        )}
      </div>

      {/* MODAL DEL MAPA DE DISTANCIA */}
      {selectedCandidateForMap && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-white rounded-[32px] max-w-2xl w-full shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
            <div className="p-8">
              <div className="flex justify-between items-center mb-6">
                <div>
                  <h3 className="text-2xl font-bold text-primary-dark">Proximidad del Candidato</h3>
                  <p className="text-gray-500 text-sm">Distancia entre tu oficina y el trabajador</p>
                </div>
                <button 
                  onClick={() => setSelectedCandidateForMap(null)}
                  className="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center text-gray-500 hover:bg-gray-200 transition-all"
                >
                  <CloseIcon size={20} />
                </button>
              </div>

              {!candidateProfileForMap ? (
                <div className="h-[350px] flex flex-col items-center justify-center bg-gray-50 rounded-3xl border border-dashed border-gray-200">
                  <Loader2 className="w-10 h-10 text-primary animate-spin mb-4" />
                  <p className="text-gray-500">Cargando coordenadas del candidato...</p>
                </div>
              ) : employerProfile?.latitude && employerProfile?.longitude && 
               typeof candidateProfileForMap.latitude === 'number' && typeof candidateProfileForMap.longitude === 'number' ? (
                <JobDistanceMap 
                  apiKey={GOOGLE_MAPS_API_KEY}
                  origin={{ 
                    lat: employerProfile.latitude, 
                    lng: employerProfile.longitude, 
                    label: "Tu Empresa" 
                  }}
                  destination={{ 
                    lat: candidateProfileForMap.latitude, 
                    lng: candidateProfileForMap.longitude, 
                    label: "Candidato" 
                  }}
                />
              ) : (
                <div className="bg-amber-50 border border-amber-200 p-8 rounded-[32px] text-amber-700 text-center">
                  <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center mx-auto shadow-sm mb-4">
                    <MapPin className="w-8 h-8 opacity-50" />
                  </div>
                  <p className="font-bold text-xl mb-2">Datos Geográficos Incompletos</p>
                  <p className="text-sm max-w-sm mx-auto">
                    Para calcular la distancia, ambos deben haber guardado su ubicación en sus respectivos perfiles.
                  </p>
                  <div className="mt-4 pt-4 border-t border-amber-100 flex justify-center gap-4 text-[10px] font-mono">
                    <span>Tú (Empresa): {employerProfile?.latitude ? '✅' : '❌'}</span>
                    <span>Candidato: {candidateProfileForMap.latitude ? '✅' : '❌'}</span>
                  </div>
                </div>
              )}

              <div className="mt-8 flex justify-end">
                <Button variant="primary" onClick={() => setSelectedCandidateForMap(null)} className="px-8">
                  Cerrar
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
