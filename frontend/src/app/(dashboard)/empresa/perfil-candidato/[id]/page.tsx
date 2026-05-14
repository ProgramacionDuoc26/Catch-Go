"use client";

import React, { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { User, Calendar, Loader2, Briefcase, ChevronLeft } from 'lucide-react';
import { profileApi, Profile } from '@/lib/api/profile';

export default function PerfilCandidatoPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;

  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await profileApi.getByUserId(id);
        if (res.data) {
          setProfile(res.data);
        }
      } catch (error) {
        console.error('Error fetching profile:', error);
      } finally {
        setLoading(false);
      }
    };
    if (id) {
      fetchProfile();
    }
  }, [id]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-gray-500">Cargando perfil del candidato...</p>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="max-w-3xl mx-auto space-y-6">
        <Button variant="ghost" onClick={() => router.back()} className="gap-2 mb-4">
          <ChevronLeft size={16} /> Volver a Candidatos
        </Button>
        <div className="text-center py-20 bg-gray-50 rounded-xl border-2 border-dashed border-gray-200">
          <div className="max-w-xs mx-auto space-y-4">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto text-gray-400">
              <User size={32} />
            </div>
            <h3 className="text-lg font-medium text-gray-900">Perfil no encontrado</h3>
            <p className="text-gray-500 text-sm">Este trabajador aún no ha completado su perfil.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center gap-4 mb-4">
        <Button variant="ghost" onClick={() => router.back()} className="gap-2 p-0 px-2 h-auto">
          <ChevronLeft size={20} /> Volver
        </Button>
      </div>
      
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Perfil del Candidato</h1>
      </div>

      <div className="space-y-6">
        <Card>
          <CardHeader className="border-b bg-gray-50/50">
            <h2 className="text-lg font-semibold flex items-center gap-2">
              <User className="text-primary w-5 h-5" />
              Información Personal
            </h2>
          </CardHeader>
          <CardContent className="p-6">
            <div className="flex flex-col md:flex-row gap-8 items-start">
              <div className="w-32 h-32 rounded-full bg-gray-200 flex items-center justify-center border-4 border-white shadow-md overflow-hidden">
                {profile.photoUrl ? (
                  /* eslint-disable-next-line @next/next/no-img-element */
                  <img src={profile.photoUrl.startsWith('url_simulada_') ? '/placeholder-user.png' : profile.photoUrl} alt="Foto de Perfil del Candidato" className="w-full h-full object-cover" />
                ) : (
                  <User className="w-16 h-16 text-gray-400" />
                )}
              </div>

              <div className="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4 w-full">
                <div>
                  <label className="block text-sm font-medium text-gray-500 mb-1">Nombre Completo</label>
                  <p className="text-gray-900 font-medium">{profile.name || 'No especificado'}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-500 mb-1">Correo Electrónico</label>
                  <p className="text-gray-900 font-medium">{profile.email || 'No especificado'}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-500 mb-1">Fecha de Nacimiento</label>
                  <p className="text-gray-900 font-medium flex items-center gap-2">
                    <Calendar className="w-4 h-4 text-gray-400" />
                    {profile.birthDate || 'No especificada'}
                  </p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-500 mb-1">Teléfono</label>
                  <p className="text-gray-900 font-medium">{profile.phone || 'No especificado'}</p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="border-b bg-gray-50/50">
            <h2 className="text-lg font-semibold flex items-center gap-2">
              <Briefcase className="text-primary w-5 h-5" />
              Documentación y Currículum
            </h2>
          </CardHeader>
          <CardContent className="p-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="border border-gray-200 rounded-lg p-4 text-center">
                <h3 className="text-sm font-semibold text-gray-900 mb-2">Currículum Vitae</h3>
                {profile.cvUrl ? (
                  <Button variant="outline" size="sm" onClick={() => window.open(profile.cvUrl, '_blank')}>Ver Currículum</Button>
                ) : (
                  <p className="text-xs text-gray-500">No ha subido currículum</p>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
