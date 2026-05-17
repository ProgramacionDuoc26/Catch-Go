"use client";

import React, { useState, useEffect } from 'react';
import { Card, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Loader2, Trash2, Briefcase } from 'lucide-react';
import Link from 'next/link';
import { jobsApi } from '@/lib/api/jobs';

export default function TrabajadorPostulacionesPage() {
  const [postulaciones, setPostulaciones] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchPostulaciones = async () => {
      setLoading(true);
      try {
        // Obtener el ID real del usuario
        let realUserId = '';
        
        // 1. Intentar primero con localStorage (fuente de verdad de la sesión activa en el frontend)
        const storedUser = localStorage.getItem('user_info');
        if (storedUser) {
          try {
            const parsed = JSON.parse(storedUser);
            realUserId = parsed.id?.toString() || '';
          } catch { /* ignore */ }
        }

        // 2. Si no hay en localStorage, usar Supabase como fallback
        if (!realUserId) {
          try {
            const { createClient } = await import('@/lib/supabase/client');
            const supabase = createClient();
            const { data: { user: supabaseUser } } = await supabase.auth.getUser();
            if (supabaseUser) {
              realUserId = supabaseUser.id;
            }
          } catch { /* ignore supabase errors */ }
        }

        if (!realUserId) {
          console.warn('No se encontró sesión de usuario.');
          setLoading(false);
          return;
        }

        const [response, jobsRes] = await Promise.all([
          jobsApi.getApplicationsByUserId(realUserId),
          jobsApi.list()
        ]);

        if (response.data && Array.isArray(response.data)) {
          const jobs = jobsRes.data || [];
          const dataWithTitles = response.data.map((p: any) => {
            const matchedJob = jobs.find((j: any) => String(j.id) === String(p.jobId));
            return {
              ...p,
              oferta: matchedJob?.titulo || p.jobTitle || `Turno ID: ${p.jobId}`,
              empresa: (matchedJob as any)?.empresa_nombre || (matchedJob as any)?.empresaNombre || "Empresa Aliada",
              remuneracion: matchedJob?.remuneracion || p.remuneracion,
              fecha: p.fechaPostulacion ? new Date(p.fechaPostulacion).toLocaleDateString('es-CL') : 'Hoy',
            };
          });
          setPostulaciones(dataWithTitles);
        }
      } catch (error) {
        console.error('Error fetching applications:', error);
        // Dejamos la lista vacía, la UI mostrará el estado vacío
      } finally {
        setLoading(false);
      }
    };
    fetchPostulaciones();
  }, []);


  const handleDeleteApplication = async (id: string) => {
    if (!confirm('¿Estás seguro de que deseas cancelar esta postulación?')) return;
    try {
      const res = await jobsApi.deleteApplication(id);
      if (!res.error) {
        setPostulaciones(prev => prev.filter(p => p.id !== id));
        alert('Postulación cancelada con éxito.');
      } else {
        alert('Error al cancelar: ' + JSON.stringify(res.error));
      }
    } catch (error) {
      console.error('Error deleting application:', error);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-gray-500 animate-pulse">Cargando tus postulaciones...</p>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Mis Postulaciones</h1>
        <p className="text-gray-500 text-sm mt-1">Sigue el estado de tus postulaciones a turnos.</p>
      </div>

      <div className="space-y-4">
        {postulaciones.length > 0 ? postulaciones.map((postulacion: any) => (
          <Card key={postulacion.id}>
            <CardContent className="p-4 sm:p-6 flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
              <div className="flex-1 space-y-1">
                <div className="flex items-center gap-3">
                  <h3 className="font-bold text-lg text-gray-900">{postulacion.oferta}</h3>
                  <Badge 
                    variant={
                      postulacion.estado === 'ACEPTADO' ? 'success' : 
                      postulacion.estado === 'RECHAZADO' ? 'error' : 'warning'
                    }
                  >
                    {postulacion.estado}
                  </Badge>
                </div>
                <p className="text-sm font-medium text-gray-700">{postulacion.empresa}</p>
                <div className="text-sm text-gray-500 flex gap-4 mt-1">
                  <span>Postulado el: {postulacion.fechaPostulacion || 'Hoy'}</span>
                  <span className="font-medium text-green-700">{postulacion.remuneracion ? `$${postulacion.remuneracion.toLocaleString('es-CL')}` : 'Remuneración no informada'}</span>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <Button 
                  variant="ghost" 
                  size="sm" 
                  className="text-red-500 hover:bg-red-50 gap-2"
                  onClick={() => handleDeleteApplication(postulacion.id)}
                >
                  <Trash2 size={16} />
                  <span className="hidden sm:inline">Cancelar</span>
                </Button>
              </div>
            </CardContent>
          </Card>
        )) : (
          <div className="text-center py-20 bg-gray-50 rounded-xl border-2 border-dashed border-gray-200">
            <div className="max-w-xs mx-auto space-y-4">
              <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto text-gray-400">
                <Briefcase size={32} />
              </div>
              <h3 className="text-lg font-medium text-gray-900">No has postulado a nada aún</h3>
              <p className="text-gray-500 text-sm">Explora las ofertas disponibles y comienza a postular para verlas aquí.</p>
              <Link href="/trabajador/ofertas" className="block">
                <Button variant="primary">Explorar Ofertas</Button>
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
