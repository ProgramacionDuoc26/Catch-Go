"use client";

import React, { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Briefcase, MapPin, Calendar, DollarSign, Save, X, Loader2 } from 'lucide-react';
import { jobsApi } from '@/lib/api/jobs';
import LocationPicker from '@/components/maps/LocationPicker';

const GOOGLE_MAPS_API_KEY = process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || '';

export default function EmpresaOfertaEditarPage() {
  const router = useRouter();
  const { id } = useParams();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  
  const [formData, setFormData] = useState({
    titulo: '',
    descripcion: '',
    ubicacion: '',
    remuneracion: 0,
    fechaInicio: '',
    empresaId: '',
    latitude: -33.4489,
    longitude: -70.6693
  });

  useEffect(() => {
    const fetchOffer = async () => {
      try {
        console.log('Fetching offer with ID:', id);
        const res = await jobsApi.getById(id as string);
        if (res.data) {
          console.log('Offer data received:', res.data);
          setFormData({
            titulo: res.data.titulo || '',
            descripcion: res.data.descripcion || '',
            ubicacion: res.data.ubicacion || '',
            remuneracion: res.data.remuneracion || 0,
            fechaInicio: res.data.fechaInicio || '',
            empresaId: res.data.empresaId || '',
            latitude: res.data.latitude || -33.4489,
            longitude: res.data.longitude || -70.6693
          });
        } else if (res.error) {
          console.error('API Error:', res.error);
          alert('Error: ' + res.error);
        }
      } catch (error) {
        console.error('Error fetching offer:', error);
        alert('No se pudo cargar la oferta');
        router.push('/empresa/ofertas');
      } finally {
        setLoading(false);
      }
    };
    fetchOffer();
  }, [id, router]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { id, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [id]: id === 'remuneracion' ? parseInt(value) || 0 : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const res = await jobsApi.update(id as string, formData);
      if (!res.error) {
        alert('Oferta actualizada con éxito');
        router.push('/empresa/ofertas');
      } else {
        alert('Error al actualizar: ' + JSON.stringify(res.error));
      }
    } catch (error) {
      console.error('Error updating offer:', error);
      alert('Error de conexión');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-gray-500">Cargando detalles de la oferta...</p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Editar Turno</h1>
        <Button variant="ghost" onClick={() => router.back()} className="gap-2">
          <X size={18} /> Cancelar
        </Button>
      </div>

      <form onSubmit={handleSubmit}>
        <Card>
          <CardHeader className="border-b bg-gray-50/50">
            <h2 className="text-lg font-semibold flex items-center gap-2">
              <Briefcase className="text-primary w-5 h-5" />
              Detalles del Turno
            </h2>
          </CardHeader>
          <CardContent className="p-6 space-y-4">
            <div>
              <label htmlFor="titulo" className="block text-sm font-medium text-gray-700 mb-1">Título del Puesto</label>
              <input id="titulo" type="text" required value={formData.titulo} onChange={handleInputChange} className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary" />
            </div>

            <div>
              <label htmlFor="descripcion" className="block text-sm font-medium text-gray-700 mb-1">Descripción de Tareas</label>
              <textarea id="descripcion" rows={4} required value={formData.descripcion} onChange={handleInputChange} className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary resize-none" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label htmlFor="ubicacion" className="block text-sm font-medium text-gray-700 mb-1">Ubicación</label>
                <div className="relative">
                  <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                  <input id="ubicacion" type="text" required value={formData.ubicacion} onChange={handleInputChange} className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" />
                </div>
              </div>
              <div>
                <label htmlFor="fechaInicio" className="block text-sm font-medium text-gray-700 mb-1">Fecha de Inicio</label>
                <div className="relative">
                  <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                  <input id="fechaInicio" type="date" required value={formData.fechaInicio} onChange={handleInputChange} className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" />
                </div>
              </div>
            </div>

            <div>
              <label htmlFor="remuneracion" className="block text-sm font-medium text-gray-700 mb-1">Pago Líquido ($)</label>
              <div className="relative">
                <DollarSign className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                <input id="remuneracion" type="number" required value={formData.remuneracion} onChange={handleInputChange} className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" />
              </div>
            </div>

            <div className="pt-2">
              <label className="block text-sm font-medium text-gray-700 mb-2">Ubicación exacta en el mapa</label>
              <LocationPicker 
                apiKey={GOOGLE_MAPS_API_KEY}
                initialLat={formData.latitude}
                initialLng={formData.longitude}
                onLocationChange={(lat, lng) => {
                  setFormData(prev => ({ ...prev, latitude: lat, longitude: lng }));
                }}
              />
            </div>
          </CardContent>
        </Card>

        <div className="mt-6 flex justify-end gap-3">
          <Button type="button" variant="outline" onClick={() => router.back()}>Descartar</Button>
          <Button type="submit" variant="primary" disabled={saving} className="gap-2 px-8">
            {saving ? <Loader2 className="animate-spin" /> : <Save size={18} />}
            {saving ? 'Guardando...' : 'Actualizar Oferta'}
          </Button>
        </div>
      </form>
    </div>
  );
}
