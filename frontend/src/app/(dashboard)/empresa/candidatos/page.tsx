"use client";

import React, { useState, useEffect } from 'react';
import { Card, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { User, Check, X, Loader2, Briefcase } from 'lucide-react';
import Link from 'next/link';
import { jobsApi } from '@/lib/api/jobs';

export default function EmpresaCandidatosPage() {
  const [candidatos, setCandidatos] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchCandidatos = async () => {
      setLoading(true);
      try {
        let realEmpresaId = '';
        const { createClient } = await import('@/lib/supabase/client');
        const supabase = createClient();
        const { data: { user } } = await supabase.auth.getUser();
        
        if (user) {
          realEmpresaId = user.id;
        } else {
          const storedUser = localStorage.getItem('user_info');
          if (storedUser) {
            const parsed = JSON.parse(storedUser);
            realEmpresaId = parsed.id?.toString() || '';
          }
        }

        if (!realEmpresaId) {
          setLoading(false);
          return;
        }

        const response = await jobsApi.getApplicationsByEmployerId(realEmpresaId);
        if (response.data) {
          // Mapeamos los datos de la aplicación a lo que espera la UI
          const mapped = response.data.map((app: any) => ({
            id: app.id,
            userId: app.userId,
            nombre: `Trabajador ID: ${app.userId.substring(0, 8)}...`,
            score: 85, // Mock score
            ofertaPostulada: `Turno ID: ${app.jobId}`,
            experiencia: 'Información en perfil',
            certificaciones: [],
            estado: app.estado
          }));
          setCandidatos(mapped);
        }
      } catch (error) {
        console.error('Error fetching candidates:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchCandidatos();
  }, []);

  const handleAction = async (id: string, action: 'ACEPTADO' | 'RECHAZADO') => {
    try {
      const res = await jobsApi.updateApplicationStatus(id, action);
      if (!res.error) {
        setCandidatos(prev => prev.map(c => 
          c.id === id ? { ...c, estado: action } : c
        ));
        const msg = action === 'ACEPTADO' ? '¡Candidato aceptado!' : 'Candidato rechazado.';
        alert(msg);
      } else {
        alert('Error al cambiar el estado: ' + res.error);
      }
    } catch (error) {
      console.error('Error updating status:', error);
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
    </div>
  );
}
