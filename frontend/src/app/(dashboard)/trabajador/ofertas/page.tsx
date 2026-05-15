"use client";

import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardFooter } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { jobsApi } from '@/lib/api/jobs';
import { Oferta } from '@/lib/api/types';
import { profileApi, Profile } from '@/lib/api/profile';
import { Search, CheckCircle2, Loader2, MapPin, Map as MapIcon, X } from 'lucide-react';
import JobDistanceMap from '@/components/maps/JobDistanceMap';
import { useNotifications } from '@/context/NotificationContext';
import { calculateMatchScore } from '@/lib/matchEngine';
import { calculateProfileCompletion } from '@/lib/profileUtils';

const GOOGLE_MAPS_API_KEY = process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || '';

export default function TrabajadorOfertasPage() {
  const { addNotification } = useNotifications();
  const [ofertas, setOfertas] = useState<any[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [appliedJobs, setAppliedJobs] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [workerProfile, setWorkerProfile] = useState<Profile | null>(null);
  const [profileCompletion, setProfileCompletion] = useState(0);
  const [selectedJobForMap, setSelectedJobForMap] = useState<Oferta | null>(null);
  const [activeTab, setActiveTab] = useState<'todas' | 'postulaciones'>('todas');

  useEffect(() => {
    const fetchInitialData = async () => {
      setLoading(true);
      try {
        const storedUser = localStorage.getItem('user_info');
        let workerProf: Profile | null = null;
        if (storedUser) {
          const userData = JSON.parse(storedUser);
          const userId = userData.id?.toString();
          
          // 1. Cargar perfil para el mapa y completitud
          const profRes = await profileApi.getByUserId(userId);
          if (profRes.data) {
            workerProf = profRes.data;
            setWorkerProfile(workerProf);
            setProfileCompletion(calculateProfileCompletion(workerProf));
          }

          // 2. Cargar postulaciones reales del usuario
          const appsRes = await jobsApi.getApplicationsByUserId(userId);
          if (appsRes.data) {
            const appliedIds = appsRes.data.map((app: any) => app.jobId);
            setAppliedJobs(appliedIds);
          }
        }
        
        const jobsRes = await jobsApi.list();
        if (jobsRes.data) {
          // Fetch employer profiles and calculate matches
          const enrichedOfertas = await Promise.all(jobsRes.data.map(async (offer: any) => {
            let matchScore = 0;
            if (workerProf) {
               try {
                 const empProfRes = await profileApi.getByUserId(offer.empresaId);
                 if (empProfRes.data) {
                    const scoreObj = calculateMatchScore(workerProf, empProfRes.data, offer);
                    matchScore = scoreObj.total;
                 }
               } catch(e) {
                 console.error("Failed to load company profile for match");
               }
            }
            return { ...offer, matchScore };
          }));
          
          // Sort by match score
          enrichedOfertas.sort((a, b) => (b.matchScore || 0) - (a.matchScore || 0));
          setOfertas(enrichedOfertas);
        }
      } catch (error) {
        console.error('Error fetching initial data:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchInitialData();
  }, []);

  const handleApply = async (id: string) => {
    try {
      // Obtener el ID real del trabajador
      let realUserId = '';
      const { createClient } = await import('@/lib/supabase/client');
      const supabase = createClient();
      const { data: { user: supabaseUser } } = await supabase.auth.getUser();
      
      if (supabaseUser) {
        realUserId = supabaseUser.id;
      } else {
        const storedUser = localStorage.getItem('user_info');
        if (storedUser) {
          const parsed = JSON.parse(storedUser);
          realUserId = parsed.id?.toString() || '';
        }
      }

      if (!realUserId) {
        alert('Debes iniciar sesión para postular.');
        return;
      }

      const response = await jobsApi.apply(id, realUserId);
      if (!response.error) {
        setAppliedJobs([...appliedJobs, id]);
        
        // Notificación de éxito
        const oferta = ofertas.find(o => o.id === id);
        addNotification(
          'Postulación Enviada',
          `Tu interés por "${oferta?.titulo || 'la oferta'}" ha sido registrado correctamente.`,
          'info'
        );
      } else {
        alert('Error: ' + response.error);
      }
    } catch (error) {
      console.error('Error applying:', error);
      alert('Error al enviar la postulación.');
    }
  };  // profileCompletion is calculated dynamically now

  const filteredOfertas = ofertas.filter(o => {
    const matchesSearch = o.titulo.toLowerCase().includes(searchTerm.toLowerCase());
    const isApplied = appliedJobs.includes(o.id);
    
    if (activeTab === 'postulaciones') {
      return matchesSearch && isApplied;
    }
    // En 'explorar', mostramos lo que NO está cerrado Y lo que aún NO hemos postulado
    return o.estado !== 'CERRADA' && matchesSearch && !isApplied;
  });

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-gray-500 animate-pulse">Buscando las mejores ofertas para ti...</p>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Ofertas Disponibles</h1>
          <p className="text-gray-500 text-sm mt-1">Encuentra turnos que calcen con tu perfil y ubicación.</p>
        </div>
        <div className="flex bg-white p-2 rounded-lg border border-gray-100 shadow-sm items-center gap-2">
          <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider ml-2">Tu Perfil:</span>
          <div className="w-24 h-2 bg-gray-100 rounded-full overflow-hidden">
            <div className="h-full bg-primary" style={{ width: `${profileCompletion}%` }}></div>
          </div>
          <span className="text-sm font-bold text-primary mr-2">{profileCompletion}%</span>
        </div>
      </div>

      <div className="bg-white p-1 rounded-xl shadow-sm border border-gray-100 flex mb-6">
        <button 
          onClick={() => setActiveTab('todas')}
          className={`flex-1 py-3 px-4 rounded-lg text-sm font-bold transition-all flex items-center justify-center gap-2 ${activeTab === 'todas' ? 'bg-primary text-white shadow-md' : 'text-gray-500 hover:bg-gray-50'}`}
        >
          <Search size={18} />
          Explorar Ofertas
        </button>
        <button 
          onClick={() => setActiveTab('postulaciones')}
          className={`flex-1 py-3 px-4 rounded-lg text-sm font-bold transition-all flex items-center justify-center gap-2 ${activeTab === 'postulaciones' ? 'bg-primary text-white shadow-md' : 'text-gray-500 hover:bg-gray-50'}`}
        >
          <CheckCircle2 size={18} />
          Mis Postulaciones ({appliedJobs.length})
        </button>
      </div>

      <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-4">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
          <input 
            type="text" 
            placeholder="Buscar por cargo (ej: Profesor, Guardia, Faenero)..." 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
          />
        </div>
        <div className="flex gap-2">
          <Button variant="primary" size="sm" className="gap-2 px-6">
            <Search className="w-4 h-4" />
            Buscar
          </Button>
        </div>
      </div>

      <div className="space-y-4">
        {filteredOfertas.length > 0 ? filteredOfertas.map((oferta: any) => {
          const matchScore = oferta.matchScore || 0;
          const isApplied = appliedJobs.includes(oferta.id);
          
          return (
            <Card 
              key={oferta.id} 
              className={`transition-all duration-300 relative overflow-hidden ${
                isApplied 
                  ? 'bg-blue-50/50 border-blue-200 shadow-inner' 
                  : 'bg-white hover:border-primary/30 hover:shadow-md'
              }`}
            >
              {isApplied && (
                <div className="absolute top-0 right-0 bg-blue-500 text-white text-[10px] font-black px-3 py-1 rounded-bl-lg uppercase tracking-widest shadow-sm z-10">
                  Ya Postulaste
                </div>
              )}
              <CardHeader className="flex justify-between items-start pb-2">
                <div>
                  <h3 className={`font-bold text-xl ${isApplied ? 'text-blue-900' : 'text-gray-900'}`}>{oferta.titulo}</h3>
                  <div className="flex items-center gap-4 text-sm text-gray-500 mt-1">
                    <span className="flex items-center gap-1 font-medium"><MapPin size={14} /> {oferta.ubicacion}</span>
                  </div>
                </div>
                {!isApplied && (
                  <Badge variant={matchScore > 80 ? "success" : "info"}>
                    Match: {matchScore}%
                  </Badge>
                )}
              </CardHeader>
              <CardContent className="py-2">
                <p className="text-gray-700 text-sm mb-4">{oferta.descripcion}</p>
                <div className="grid grid-cols-2 gap-4 text-sm bg-gray-50 p-3 rounded-md border border-gray-100">
                  <div>
                    <span className="text-gray-500 block text-xs uppercase tracking-wider font-semibold">Fecha Inicio</span>
                    <span className="font-medium text-gray-900">{oferta.fechaInicio}</span>
                  </div>
                  <div>
                    <span className="text-gray-500 block text-xs uppercase tracking-wider font-semibold">Pago Líquido</span>
                    <span className="font-bold text-green-700 text-base">${oferta.remuneracion.toLocaleString('es-CL')}</span>
                  </div>
                </div>
              </CardContent>
              <CardFooter className="flex justify-end gap-2 pt-3">
                <Button 
                  variant="outline"
                  onClick={() => setSelectedJobForMap(oferta)}
                  className="gap-2"
                >
                  <MapIcon size={16} /> Ver Mapa
                </Button>
                <Button 
                  variant={appliedJobs.includes(oferta.id) ? "outline" : "primary"}
                  disabled={appliedJobs.includes(oferta.id)}
                  onClick={() => handleApply(oferta.id)}
                  className="min-w-[140px] gap-2"
                >
                  {appliedJobs.includes(oferta.id) ? (
                    <>
                      <CheckCircle2 className="w-4 h-4" />
                      Postulado
                    </>
                  ) : (
                    'Postular Ahora'
                  )}
                </Button>
              </CardFooter>
            </Card>
          );
        }) : (
          <div className="text-center py-12 bg-gray-50 rounded-xl border-2 border-dashed border-gray-200">
            <p className="text-gray-500 italic">No se encontraron ofertas que coincidan con tu búsqueda.</p>
          </div>
        )}
      </div>

      {/* MODAL DEL MAPA DE DISTANCIA */}
      {selectedJobForMap && workerProfile && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-white rounded-[32px] max-w-2xl w-full shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
            <div className="p-8">
              <div className="flex justify-between items-center mb-6">
                <div>
                  <h3 className="text-2xl font-bold text-primary-dark">Ubicación del Trabajo</h3>
                  <p className="text-gray-500 text-sm">Calculando distancia desde tu ubicación guardada</p>
                </div>
                <button 
                  onClick={() => setSelectedJobForMap(null)}
                  className="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center text-gray-500 hover:bg-gray-200 transition-all"
                >
                  <X size={20} />
                </button>
              </div>

              {typeof workerProfile.latitude === 'number' && typeof workerProfile.longitude === 'number' && 
               typeof selectedJobForMap.latitude === 'number' && typeof selectedJobForMap.longitude === 'number' ? (
                <JobDistanceMap 
                  apiKey={GOOGLE_MAPS_API_KEY}
                  origin={{ 
                    lat: workerProfile.latitude, 
                    lng: workerProfile.longitude, 
                    label: "Tu Ubicación" 
                  }}
                  destination={{ 
                    lat: selectedJobForMap.latitude, 
                    lng: selectedJobForMap.longitude, 
                    label: selectedJobForMap.titulo 
                  }}
                />
              ) : (
                <div className="bg-amber-50 border border-amber-200 p-8 rounded-[32px] text-amber-700 text-center">
                  <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center mx-auto shadow-sm mb-4">
                    <MapPin className="w-8 h-8 opacity-50" />
                  </div>
                  <p className="font-bold text-xl mb-2">Datos Geográficos Incompletos</p>
                  <p className="text-sm max-w-sm mx-auto">
                    Para calcular la distancia, asegúrate de que **ambos** (tú y la empresa) hayan marcado su ubicación en el mapa.
                  </p>
                  <div className="mt-4 pt-4 border-t border-amber-100 flex justify-center gap-4 text-[10px] font-mono">
                    <span>Tú: {workerProfile.latitude ? '✅' : '❌'}</span>
                    <span>Trabajo: {selectedJobForMap.latitude ? '✅' : '❌'}</span>
                  </div>
                </div>
              )}

              <div className="mt-8 flex justify-end">
                <Button variant="primary" onClick={() => setSelectedJobForMap(null)} className="px-8">
                  Entendido
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
