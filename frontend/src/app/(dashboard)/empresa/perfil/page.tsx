"use client";

import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Building2, Camera, MapPin, Globe, Save, Loader2, Mail, Phone } from 'lucide-react';
import { profileApi, Profile } from '@/lib/api/profile';

export default function EmpresaPerfilPage() {
  const [formData, setFormData] = useState<Profile>({
    userId: '2', // Hardcoded for Empresa test
    name: '',
    email: '',
    phone: '',
    birthDate: '', // Not used for company but required by type
    description: '',
    type: 'EMPRESA'
  });

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await profileApi.getByUserId('2');
        if (res.data) {
          setFormData({
            ...res.data,
            name: res.data.name || '',
            email: res.data.email || '',
            phone: res.data.phone || '',
            description: res.data.description || ''
          });
        } else {
          // Si no hay perfil, traer datos de la cuenta
          const { authApi } = await import('@/lib/api/auth');
          const authRes = await authApi.getUserById('2');
          if (authRes.data) {
            setFormData(prev => ({
              ...prev,
              name: authRes.data.nombre || '',
              email: authRes.data.email || '',
              phone: authRes.data.telefono || ''
            }));
          }
        }
      } catch (error) {
        console.error('Error fetching profile:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { id, value } = e.target;
    setFormData(prev => ({ ...prev, [id]: value }));
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const res = await profileApi.save(formData);
      if (!res.error) {
        alert('Información de la empresa actualizada en la base de datos');
      } else {
        alert('Error al guardar: ' + JSON.stringify(res.error));
      }
    } catch (error) {
      console.error('Error saving profile:', error);
      alert('Error de conexión');
    } finally {
      setSaving(false);
    }
  };

  const handlePhotoUpload = () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = (e: any) => {
      const file = e.target.files[0];
      if (file) {
        setFormData(prev => ({ ...prev, photoUrl: 'url_logo_simulada_' + file.name }));
        alert('Logo seleccionado: ' + file.name + '. Se guardará al presionar "Guardar Cambios".');
      }
    };
    input.click();
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-gray-500">Cargando datos corporativos...</p>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-8 pb-20">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Perfil de Empresa</h1>
        <p className="text-gray-500">Gestiona la identidad y datos de contacto de tu organización.</p>
      </div>

      <form onSubmit={handleSave} className="space-y-6">
        <Card>
          <CardHeader className="border-b bg-gray-50/50">
            <h2 className="text-lg font-semibold flex items-center gap-2">
              <Building2 className="text-primary w-5 h-5" />
              Datos de la Organización
            </h2>
          </CardHeader>
          <CardContent className="p-6">
            <div className="flex flex-col md:flex-row gap-8 items-start">
              <div className="relative group">
                <div className="w-32 h-32 rounded-lg bg-gray-100 flex items-center justify-center border-2 border-dashed border-gray-300 overflow-hidden">
                  {formData.photoUrl ? (
                    <Building2 className="w-12 h-12 text-primary" />
                  ) : (
                    <Building2 className="w-12 h-12 text-gray-300" />
                  )}
                </div>
                <button 
                  type="button" 
                  onClick={handlePhotoUpload}
                  className="absolute -bottom-2 -right-2 p-2 bg-primary text-white rounded-md shadow-lg hover:bg-primary-dark transition-colors"
                >
                  <Camera size={18} />
                </button>
              </div>

              <div className="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4 w-full">
                <div className="md:col-span-2">
                  <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">Nombre de la Empresa / Razón Social</label>
                  <input id="name" type="text" value={formData.name} onChange={handleInputChange} placeholder="Ej: Tech Solutions SpA" className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary" />
                </div>
                <div>
                  <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">Correo Corporativo</label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <input id="email" type="email" value={formData.email} onChange={handleInputChange} className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" />
                  </div>
                </div>
                <div>
                  <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-1">Teléfono de Contacto</label>
                  <div className="relative">
                    <Phone className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <input id="phone" type="text" value={formData.phone} onChange={handleInputChange} className="w-full border border-gray-300 rounded-md pl-10 pr-4 py-2 focus:ring-primary focus:border-primary" />
                  </div>
                </div>
              </div>
            </div>

            <div className="mt-6">
              <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">Descripción de la Empresa</label>
              <textarea id="description" rows={4} value={formData.description} onChange={handleInputChange} placeholder="Cuéntanos a qué se dedica tu empresa..." className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary resize-none" />
            </div>
          </CardContent>
        </Card>

        <div className="flex justify-end">
          <Button type="submit" variant="primary" disabled={saving} className="gap-2 px-8 py-6 text-lg">
            {saving ? <Loader2 className="animate-spin" /> : <Save size={20} />}
            {saving ? 'Guardando...' : 'Guardar Información'}
          </Button>
        </div>
      </form>
    </div>
  );
}
