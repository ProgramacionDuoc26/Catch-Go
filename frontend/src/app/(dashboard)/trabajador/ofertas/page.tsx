"use client";

import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardFooter } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { jobsApi } from '@/lib/api/jobs';
import { Oferta } from '@/lib/api/types';
import { Search, Filter, CheckCircle2, Loader2, MapPin, Calendar } from 'lucide-react';

export default function TrabajadorOfertasPage() {
  const [ofertas, setOfertas] = useState<Oferta[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [appliedJobs, setAppliedJobs] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchOfertas = async () => {
      try {
        const response = await jobsApi.list();
        if (response.data) {
          setOfertas(response.data);
        }
      } catch (error) {
        console.error('Error fetching jobs:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchOfertas();
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
        alert('¡Postulación enviada con éxito!');
      } else {
        alert('Error: ' + response.error);
      }
    } catch (error) {
      console.error('Error applying:', error);
      alert('Error al enviar la postulación.');
    }
  };  // Simulación de completitud de perfil (esto vendría de un estado global o API)
  const [profileCompletion, setProfileCompletion] = useState(65); // 65% base

  const getMatchScore = (jobId: string) => {
    // El score base es la completitud del perfil
    // Sumamos un factor "aleatorio pero consistente" basado en el ID del trabajo
    const jobFactor = (parseInt(jobId) % 20); 
    const score = Math.min(100, profileCompletion + jobFactor);
    return score;
  };

  const filteredOfertas = ofertas.filter(o => 
    o.estado !== 'CERRADA' && 
    (o.titulo.toLowerCase().includes(searchTerm.toLowerCase()))
  );

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

      <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-4">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
          <input 
            type="text" 
            placeholder="Buscar por cargo (ej: Guardia, Garzón)..." 
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
        {filteredOfertas.length > 0 ? filteredOfertas.map((oferta) => {
          const matchScore = getMatchScore(oferta.id);
          return (
            <Card key={oferta.id} className="hover:border-primary/30 transition-all hover:shadow-md">
              <CardHeader className="flex justify-between items-start pb-2">
                <div>
                  <h3 className="font-semibold text-xl text-gray-900">{oferta.titulo}</h3>
                  <div className="flex items-center gap-4 text-sm text-gray-500 mt-1">
                    <span className="flex items-center gap-1 font-medium"><MapPin size={14} /> {oferta.ubicacion}</span>
                  </div>
                </div>
                <Badge variant={matchScore > 80 ? "success" : "info"}>Match: {matchScore}%</Badge>
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
    </div>
  );
}
