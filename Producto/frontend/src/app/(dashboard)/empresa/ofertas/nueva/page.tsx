"use client";

import React, { useState } from 'react';
import Link from 'next/link';
import { Card, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { jobsApi } from '@/lib/api/jobs';
import { useRouter } from 'next/navigation';
import { Loader2 } from 'lucide-react';
import LocationPicker from '@/components/maps/LocationPicker';

const GOOGLE_MAPS_API_KEY = process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || '';

export default function NuevaOfertaPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    titulo: '',
    categoria: 'Guardia',
    remuneracion: 0,
    fechaInicio: '',
    fechaFin: '',
    descripcion: '',
    ubicacion: 'Santiago, RM',
    latitude: -33.4489,
    longitude: -70.6693
  });

  const today = new Date().toISOString().split('T')[0];

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const value = e.target.type === 'number' ? parseInt(e.target.value) : e.target.value;
    setFormData({ ...formData, [e.target.id]: value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      // Obtener el ID real de la empresa
      let realEmpresaId = '';
      
      // 1. Intentar con Supabase
      const { createClient } = await import('@/lib/supabase/client');
      const supabase = createClient();
      const { data: { user: supabaseUser } } = await supabase.auth.getUser();
      
      if (supabaseUser) {
        realEmpresaId = supabaseUser.id;
      } else {
        // 2. Intentar con localStorage
        const storedUser = localStorage.getItem('user_info');
        if (storedUser) {
          const parsed = JSON.parse(storedUser);
          realEmpresaId = parsed.id?.toString() || '';
        }
      }

      if (!realEmpresaId) {
        alert('Sesión no encontrada. Por favor inicia sesión nuevamente.');
        router.push('/login');
        return;
      }

      const response = await jobsApi.create({
        ...formData,
        empresaId: realEmpresaId
      });
      
      if (response.error) {
        alert('Error al publicar: ' + response.error);
      } else {
        alert('¡Oferta publicada con éxito!');
        router.push('/empresa/ofertas');
      }
    } catch (error) {
      console.error('Error creating offer:', error);
      alert('Error de conexión al publicar la oferta.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Publicar Nuevo Turno</h1>
        <p className="text-gray-500 text-sm mt-1">Completa los detalles para encontrar al worker ideal.</p>
      </div>

      <Card>
        <CardContent className="space-y-6 pt-6">
          <form className="space-y-4" onSubmit={handleSubmit}>
            <div className="space-y-2">
              <label htmlFor="titulo" className="block text-sm font-medium text-gray-700">Título del Turno/Oferta</label>
              <input 
                type="text" 
                id="titulo" 
                required
                value={formData.titulo}
                onChange={handleInputChange}
                className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
                placeholder="Ej. Guardia de Seguridad Turno Noche"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label htmlFor="categoria" className="block text-sm font-medium text-gray-700">Categoría</label>
                <select 
                  id="categoria" 
                  value={formData.categoria}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-md bg-white focus:ring-primary focus:border-primary font-medium"
                >
                  <optgroup label="Seguridad y Servicios">
                    <option>Guardia</option>
                    <option>Aseo / Limpieza</option>
                    <option>Seguridad Eventos</option>
                  </optgroup>
                  <optgroup label="Gastronomía y Eventos">
                    <option>Garzón</option>
                    <option>Cocina / Ayudante</option>
                    <option>Anfitrión(a) / Recepcionista</option>
                    <option>Bailarín(a)</option>
                    <option>Cantante Eventos</option>
                  </optgroup>
                  <optgroup label="Logística y Retail">
                    <option>Carga/Descarga</option>
                    <option>Reponedor</option>
                    <option>Operario de Bodega</option>
                    <option>Promotor(a)</option>
                    <option>Delivery</option>
                  </optgroup>
                  <optgroup label="Educación y Otros">
                    <option>Profesor</option>
                    <option>Tutor Particular</option>
                    <option>Faenero</option>
                    <option>Administrativo</option>
                  </optgroup>
                </select>
              </div>
              <div className="space-y-2">
                <label htmlFor="remuneracion" className="block text-sm font-medium text-gray-700">Remuneración Líquida ($)</label>
                <input 
                  type="number" 
                  id="remuneracion" 
                  required
                  value={formData.remuneracion}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
                  placeholder="35000"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label htmlFor="fechaInicio" className="block text-sm font-medium text-gray-700">Fecha de Inicio</label>
                <input 
                  type="date" 
                  id="fechaInicio" 
                  required
                  min={today}
                  value={formData.fechaInicio}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
                />
              </div>
              <div className="space-y-2">
                <label htmlFor="fechaFin" className="block text-sm font-medium text-gray-700">Fecha de Fin</label>
                <input 
                  type="date" 
                  id="fechaFin" 
                  min={formData.fechaInicio || today}
                  value={formData.fechaFin}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
                />
              </div>
            </div>

            <div className="space-y-2">
              <label htmlFor="descripcion" className="block text-sm font-medium text-gray-700">Descripción y Requisitos</label>
              <textarea 
                id="descripcion" 
                rows={4}
                required
                value={formData.descripcion}
                onChange={handleInputChange}
                className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary resize-none" 
                placeholder="Describe el trabajo y lista los requisitos mínimos (ej. OS10 vigente, zapatos de seguridad)..."
              ></textarea>
            </div>

            {/* MAPA DE UBICACION DEL TRABAJO */}
            <div className="pt-2">
              <LocationPicker 
                apiKey={GOOGLE_MAPS_API_KEY}
                initialLat={formData.latitude}
                initialLng={formData.longitude}
                onLocationChange={(lat, lng) => {
                  setFormData(prev => ({ ...prev, latitude: lat, longitude: lng }));
                }}
              />
            </div>

            <div className="pt-4 flex flex-col sm:flex-row gap-3">
              <Button variant="primary" fullWidth disabled={loading} type="submit">
                {loading ? <Loader2 className="animate-spin mr-2" /> : 'Publicar Oferta'}
              </Button>
              <Link href="/empresa/ofertas" className="w-full sm:w-auto">
                <Button variant="ghost" fullWidth type="button">Cancelar</Button>
              </Link>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
