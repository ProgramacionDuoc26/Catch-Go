"use client";

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardFooter } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { jobsApi } from '@/lib/api/jobs';
import { Oferta } from '@/lib/api/types';
import { Plus, Users, Calendar, MapPin, Loader2, Trash2, Edit, Pause, Play, CheckCircle, Eye, X } from 'lucide-react';

export default function EmpresaOfertasPage() {
  const [ofertas, setOfertas] = useState<Oferta[]>([]);
  const [applications, setApplications] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [appsCount, setAppsCount] = useState(0);
  const [porPagarCount, setPorPagarCount] = useState(0);
  const [enRevisionCount, setEnRevisionCount] = useState(0);
  const [selectedJobForDetails, setSelectedJobForDetails] = useState<Oferta | null>(null);

  const fetchData = async () => {
    setLoading(true);
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
        setLoading(false);
        return;
      }

      // Fetch ofertas y filtrar las que NO estén COMPLETADAS (para sacarlas de la vista activa)
      const response = await jobsApi.list();
      if (response.data) {
        setOfertas(response.data.filter(o => o.empresaId === realEmpresaId && o.estado !== 'COMPLETADA'));
      }

      // Fetch applications count and calculate sub-states
      const res = await jobsApi.getApplicationsByEmployerId(realEmpresaId);
      if (res.data) {
        setApplications(res.data);
        setAppsCount(res.data.length);
        setPorPagarCount(res.data.filter((app: any) => app.estado === 'TRABAJO_FINALIZADO' || app.estado === 'PAGO_ENVIADO').length);
        setEnRevisionCount(res.data.filter((app: any) => app.estado === 'PENDIENTE').length);
      }
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleDelete = async (id: string) => {
    if (!confirm('¿Estás seguro de que deseas eliminar esta oferta?')) return;
    try {
      const res = await jobsApi.delete(id);
      if (!res.error) {
        setOfertas(prev => prev.filter(o => o.id !== id));
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
        if (newStatus === 'COMPLETADA') {
          // Filtrar fuera de la lista activa
          setOfertas(prev => prev.filter(o => o.id !== id));
          alert('Oferta completada con éxito. Ha sido archivada y retirada de la lista activa.');
        } else {
          // Actualizar estado localmente
          setOfertas(prev => prev.map(o => o.id === id ? { ...o, estado: newStatus } : o));
          alert(`Estado de la oferta cambiado a ${newStatus === 'ABIERTA' ? 'abierta' : 'pausada'} con éxito.`);
        }
        
        // Recargar contadores generales de postulaciones para mantener coherencia
        const storedUser = localStorage.getItem('user_info');
        if (storedUser) {
          const parsed = JSON.parse(storedUser);
          const realEmpresaId = parsed.id?.toString() || '';
          if (realEmpresaId) {
            const appsRes = await jobsApi.getApplicationsByEmployerId(realEmpresaId);
            if (appsRes.data) {
              setApplications(appsRes.data);
              setAppsCount(appsRes.data.length);
              setPorPagarCount(appsRes.data.filter((app: any) => app.estado === 'TRABAJO_FINALIZADO' || app.estado === 'PAGO_ENVIADO').length);
              setEnRevisionCount(appsRes.data.filter((app: any) => app.estado === 'PENDIENTE').length);
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

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
        <Link href="/empresa/ofertas" className="block transition-all hover:scale-[1.02]">
          <Card className="bg-white border-none shadow-sm hover:shadow-md cursor-pointer h-full">
            <CardContent className="p-4 flex flex-col items-center justify-center">
              <span className="block text-2xl font-bold text-primary">{ofertas.length}</span>
              <span className="text-xs text-gray-500 font-medium uppercase tracking-wider">Turnos Activos</span>
            </CardContent>
          </Card>
        </Link>
        
        <Link href="/empresa/candidatos" className="block transition-all hover:scale-[1.02]">
          <Card className="bg-white border-none shadow-sm hover:shadow-md cursor-pointer h-full">
            <CardContent className="p-4 flex flex-col items-center justify-center">
              <span className="block text-2xl font-bold text-primary">{appsCount}</span>
              <span className="text-xs text-gray-500 font-medium uppercase tracking-wider">Candidatos Totales</span>
            </CardContent>
          </Card>
        </Link>

        <Link href="/empresa/candidatos?tab=activos" className="block transition-all hover:scale-[1.02]">
          <Card className="bg-white border-none shadow-sm hover:shadow-md cursor-pointer h-full">
            <CardContent className="p-4 flex flex-col items-center justify-center">
              <span className="block text-2xl font-bold text-green-600">{porPagarCount}</span>
              <span className="text-xs text-gray-500 font-medium uppercase tracking-wider">Por Pagar</span>
            </CardContent>
          </Card>
        </Link>

        <Link href="/empresa/candidatos?tab=pendientes" className="block transition-all hover:scale-[1.02]">
          <Card className="bg-white border-none shadow-sm hover:shadow-md cursor-pointer h-full">
            <CardContent className="p-4 flex flex-col items-center justify-center">
              <span className="block text-2xl font-bold text-amber-600">{enRevisionCount}</span>
              <span className="text-xs text-gray-500 font-medium uppercase tracking-wider">En Revisión</span>
            </CardContent>
          </Card>
        </Link>
      </div>

      <div className="grid grid-cols-1 gap-4">
        {ofertas.length > 0 ? ofertas.map((oferta) => (
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
              <div className="mt-4 flex gap-6 border-t pt-4">
                <div className="text-center">
                  <span className="block text-lg font-bold text-primary flex items-center justify-center gap-1.5">
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
                <Link href={`/empresa/ofertas/${oferta.id}/editar`}>
                  <Button variant="ghost" size="sm" className="text-amber-600 hover:bg-amber-50">
                    <Edit className="w-3.5 h-3.5 mr-1" /> Editar
                  </Button>
                </Link>
              </div>
              <div className="flex gap-2 items-center">
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

                <Button variant="ghost" size="sm" className="text-red-600 hover:bg-red-50 p-2" onClick={() => handleDelete(oferta.id)}>
                  <Trash2 className="w-3.5 h-3.5" />
                </Button>
              </div>
            </CardFooter>
          </Card>
        )) : (
          <div className="text-center py-20 bg-white rounded-xl border-2 border-dashed border-gray-100">
            <div className="max-w-xs mx-auto space-y-4">
              <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto text-gray-300">
                <Plus size={32} />
              </div>
              <h3 className="text-lg font-medium text-gray-900">No tienes turnos publicados</h3>
              <p className="text-gray-500 text-sm">Comienza a publicar para encontrar a los mejores workers.</p>
              <Link href="/empresa/ofertas/nueva">
                <Button variant="primary">Publicar Primer Turno</Button>
              </Link>
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
