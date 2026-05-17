"use client";

import React, { useState, useEffect, Suspense } from 'react';
import { Card, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { User, Check, X, Loader2, Briefcase, Map as MapIcon, X as CloseIcon, MapPin, DollarSign, Star, CheckCircle2 } from 'lucide-react';
import Link from 'next/link';
import { jobsApi } from '@/lib/api/jobs';
import { profileApi, Profile } from '@/lib/api/profile';
import JobDistanceMap from '@/components/maps/JobDistanceMap';
import { useNotifications } from '@/context/NotificationContext';
import { calculateMatchScore } from '@/lib/matchEngine';
import PaymentGatewayModal from '@/components/modals/PaymentGatewayModal';
import RatingModal from '@/components/modals/RatingModal';
import { useSearchParams, useRouter } from 'next/navigation';
import { paymentApi } from '@/lib/api/payment';
import { useSettings } from '@/context/SettingsContext';

const GOOGLE_MAPS_API_KEY = process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || '';

export default function EmpresaCandidatosPage() {
  return (
    <Suspense fallback={<Loader2 className="w-10 h-10 text-primary animate-spin" />}>
      <EmpresaCandidatosContent />
    </Suspense>
  );
}

function EmpresaCandidatosContent() {
  const { addNotification } = useNotifications();
  const { t } = useSettings();
  const [candidatos, setCandidatos] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [employerProfile, setEmployerProfile] = useState<Profile | null>(null);
  const [selectedCandidateForMap, setSelectedCandidateForMap] = useState<any | null>(null);
  const [candidateProfileForMap, setCandidateProfileForMap] = useState<Profile | null>(null);
  const searchParams = useSearchParams();
  const router = useRouter();
  const tabParam = searchParams.get('tab');
  const [activeTab, setActiveTab] = useState<'pendientes' | 'activos' | 'por_calificar' | 'historial'>('pendientes');
  const [isProcessingWebpay, setIsProcessingWebpay] = useState(false);

  useEffect(() => {
    if (tabParam && ['pendientes', 'activos', 'por_calificar', 'historial'].includes(tabParam)) {
      setActiveTab(tabParam as any);
    }
  }, [tabParam]);

  
  // Payment & Rating States
  const [selectedCandidateForPayment, setSelectedCandidateForPayment] = useState<any | null>(null);
  const [selectedCandidateForRating, setSelectedCandidateForRating] = useState<any | null>(null);

  useEffect(() => {
    const confirmWebpayPayout = async () => {
      const tokenWs = searchParams.get('token_ws');
      const appId = searchParams.get('applicationId');
      if (tokenWs && appId) {
        setLoading(true);
        try {
          const response = await paymentApi.confirmWebpay(tokenWs);
          if (response.data && response.data.status === 'AUTHORIZED') {
            await jobsApi.updateApplicationStatus(appId, 'PAGO_CONFIRMADO');
            
            addNotification(
              'Pago Exitoso',
              'El pago se procesó de forma segura con Webpay Plus y el estado del turno se actualizó a Pago Confirmado.',
              'success',
              '/empresa/candidatos?tab=por_calificar'
            );
            
            // Recargar datos locales
            setCandidatos(prev => prev.map(c => 
              c.id === appId ? { ...c, estado: 'PAGO_CONFIRMADO' } : c
            ));
            
            setActiveTab('por_calificar');
            router.push('/empresa/candidatos?tab=por_calificar');
          } else {
            alert('El pago de Webpay fue rechazado o fallido.');
            router.push('/empresa/candidatos?tab=activos');
          }
        } catch (err) {
          console.error("Error confirmando pago de Webpay:", err);
          alert('Hubo un error de conexión al confirmar el pago.');
          router.push('/empresa/candidatos?tab=activos');
        } finally {
          setLoading(false);
        }
      }
    };
    confirmWebpayPayout();
  }, [searchParams]);

  const fetchInitialData = async (isSilent = false) => {
    if (!isSilent) setLoading(true);
    try {
      let realEmpresaId = '';
      let localEmployerProfile: Profile | null = null;
      const storedUser = localStorage.getItem('user_info');
      if (storedUser) {
        const parsed = JSON.parse(storedUser);
        realEmpresaId = parsed.id?.toString() || '';
        
        // Cargar perfil del empleador para coordenadas
        const empProfRes = await profileApi.getByUserId(realEmpresaId);
        if (empProfRes.data) {
          setEmployerProfile(empProfRes.data);
          localEmployerProfile = empProfRes.data;
        }
      }

      if (!realEmpresaId) {
        if (!isSilent) setLoading(false);
        return;
      }

      const response = await jobsApi.getApplicationsByEmployerId(realEmpresaId);
      if (response.data) {
        // Enriquecer datos con nombres de usuarios y títulos de trabajos
        const enrichedCandidatos = await Promise.all(response.data.map(async (app: any) => {
          let nombreReal = `Candidato (${app.userId.substring(0, 5)})`;
          let tituloTrabajo = `Turno #${app.jobId}`;
          let experiencia = 'Ver perfil para detalles';

            let matchScore = 85;
            let workerProfile = null;
            let jobOffer = null;

            try {
              // Buscar perfil del trabajador
              const profRes = await profileApi.getByUserId(app.userId);
              
              if (profRes.data) {
                workerProfile = profRes.data;
                nombreReal = profRes.data.name;
                experiencia = profRes.data.description || 'Sin descripción detallada';
              }

              // Buscar detalles del trabajo
              const jobRes = await jobsApi.getById(app.jobId);
              
              if (jobRes.data) {
                jobOffer = jobRes.data;
                tituloTrabajo = jobRes.data.titulo;
              }
              
              // Calcular MATCH REAL
              if (workerProfile && localEmployerProfile) {
                const scoreObj = calculateMatchScore(workerProfile, localEmployerProfile, jobOffer);
                matchScore = scoreObj.total;
              }
            } catch (e) {
              console.error("Error enriqueciendo datos:", e);
            }

            return {
              id: app.id,
              userId: app.userId,
              nombre: nombreReal,
              score: matchScore,
              ofertaPostulada: tituloTrabajo,
              remuneracion: jobOffer?.remuneracion || 0,
              workerProfile: workerProfile,
              experiencia: experiencia,
              certificaciones: [],
              estado: app.estado,
              jobId: app.jobId
            };
          }));
          
          // Ordenar por score de mayor a menor
          enrichedCandidatos.sort((a, b) => b.score - a.score);
          setCandidatos(enrichedCandidatos);
      }
    } catch (error) {
      console.error('Error fetching initial data:', error);
    } finally {
      if (!isSilent) setLoading(false);
    }
  };

  useEffect(() => {
    fetchInitialData();
  }, [tabParam]); // Recargar datos si cambia el tab vía URL (notificaciones)

  // Polling en segundo plano para actualizar estados automáticamente
  useEffect(() => {
    const interval = setInterval(() => {
      fetchInitialData(true);
    }, 8000);
    return () => clearInterval(interval);
  }, []);

  const handleAction = async (applicationId: string, status: string) => {
    try {
      await jobsApi.updateApplicationStatus(applicationId, status);
      // Actualizar estado local
      setCandidatos(prev => prev.map(c => 
        c.id === applicationId ? { ...c, estado: status } : c
      ));

      // Navegación automática de tabs para evitar F5
      if (status === 'ACEPTADO') {
        setActiveTab('activos');
      } else if (status === 'RECHAZADO') {
        setActiveTab('historial');
      }

      // Notificación según la acción
      const cand = candidatos.find(c => c.id === applicationId);
      if (status === 'ACEPTADO') {
        addNotification(
          'Candidato Seleccionado',
          `Has aceptado a ${cand?.nombre || 'un candidato'} para el turno.`,
          'success',
          '/empresa/candidatos?tab=activos'
        );
      } else if (status === 'TRABAJO_FINALIZADO') {
        addNotification(
          'Trabajo Validado',
          `Has confirmado que ${cand?.nombre} finalizó el turno. Ahora puedes proceder al pago.`,
          'success',
          '/empresa/candidatos?tab=activos'
        );
      } else {
        addNotification(
          'Postulación Rechazada',
          `Se ha procesado el rechazo del candidato.`,
          'warning',
          '/empresa/candidatos?tab=historial'
        );
      }
    } catch (error) {
      console.error('Error updating application status:', error);
      alert('Error al actualizar el estado de la postulación');
    }
  };

  const handlePaymentSubmit = async ({ file, dataUrl }: { file: File, dataUrl: string }) => {
    if (!selectedCandidateForPayment) return;
    
    try {
      // Guardar comprobante simulado en localStorage para fallback local
      const receiptData = {
        fileName: file.name,
        date: new Date().toISOString(),
        dataUrl: dataUrl
      };
      localStorage.setItem(`payment_receipt_${selectedCandidateForPayment.id}`, JSON.stringify(receiptData));

      // Guardar en la API del servidor local para compartirlo entre distintos navegadores (Chrome, Firefox, Incognito)
      try {
        await fetch('/api/receipts', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            appId: selectedCandidateForPayment.id,
            fileName: file.name,
            dataUrl: dataUrl
          })
        });
      } catch (err) {
        console.error("Error al persistir comprobante en API:", err);
      }

      await jobsApi.updateApplicationStatus(selectedCandidateForPayment.id, 'PAGO_ENVIADO');
      setCandidatos(prev => prev.map(c => 
        String(c.id) === String(selectedCandidateForPayment.id) ? { ...c, estado: 'PAGO_ENVIADO' } : c
      ));
      setActiveTab('activos');
      addNotification(
        'Pago Enviado',
        `Has enviado el comprobante de pago a ${selectedCandidateForPayment.nombre}.`,
        'success',
        '/empresa/candidatos?tab=activos'
      );
    } catch (error) {
      console.error('Error procesando pago:', error);
      alert('Error al registrar el pago');
    } finally {
      setSelectedCandidateForPayment(null);
    }
  };

  const handleWebpayPayment = async () => {
    if (!selectedCandidateForPayment) return;
    setIsProcessingWebpay(true);
    try {
      // En Webpay, la empresa que paga transfiere al trabajador
      const employerId = employerProfile?.userId;
      if (!employerId) throw new Error("ID de empresa no encontrado");

      const returnUrl = `${window.location.origin}/empresa/candidatos?tab=activos&payment=success&appId=${selectedCandidateForPayment.id}`;
      
      const res = await paymentApi.initWebpay(
        employerId,
        selectedCandidateForPayment.remuneracion,
        returnUrl
      );

      if (res.data?.url && res.data?.token) {
        // Enviar formulario POST a Webpay Plus Sandbox (como Transbank exige)
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = res.data.url;
        
        const tokenInput = document.createElement('input');
        tokenInput.type = 'hidden';
        tokenInput.name = 'token_ws';
        tokenInput.value = res.data.token;
        form.appendChild(tokenInput);

        document.body.appendChild(form);
        form.submit();
      } else {
        alert("No se pudo inicializar Webpay.");
        setIsProcessingWebpay(false);
      }
    } catch (err) {
      console.error("Error iniciando Webpay:", err);
      alert("Error de conexión al iniciar Webpay.");
      setIsProcessingWebpay(false);
    }
  };

  const handleRatingSubmit = async (stars: number, feedback: string) => {
    if (!selectedCandidateForRating) return;
    
    try {
      // 1. Guardar la calificación real en el perfil del trabajador
      const workerId = selectedCandidateForRating.userId;
      const profileRes = await profileApi.getByUserId(workerId);
      
      if (profileRes.data) {
        const profile = profileRes.data;
        const currentCount = profile.ratingCount || 0;
        const currentRating = profile.rating || 0;
        
        // Nuevo promedio: ((promedio anterior * cantidad) + nuevas estrellas) / (cantidad + 1)
        const newCount = currentCount + 1;
        const newRating = ((currentRating * currentCount) + stars) / newCount;
        
        profile.rating = newRating;
        profile.ratingCount = newCount;
        
        // Guardar en backend
        await profileApi.save(profile);
      }

      // 2. Actualizar estado de la postulación
      const newStatus = selectedCandidateForRating.estado === 'CALIFICADO_TRABAJADOR' ? 'FINALIZADA' : 'CALIFICADO_EMPRESA';
      await jobsApi.updateApplicationStatus(selectedCandidateForRating.id, newStatus);
      setCandidatos(prev => prev.map(c => 
        String(c.id) === String(selectedCandidateForRating.id) ? { ...c, estado: newStatus } : c
      ));
      setActiveTab('historial');
      addNotification(
        'Calificación Enviada',
        `Has calificado a ${selectedCandidateForRating.nombre} con ${stars} estrellas.`,
        'success',
        '/empresa/candidatos?tab=historial'
      );
    } catch (error) {
      console.error('Error enviando calificación:', error);
      alert('Error al enviar la calificación');
    } finally {
      setSelectedCandidateForRating(null);
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
        <h1 className="text-2xl font-bold text-gray-900">Gestión de Candidatos</h1>
        <p className="text-gray-500 text-sm mt-1">Administra las postulaciones recibidas para tus ofertas.</p>
      </div>

      <div className="bg-white p-1 rounded-2xl shadow-sm border border-gray-100 flex flex-wrap mb-6 gap-1">
        <button 
          onClick={() => setActiveTab('pendientes')}
          className={`flex-1 min-w-[120px] py-3 px-4 rounded-xl text-sm font-bold transition-all flex items-center justify-center gap-2 ${activeTab === 'pendientes' ? 'bg-primary text-white shadow-md' : 'text-gray-500 hover:bg-gray-50'}`}
        >
          <User size={18} />
          Nuevos ({candidatos.filter(c => c.estado === 'PENDIENTE').length})
        </button>
        <button 
          onClick={() => setActiveTab('activos')}
          className={`flex-1 min-w-[120px] py-3 px-4 rounded-xl text-sm font-bold transition-all flex items-center justify-center gap-2 ${activeTab === 'activos' ? 'bg-blue-600 text-white shadow-md' : 'text-gray-500 hover:bg-gray-50'}`}
        >
          <Briefcase size={18} />
          En Proceso ({candidatos.filter(c => ['ACEPTADO', 'TRABAJO_FINALIZADO', 'PAGO_ENVIADO'].includes(c.estado)).length})
        </button>
        <button 
          onClick={() => setActiveTab('por_calificar')}
          className={`flex-1 min-w-[120px] py-3 px-4 rounded-xl text-sm font-bold transition-all flex items-center justify-center gap-2 ${activeTab === 'por_calificar' ? 'bg-amber-500 text-white shadow-md' : 'text-gray-500 hover:bg-gray-50'}`}
        >
          <Star size={18} />
          Por Calificar ({candidatos.filter(c => ['PAGO_CONFIRMADO', 'CALIFICADO_TRABAJADOR'].includes(c.estado)).length})
        </button>
        <button 
          onClick={() => setActiveTab('historial')}
          className={`flex-1 min-w-[120px] py-3 px-4 rounded-xl text-sm font-bold transition-all flex items-center justify-center gap-2 ${activeTab === 'historial' ? 'bg-gray-800 text-white shadow-md' : 'text-gray-500 hover:bg-gray-50'}`}
        >
          <Check size={18} />
          Historial ({candidatos.filter(c => ['CALIFICADO_EMPRESA', 'RECHAZADO', 'FINALIZADA'].includes(c.estado)).length})
        </button>
      </div>

      <div className="space-y-4">
        {candidatos.filter(c => {
          if (activeTab === 'pendientes') return c.estado === 'PENDIENTE';
          if (activeTab === 'activos') return ['ACEPTADO', 'TRABAJO_FINALIZADO', 'PAGO_ENVIADO'].includes(c.estado);
          if (activeTab === 'por_calificar') return ['PAGO_CONFIRMADO', 'CALIFICADO_TRABAJADOR'].includes(c.estado);
          if (activeTab === 'historial') return ['CALIFICADO_EMPRESA', 'RECHAZADO', 'FINALIZADA'].includes(c.estado);
          return false;
        }).length > 0 
          ? candidatos
            .filter(c => {
              if (activeTab === 'pendientes') return c.estado === 'PENDIENTE';
              if (activeTab === 'activos') return ['ACEPTADO', 'TRABAJO_FINALIZADO', 'PAGO_ENVIADO'].includes(c.estado);
              if (activeTab === 'por_calificar') return ['PAGO_CONFIRMADO', 'CALIFICADO_TRABAJADOR'].includes(c.estado);
              if (activeTab === 'historial') return ['CALIFICADO_EMPRESA', 'RECHAZADO', 'FINALIZADA'].includes(c.estado);
              return false;
            })
            .map((candidato) => (
          <Card key={candidato.id} className={`hover:border-primary/50 transition-all ${candidato.estado === 'RECHAZADO' ? 'opacity-60 grayscale' : ''}`}>
            <CardContent className="p-4 sm:p-6 flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
              <div className="flex-1 space-y-2">
                <div className="flex items-center gap-3">
                  <h3 className="font-bold text-lg text-gray-900">{candidato.nombre}</h3>
                  <Badge variant={candidato.score >= 90 ? 'success' : candidato.score >= 70 ? 'info' : 'warning'}>
                    Match: {candidato.score}%
                  </Badge>
                  {candidato.estado === 'ACEPTADO' && <Badge variant="info" className="bg-blue-100 text-blue-700 border-blue-200">Seleccionado</Badge>}
                  {candidato.estado === 'TRABAJO_FINALIZADO' && <Badge variant="info" className="bg-indigo-100 text-indigo-700 border-indigo-200">Trabajo Finalizado</Badge>}
                  {candidato.estado === 'PAGO_ENVIADO' && <Badge variant="info" className="bg-blue-50 text-blue-600 border-blue-100 italic">Esperando Validación</Badge>}
                  {candidato.estado === 'PAGO_CONFIRMADO' && <Badge variant="success" className="bg-green-100 text-green-700 border-green-200"><Check size={12}/> Pago Confirmado</Badge>}
                  {candidato.estado === 'CALIFICADO_TRABAJADOR' && <Badge variant="info" className="bg-blue-100 text-blue-700 border-blue-200">Calificado por Trabajador</Badge>}
                  {candidato.estado === 'CALIFICADO_EMPRESA' && <Badge variant="default" className="bg-purple-100 text-purple-700 border-purple-200"><Star size={12} className="mr-1"/> Calificado</Badge>}
                  {candidato.estado === 'FINALIZADA' && <Badge variant="success" className="bg-green-100 text-green-700 border-green-200"><Check size={12} className="mr-1"/> Completado</Badge>}
                  {candidato.estado === 'RECHAZADO' && <Badge variant="warning" className="bg-red-100 text-red-700 border-red-200">Rechazado</Badge>}
                </div>
                
                <p className="text-sm text-gray-600 font-medium">Postula a: {candidato.ofertaPostulada}</p>
                
                <div className="text-sm text-gray-500">
                  <span className="font-medium">Resumen:</span> {candidato.experiencia}
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
                 {candidato.estado === 'ACEPTADO' && (
                  <Button 
                    variant="primary" 
                    size="sm" 
                    className="gap-2 bg-blue-600 hover:bg-blue-700 text-white border-blue-600"
                    onClick={() => handleAction(candidato.id, 'TRABAJO_FINALIZADO')}
                  >
                    <CheckCircle2 size={16} />
                    Validar Trabajo
                  </Button>
                )}
                {candidato.estado === 'TRABAJO_FINALIZADO' && (
                  <Button 
                    variant="primary" 
                    size="sm" 
                    className="gap-2 bg-green-600 hover:bg-green-700 text-white border-green-600"
                    onClick={() => setSelectedCandidateForPayment(candidato)}
                  >
                    <DollarSign size={16} />
                    Pagar
                  </Button>
                )}
                {['PAGO_CONFIRMADO', 'CALIFICADO_TRABAJADOR'].includes(candidato.estado) && (
                  <Button 
                    variant="primary" 
                    size="sm" 
                    className="gap-2 bg-amber-500 hover:bg-amber-600 text-white border-amber-500"
                    onClick={() => setSelectedCandidateForRating(candidato)}
                  >
                    <Star size={16} />
                    Calificar
                  </Button>
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
              <h3 className="text-lg font-medium text-gray-900">Sin candidatos en esta sección</h3>
              <p className="text-gray-500 text-sm">Los candidatos aparecerán aquí cuando postulen a tus ofertas.</p>
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

      {/* MODALES DE PAGO Y CALIFICACIÓN */}
      {selectedCandidateForPayment && (
        <PaymentGatewayModal
          isOpen={true}
          onClose={() => setSelectedCandidateForPayment(null)}
          onSubmit={handlePaymentSubmit}
          workerProfile={selectedCandidateForPayment.workerProfile}
          amount={selectedCandidateForPayment.remuneracion}
          onPayWithWebpay={handleWebpayPayment}
          isProcessingWebpay={isProcessingWebpay}
        />
      )}

      {selectedCandidateForRating && (
        <RatingModal
          isOpen={true}
          onClose={() => setSelectedCandidateForRating(null)}
          onSubmit={handleRatingSubmit}
          title="Calificar Trabajador"
          subtitle="Evalúa el desempeño de este trabajador en el turno."
          targetName={selectedCandidateForRating.nombre}
        />
      )}
    </div>
  );
}
