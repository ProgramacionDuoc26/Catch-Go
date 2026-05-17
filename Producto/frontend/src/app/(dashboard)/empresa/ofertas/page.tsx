"use client";

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardFooter } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { jobsApi } from '@/lib/api/jobs';
import { profileApi } from '@/lib/api/profile';
import { Oferta } from '@/lib/api/types';
import { Plus, Users, Calendar, MapPin, Loader2, Trash2, Edit, Pause, Play, CheckCircle, Eye, X } from 'lucide-react';

export default function EmpresaOfertasPage() {
  const [allOfertas, setAllOfertas] = useState<Oferta[]>([]);
  const [applications, setApplications] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [viewTab, setViewTab] = useState<'activos' | 'completados'>('activos');
  const [selectedJobForDetails, setSelectedJobForDetails] = useState<Oferta | null>(null);

  // Variables calculadas de manera reactiva para evitar desincronización
  const activeApplications = applications.filter((app: any) => {
    const isAppActive = !['FINALIZADA', 'RECHAZADO'].includes(app.estado);
    const offer = allOfertas.find(o => String(o.id) === String(app.jobId));
    const isOfferActive = offer ? offer.estado !== 'COMPLETADA' : true;
    return isAppActive && isOfferActive;
  });
  
  const appsCount = activeApplications.length;
  
  const porPagarCount = applications.filter((app: any) => {
    const isPendingPayment = app.estado === 'TRABAJO_FINALIZADO' || app.estado === 'PAGO_ENVIADO';
    const offer = allOfertas.find(o => String(o.id) === String(app.jobId));
    const isOfferActive = offer ? offer.estado !== 'COMPLETADA' : true;
    return isPendingPayment && isOfferActive;
  }).length;

  const fetchData = async (isSilent = false) => {
    if (!isSilent) setLoading(true);
    try {
      // Obtener el ID real de la empresa
      let realEmpresaId = '';
      
      const { createClient } = await import('@/lib/supabase/client');
      const supabase = createClient();
      const { data: { user: supabaseUser } } = await supabase.auth.getUser();
      
      if (supabaseUser) {
        realEmpresaId = supabaseUser.id;
      } else {
        const storedUser = localStorage.getItem('user_info');
        if (storedUser) {
          const parsed = JSON.parse(storedUser);
          realEmpresaId = parsed.id?.toString() || '';
        }
      }
 
      if (!realEmpresaId) {
        console.warn('No se encontró sesión de empresa.');
        if (!isSilent) setLoading(false);
        return;
      }
 
      // Fetch ofertas y ordenar por ID de forma descendente (más nuevo arriba)
      const response = await jobsApi.list();
      if (response.data) {
        const sorted = response.data
          .filter(o => o.empresaId === realEmpresaId)
          .sort((a, b) => Number(b.id) - Number(a.id));
        setAllOfertas(sorted);
      }
 
      // Fetch applications count and calculate sub-states
      const res = await jobsApi.getApplicationsByEmployerId(realEmpresaId);
      if (res.data) {
        // Enriquecer postulaciones con el nombre real del trabajador para el historial
        const enrichedApps = await Promise.all(res.data.map(async (app: any) => {
          try {
            const profRes = await profileApi.getByUserId(app.userId);
            return {
              ...app,
              workerName: profRes.data ? profRes.data.name : `Trabajador (${app.userId.substring(0, 5)})`
            };
          } catch (e) {
            return { ...app, workerName: `Trabajador (${app.userId.substring(0, 5)})` };
          }
        }));
        setApplications(enrichedApps);
      }
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      if (!isSilent) setLoading(false);
    }
  };
 
  useEffect(() => {
    fetchData(false);
 
    // Polling cada 8 segundos en segundo plano para actualización reactiva en tiempo real de los candidatos y estados
    const interval = setInterval(() => {
      fetchData(true);
    }, 8000);
 
    return () => clearInterval(interval);
  }, []);

  const handleDelete = async (id: string) => {
    if (!confirm('¿Estás seguro de que deseas eliminar esta oferta?')) return;
    try {
      const res = await jobsApi.delete(id);
      if (!res.error) {
        setAllOfertas(prev => prev.filter(o => String(o.id) !== String(id)));
        alert('Oferta eliminada con éxito.');
      } else {
        alert('Error al eliminar: ' + JSON.stringify(res.error));
      }
    } catch (error) {
      console.error('Error deleting offer:', error);
    }
  };

  const handleUpdateStatus = async (id: string, newStatus: Oferta['estado']) => {
    try {
      const res = await jobsApi.updateStatus(id, newStatus);
      if (!res.error) {
        // Actualizar estado localmente usando comparación segura de strings para evitar mismatch de tipos (number vs string)
        setAllOfertas(prev => prev.map(o => String(o.id) === String(id) ? { ...o, estado: newStatus } : o));
        
        if (newStatus === 'COMPLETADA') {
          alert('Oferta completada con éxito. Ha sido archivada en la pestaña de Completados.');
        } else if (newStatus === 'ABIERTA') {
          alert('Oferta reactivada/reanudada con éxito. Se encuentra disponible en Turnos Activos.');
        } else {
          alert('Oferta pausada con éxito.');
        }
        
        // Recargar contadores generales de postulaciones para mantener coherencia
        const storedUser = localStorage.getItem('user_info');
        if (storedUser) {
          const parsed = JSON.parse(storedUser);
          const realEmpresaId = parsed.id?.toString() || '';
          if (realEmpresaId) {
            const appsRes = await jobsApi.getApplicationsByEmployerId(realEmpresaId);
            if (appsRes.data) {
              const enrichedApps = await Promise.all(appsRes.data.map(async (app: any) => {
                try {
                  const profRes = await profileApi.getByUserId(app.userId);
                  return {
                    ...app,
                    workerName: profRes.data ? profRes.data.name : `Trabajador (${app.userId.substring(0, 5)})`
                  };
                } catch (e) {
                  return { ...app, workerName: `Trabajador (${app.userId.substring(0, 5)})` };
                }
              }));
              setApplications(enrichedApps);
            }
          }
        }
      } else {
        alert('Error al actualizar estado: ' + JSON.stringify(res.error));
      }
    } catch (error) {
      console.error('Error updating status:', error);
    }
  };

  // Filtrar ofertas a mostrar según la pestaña seleccionada
  const displayedOfertas = allOfertas.filter(o => {
    if (viewTab === 'activos') {
      return o.estado !== 'COMPLETADA';
    } else {
      return o.estado === 'COMPLETADA';
    }
  });

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-gray-500 animate-pulse">Cargando tus ofertas...</p>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Mis Ofertas Publicadas</h1>
          <p className="text-gray-500 text-sm mt-1">Gestiona los turnos que has publicado para tu empresa.</p>
        </div>
        <Link href="/empresa/ofertas/nueva">
          <Button variant="primary" className="gap-2 shadow-lg shadow-primary/20">
            <Plus className="w-4 h-4" />
            Publicar Nuevo Turno
          </Button>
        </Link>
      </div>

      {/* Métrica superior con filtros interactivos */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
        {/* Card 1: Turnos Activos */}
        <div 
          onClick={() => setViewTab('activos')} 
          className={`block transition-all duration-200 hover:scale-[1.02] cursor-pointer rounded-2xl p-1 ${
            viewTab === 'activos' 
              ? 'ring-2 ring-primary bg-primary/5 shadow-md' 
              : 'bg-white border border-gray-100 shadow-sm hover:shadow-md'
          }`}
        >
          <Card className="border-none bg-transparent h-full shadow-none">
            <CardContent className="p-4 flex flex-col items-center justify-center">
              <span className="block text-2xl font-bold text-primary">
                {allOfertas.filter(o => o.estado === 'ABIERTA').length}
              </span>
              <span className="text-xs text-gray-500 font-semibold uppercase tracking-wider mt-1">Turnos Activos</span>
            </CardContent>
          </Card>
        </div>
        
        {/* Card 2: Candidatos Totales */}
        <Link href="/empresa/candidatos" className="block transition-all hover:scale-[1.02]">
          <Card className="bg-white border border-gray-100 shadow-sm hover:shadow-md cursor-pointer h-full">
            <CardContent className="p-4 flex flex-col items-center justify-center">
              <span className="block text-2xl font-bold text-primary">{appsCount}</span>
              <span className="text-xs text-gray-500 font-semibold uppercase tracking-wider mt-1">Candidatos Activos</span>
            </CardContent>
          </Card>
        </Link>

        {/* Card 3: Por Pagar */}
        <Link href="/empresa/candidatos?tab=activos" className="block transition-all hover:scale-[1.02]">
          <Card className="bg-white border border-gray-100 shadow-sm hover:shadow-md cursor-pointer h-full">
            <CardContent className="p-4 flex flex-col items-center justify-center">
              <span className="block text-2xl font-bold text-green-600">{porPagarCount}</span>
              <span className="text-xs text-gray-500 font-semibold uppercase tracking-wider mt-1">Por Pagar</span>
            </CardContent>
          </Card>
        </Link>

        {/* Card 4: Completados */}
        <div 
          onClick={() => setViewTab('completados')} 
          className={`block transition-all duration-200 hover:scale-[1.02] cursor-pointer rounded-2xl p-1 ${
            viewTab === 'completados' 
              ? 'ring-2 ring-blue-500 bg-blue-50/20 shadow-md' 
              : 'bg-white border border-gray-100 shadow-sm hover:shadow-md'
          }`}
        >
          <Card className="border-none bg-transparent h-full shadow-none">
            <CardContent className="p-4 flex flex-col items-center justify-center">
              <span className="block text-2xl font-bold text-blue-600">
                {allOfertas.filter(o => o.estado === 'COMPLETADA').length}
              </span>
              <span className="text-xs text-gray-500 font-semibold uppercase tracking-wider mt-1">Completados</span>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Título de Sección Contextual */}
      <div className="pt-2 border-b border-gray-100 pb-2">
        <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
          {viewTab === 'activos' ? (
            <>
              <span className="w-2 h-2 rounded-full bg-primary animate-pulse"></span>
              Mis Turnos Activos y Pausados
            </>
          ) : (
            <>
              <span className="w-2 h-2 rounded-full bg-blue-500"></span>
              Historial de Turnos Completados
            </>
          )}
        </h2>
        <p className="text-gray-500 text-xs mt-0.5">
          {viewTab === 'activos' 
            ? 'Gestiona la disponibilidad y cambia los estados de tus publicaciones vigentes.' 
            : 'Revisa las ofertas archivadas e históricas de Catch-Go.'}
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4">
        {displayedOfertas.length > 0 ? displayedOfertas.map((oferta) => {
          // Buscar si hay una postulación finalizada para mostrar quién realizó el turno
          const completedApp = applications.find((app: any) => 
            String(app.jobId) === String(oferta.id) && 
            ['FINALIZADA', 'CALIFICADO_TRABAJADOR', 'CALIFICADO_EMPRESA', 'ARCHIVADA', 'PAGO_CONFIRMADO'].includes(app.estado)
          );

          return (
            <Card key={oferta.id} className="hover:border-primary/30 transition-all border-gray-100 shadow-sm overflow-hidden">
              <CardHeader className="flex justify-between items-start pb-2">
                <div>
                  <h3 className="font-semibold text-xl text-gray-900">{oferta.titulo}</h3>
                  <div className="flex items-center gap-4 text-sm text-gray-500 mt-1">
                    <span className="flex items-center gap-1"><MapPin size={14} /> {oferta.ubicacion}</span>
                    <span className="flex items-center gap-1"><Calendar size={14} /> {oferta.fechaInicio}</span>
                  </div>
                </div>
                <span className={`px-2.5 py-1 rounded-full text-xs font-semibold tracking-wide uppercase border ${
                  oferta.estado === 'ABIERTA' ? 'bg-green-50 text-green-700 border-green-200' :
                  oferta.estado === 'PAUSADA' ? 'bg-amber-50 text-amber-700 border-amber-200' :
                  'bg-blue-50 text-blue-700 border-blue-200'
                }`}>
                  {oferta.estado}
                </span>
              </CardHeader>
              <CardContent className="py-2">
                <p className="text-gray-600 text-sm line-clamp-2">{oferta.descripcion}</p>
                
                {oferta.estado === 'COMPLETADA' && completedApp && (
                  <div className="mt-3 p-3 bg-blue-50/50 rounded-xl border border-blue-100/30 flex items-center gap-2 text-xs font-semibold text-blue-700">
                    <Users size={14} className="text-blue-500" />
                    <span>Realizado por: <span className="font-extrabold">{completedApp.workerName}</span></span>
                  </div>
                )}

                <div className="mt-4 flex gap-6 border-t pt-4">
                <div className="text-center">
                  <span className="text-lg font-bold text-primary flex items-center justify-center gap-1.5">
                    <Users className="w-4 h-4 mb-0.5" />
                    <span className="bg-primary/10 text-primary px-2 py-0.5 rounded-full text-xs font-bold">
                      {applications.filter((app: any) => Number(app.jobId) === Number(oferta.id)).length}
                    </span>
                  </span>
                  <span className="text-[10px] text-gray-500 uppercase tracking-widest font-bold">Candidatos</span>
                </div>
                <div className="text-center border-l pl-6">
                  <span className="block text-lg font-bold text-green-700">${oferta.remuneracion.toLocaleString('es-CL')}</span>
                  <span className="text-[10px] text-gray-500 uppercase tracking-widest font-bold">Pago Turno</span>
                </div>
              </div>
            </CardContent>
            <CardFooter className="flex flex-wrap justify-between items-center gap-2 pt-3 border-t bg-gray-50/30">
              <div className="flex gap-2">
                <Button variant="ghost" size="sm" className="text-gray-600 hover:bg-gray-100" onClick={() => setSelectedJobForDetails(oferta)}>
                  <Eye className="w-3.5 h-3.5 mr-1" /> Ver Detalles
                </Button>
                {oferta.estado !== 'COMPLETADA' && (
                  <Link href={`/empresa/ofertas/${oferta.id}/editar`}>
                    <Button variant="ghost" size="sm" className="text-amber-600 hover:bg-amber-50">
                      <Edit className="w-3.5 h-3.5 mr-1" /> Editar
                    </Button>
                  </Link>
                )}
              </div>
              <div className="flex gap-2 items-center">
                {oferta.estado !== 'COMPLETADA' && (
                  <>
                    {oferta.estado === 'ABIERTA' ? (
                      <Button 
                        variant="outline" 
                        size="sm" 
                        className="text-amber-600 border-amber-200 hover:bg-amber-50"
                        onClick={() => handleUpdateStatus(oferta.id, 'PAUSADA')}
                      >
                        <Pause className="w-3 h-3 mr-1" /> Pausar
                      </Button>
                    ) : (
                      <Button 
                        variant="outline" 
                        size="sm" 
                        className="text-green-600 border-green-200 hover:bg-green-50"
                        onClick={() => handleUpdateStatus(oferta.id, 'ABIERTA')}
                      >
                        <Play className="w-3 h-3 mr-1" /> Reanudar
                      </Button>
                    )}

                    <Button 
                      variant="outline" 
                      size="sm" 
                      className="text-blue-600 border-blue-200 hover:bg-blue-50"
                      onClick={() => handleUpdateStatus(oferta.id, 'COMPLETADA')}
                    >
                      <CheckCircle className="w-3 h-3 mr-1" /> Completar
                    </Button>
                  </>
                )}

                {oferta.estado !== 'COMPLETADA' && (
                  <Button variant="ghost" size="sm" className="text-red-600 hover:bg-red-50 p-2" onClick={() => handleDelete(oferta.id)}>
                    <Trash2 className="w-3.5 h-3.5" />
                  </Button>
                )}
              </div>
            </CardFooter>
          </Card>
        );
      }) : (
          <div className="text-center py-20 bg-white rounded-xl border-2 border-dashed border-gray-100">
            <div className="max-w-xs mx-auto space-y-4">
              <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto text-gray-300">
                <Plus size={32} />
              </div>
              <h3 className="text-lg font-medium text-gray-900">
                {viewTab === 'activos' ? 'No tienes turnos publicados' : 'No hay turnos completados'}
              </h3>
              <p className="text-gray-500 text-sm">
                {viewTab === 'activos' 
                  ? 'Comienza a publicar para encontrar a los mejores workers.' 
                  : 'Los turnos que completes con éxito aparecerán en este historial.'}
              </p>
              {viewTab === 'activos' && (
                <Link href="/empresa/ofertas/nueva">
                  <Button variant="primary">Publicar Primer Turno</Button>
                </Link>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Modal de Detalles de la Oferta */}
      {selectedJobForDetails && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-white rounded-[28px] max-w-lg w-full shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200 border border-gray-100">
            <div className="p-6 sm:p-8">
              <div className="flex justify-between items-start mb-6">
                <div>
                  <div className="flex items-center gap-2 mb-2">
                    <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold tracking-wide uppercase bg-primary/10 text-primary">
                      {selectedJobForDetails.categoria || 'General'}
                    </span>
                    <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold tracking-wide uppercase border ${
                      selectedJobForDetails.estado === 'ABIERTA' ? 'bg-green-50 text-green-700 border-green-200' :
                      selectedJobForDetails.estado === 'PAUSADA' ? 'bg-amber-50 text-amber-700 border-amber-200' :
                      'bg-blue-50 text-blue-700 border-blue-200'
                    }`}>
                      {selectedJobForDetails.estado}
                    </span>
                  </div>
                  <h3 className="text-2xl font-bold text-gray-900 leading-snug">
                    {selectedJobForDetails.titulo}
                  </h3>
                </div>
                <button 
                  onClick={() => setSelectedJobForDetails(null)}
                  className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center text-gray-500 hover:bg-gray-200 transition-all"
                >
                  <X size={18} />
                </button>
              </div>

              <div className="space-y-4 max-h-[350px] overflow-y-auto pr-2">
                <div>
                  <h4 className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-1.5">Descripción del Puesto</h4>
                  <div className="bg-gray-50/70 border border-gray-100 p-4 rounded-2xl">
                    <p className="text-gray-700 text-sm leading-relaxed whitespace-pre-line">
                      {selectedJobForDetails.descripcion}
                    </p>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-gray-50/70 border border-gray-100 p-3 rounded-2xl">
                    <h5 className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1">Pago del Turno</h5>
                    <span className="text-base font-extrabold text-green-700 block mt-1">
                      ${selectedJobForDetails.remuneracion.toLocaleString('es-CL')}
                    </span>
                  </div>
                  <div className="bg-gray-50/70 border border-gray-100 p-3 rounded-2xl">
                    <h5 className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1">Ubicación</h5>
                    <span className="text-xs font-semibold text-gray-800 flex items-center gap-1 mt-1.5 truncate">
                      <MapPin size={12} className="text-gray-400 shrink-0" />
                      {selectedJobForDetails.ubicacion}
                    </span>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-gray-50/70 border border-gray-100 p-3 rounded-2xl">
                    <h5 className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1">Fecha de Inicio</h5>
                    <span className="text-xs font-semibold text-gray-800 flex items-center gap-1 mt-1.5">
                      <Calendar size={12} className="text-gray-400 shrink-0" />
                      {selectedJobForDetails.fechaInicio}
                    </span>
                  </div>
                  <div className="bg-gray-50/70 border border-gray-100 p-3 rounded-2xl">
                    <h5 className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1">Total Postulantes</h5>
                    <span className="text-xs font-semibold text-gray-800 flex items-center gap-1 mt-1.5">
                      <Users size={12} className="text-gray-400 shrink-0" />
                      {applications.filter((app: any) => Number(app.jobId) === Number(selectedJobForDetails.id)).length} Candidatos
                    </span>
                  </div>
                </div>

                {/* Desglose de postulantes */}
                <div className="bg-blue-50/50 border border-blue-100/50 p-4 rounded-2xl">
                  <h4 className="text-xs font-bold text-blue-900 mb-2.5 uppercase tracking-widest">Resumen de Postulaciones</h4>
                  <div className="grid grid-cols-3 gap-2 text-center">
                    <div className="bg-white/85 p-2 rounded-xl border border-blue-100/30">
                      <span className="block text-base font-extrabold text-amber-600">
                        {applications.filter((app: any) => Number(app.jobId) === Number(selectedJobForDetails.id) && app.estado === 'PENDIENTE').length}
                      </span>
                      <span className="text-[8px] text-gray-500 uppercase tracking-wider font-bold">Nuevos</span>
                    </div>
                    <div className="bg-white/85 p-2 rounded-xl border border-blue-100/30">
                      <span className="block text-base font-extrabold text-green-600">
                        {applications.filter((app: any) => Number(app.jobId) === Number(selectedJobForDetails.id) && app.estado === 'ACEPTADO').length}
                      </span>
                      <span className="text-[8px] text-gray-500 uppercase tracking-wider font-bold">Activos</span>
                    </div>
                    <div className="bg-white/85 p-2 rounded-xl border border-blue-100/30">
                      <span className="block text-base font-extrabold text-primary">
                        {applications.filter((app: any) => Number(app.jobId) === Number(selectedJobForDetails.id) && ['TRABAJO_FINALIZADO', 'PAGO_ENVIADO', 'PAGO_CONFIRMADO', 'CALIFICADO_EMPRESA', 'CALIFICADO_TRABAJADOR'].includes(app.estado)).length}
                      </span>
                      <span className="text-[8px] text-gray-500 uppercase tracking-wider font-bold">Completados</span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="mt-6 flex justify-end">
                <Button 
                  variant="primary" 
                  onClick={() => setSelectedJobForDetails(null)}
                  className="rounded-xl w-full sm:w-auto"
                >
                  Cerrar Detalles
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
