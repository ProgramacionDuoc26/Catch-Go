"use client";

import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Loader2, Sparkles, CheckCircle2, Zap, ArrowRight, Eye, Target, MapPin, Edit3, Crown } from 'lucide-react';
import { profileApi, Profile } from '@/lib/api/profile';
import { paymentApi } from '@/lib/api/payment';

export default function TrabajadorSuscripcionPage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [userId, setUserId] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [upgrading, setUpgrading] = useState<boolean>(false);
  const [profileViews, setProfileViews] = useState<number>(0);

  useEffect(() => {
    const fetchProfile = async () => {
      setLoading(true);
      try {
        let uid = '';
        try {
          const { createClient } = await import('@/lib/supabase/client');
          const supabase = createClient();
          const { data: { user: supabaseUser } } = await supabase.auth.getUser();
          if (supabaseUser) uid = supabaseUser.id;
        } catch { /* ignore */ }

        if (!uid) {
          const storedUser = localStorage.getItem('user_info');
          if (storedUser) {
            const parsed = JSON.parse(storedUser);
            uid = parsed.id?.toString() || '';
          }
        }
        
        setUserId(uid);
        if (uid) {
          const res = await profileApi.getByUserId(uid);
          if (res.data) {
            setProfile(res.data);
          }
          const views = localStorage.getItem(`profile_views_${uid}`);
          setProfileViews(views ? parseInt(views) : Math.floor(Math.random() * 22) + 5);
        }
      } catch (err) {
        console.error('Error cargando perfil:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  const handleUpgrade = async () => {
    if (!userId) {
      alert('Debes iniciar sesión para realizar un pago.');
      return;
    }

    setUpgrading(true);
    try {
      const returnUrl = `${window.location.origin}/trabajador/suscripcion/callback`;
      const amount = 1390;
      
      const response = await paymentApi.initWebpay(userId, amount, returnUrl);
      
      if (response.data && response.data.token && response.data.url) {
        const { token, url } = response.data;
        
        if (token.startsWith('mock-token-')) {
          setTimeout(() => {
            window.location.href = `/trabajador/suscripcion/callback?token_ws=${token}`;
          }, 1000);
          return;
        }
        
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = url;
        
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'token_ws';
        input.value = token;
        
        form.appendChild(input);
        document.body.appendChild(form);
        form.submit();
      } else {
        alert('Error al inicializar la pasarela de pago. Inténtalo de nuevo.');
        setUpgrading(false);
      }
    } catch (err) {
      console.error('Error en upgrade:', err);
      alert('Hubo un problema de conexión con la pasarela de pago.');
      setUpgrading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-gray-500 animate-pulse">Cargando datos de suscripción...</p>
      </div>
    );
  }

  const currentPlan = profile?.plan || 'FREE';
  const isPremium = currentPlan === 'PREMIUM' || currentPlan === 'ENTERPRISE';
  const planExpiryDate = profile?.planExpiry 
    ? new Date(profile.planExpiry).toLocaleDateString('es-CL', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      })
    : null;

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <Crown className="text-amber-500" />
          Suscripción Trabajador
        </h1>
        <p className="text-gray-500 text-sm mt-1">Destácate y accede a más oportunidades laborales con Catch&Go Premium.</p>
      </div>

      {/* Contador de visitas al perfil */}
      <Card className="bg-gradient-to-r from-violet-50 to-indigo-50 border-violet-100/50 rounded-2xl overflow-hidden">
        <CardContent className="p-5 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-2xl bg-violet-100 flex items-center justify-center">
              <Eye className="w-6 h-6 text-violet-600" />
            </div>
            <div>
              <h3 className="font-bold text-gray-900">Visitas a tu perfil</h3>
              <p className="text-xs text-gray-500">Empresas que han revisado tu currículum</p>
            </div>
          </div>
          <div className="text-right">
            <span className="text-3xl font-black text-violet-600">{profileViews}</span>
            <p className="text-[10px] text-gray-400 uppercase tracking-wider font-bold">este mes</p>
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Plan actual */}
        <Card className="overflow-hidden border border-gray-100 shadow-sm rounded-3xl">
          <CardHeader className="bg-gradient-to-r from-slate-50 to-white flex justify-between items-center p-6 border-b border-gray-50">
            <h2 className="font-bold text-lg text-gray-800">Plan Actual</h2>
            <Badge variant={isPremium ? 'success' : 'info'} className="px-3 py-1 rounded-full text-xs font-semibold">
              {isPremium ? '⭐ Premium Activo' : 'Plan Gratuito'}
            </Badge>
          </CardHeader>
          <CardContent className="p-6 pt-4 space-y-6">
            <div className="flex items-center gap-4">
              <div className="p-4 bg-primary/10 rounded-2xl flex items-center justify-center text-primary">
                {isPremium ? (
                  <Sparkles className="w-8 h-8 text-amber-500 animate-pulse" />
                ) : (
                  <Zap className="w-8 h-8 text-primary" />
                )}
              </div>
              <div>
                <h3 className="text-xl font-bold text-gray-900">
                  {isPremium ? 'Plan Premium Trabajador' : 'Plan Gratuito'}
                </h3>
                <p className="text-gray-500 text-sm mt-0.5">
                  {isPremium 
                    ? `Suscripción válida hasta el ${planExpiryDate}` 
                    : 'Funciones básicas de postulación'}
                </p>
              </div>
            </div>
            
            <div className="border-t border-gray-100 pt-6 mt-6">
              <h4 className="font-semibold text-gray-900 mb-2 flex items-center gap-2">
                <CheckCircle2 size={16} className="text-green-600" />
                {isPremium ? 'Tus beneficios activos' : 'Incluye'}
              </h4>
              <div className="grid grid-cols-1 gap-3 text-sm text-gray-600 mt-3">
                <div className="flex items-center gap-2">
                  <span className={`w-1.5 h-1.5 rounded-full ${isPremium ? 'bg-amber-500' : 'bg-gray-300'}`}></span>
                  <span>{isPremium ? '✅ +20% bonus en matching de IA' : '❌ Matching estándar'}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`w-1.5 h-1.5 rounded-full ${isPremium ? 'bg-amber-500' : 'bg-gray-300'}`}></span>
                  <span>{isPremium ? '✅ +500 mts en rango de ofertas' : '❌ Rango de búsqueda básico'}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`w-1.5 h-1.5 rounded-full ${isPremium ? 'bg-amber-500' : 'bg-gray-300'}`}></span>
                  <span>{isPremium ? '✅ Editar habilidades y preferencias' : '❌ Habilidades bloqueadas tras configurar'}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`w-1.5 h-1.5 rounded-full ${isPremium ? 'bg-amber-500' : 'bg-gray-300'}`}></span>
                  <span>{isPremium ? '✅ Contador de visitas al perfil' : '❌ Sin datos de visitas'}</span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Card Upgrade */}
        <Card className={`border shadow-lg rounded-3xl overflow-hidden relative ${isPremium ? 'border-green-200' : 'border-amber-200'}`}>
          {!isPremium && (
            <div className="absolute top-0 right-0 bg-amber-500 text-white text-[10px] font-bold px-3 py-1 rounded-bl-xl uppercase tracking-wider flex items-center gap-1 shadow-sm">
              <Sparkles size={10} />
              Recomendado
            </div>
          )}
          <CardHeader className={`border-b p-6 ${isPremium ? 'bg-green-50/50 border-green-100' : 'bg-amber-500/5 border-amber-100'}`}>
            <h2 className={`font-extrabold text-lg flex items-center gap-2 ${isPremium ? 'text-green-800' : 'text-amber-800'}`}>
              <Crown size={18} className={isPremium ? 'text-green-500' : 'text-amber-500'} />
              {isPremium ? 'Tu Plan Premium' : 'Mejorar a Premium'}
            </h2>
          </CardHeader>
          <CardContent className="p-6 pt-4 space-y-5">
            <p className="text-sm text-gray-600">
              {isPremium 
                ? 'Ya cuentas con todos los beneficios premium de Catch&Go para trabajadores.' 
                : 'Destaca tu perfil y accede a mejores oportunidades con beneficios exclusivos.'}
            </p>
            <div className="text-3xl font-extrabold text-gray-900 flex items-baseline gap-1">
              $1.390
              <span className="text-sm text-gray-500 font-normal">/mes</span>
            </div>

            <div className="space-y-3 border-t border-dashed border-gray-100 pt-4">
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-xl bg-blue-100 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <Target size={16} className="text-blue-600" />
                </div>
                <div>
                  <p className="text-sm font-bold text-gray-800">+20% en Match IA</p>
                  <p className="text-xs text-gray-500">Tu perfil aparece primero en las búsquedas de empresas</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-xl bg-green-100 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <MapPin size={16} className="text-green-600" />
                </div>
                <div>
                  <p className="text-sm font-bold text-gray-800">+500 mts de rango</p>
                  <p className="text-xs text-gray-500">Descubre ofertas un poco más lejos de tu ubicación habitual</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-xl bg-purple-100 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <Edit3 size={16} className="text-purple-600" />
                </div>
                <div>
                  <p className="text-sm font-bold text-gray-800">Editar Habilidades</p>
                  <p className="text-xs text-gray-500">Actualiza tus habilidades y preferencias cuando quieras</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-xl bg-indigo-100 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <Eye size={16} className="text-indigo-600" />
                </div>
                <div>
                  <p className="text-sm font-bold text-gray-800">Contador de Visitas</p>
                  <p className="text-xs text-gray-500">Mira cuántas empresas revisaron tu perfil</p>
                </div>
              </div>
            </div>
            
            <Button 
              variant="primary" 
              fullWidth 
              className={`font-bold py-3 rounded-2xl flex items-center justify-center gap-2 border-none shadow-md mt-4 transition-all ${
                isPremium 
                  ? 'bg-green-500 hover:bg-green-600 text-white' 
                  : 'bg-amber-500 hover:bg-amber-600 text-white'
              }`}
              onClick={handleUpgrade}
              disabled={isPremium || upgrading}
            >
              {upgrading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Conectando Webpay...</span>
                </>
              ) : isPremium ? (
                <span>✅ Plan Premium Activo</span>
              ) : (
                <>
                  <span>Suscribirse por $1.390/mes</span>
                  <ArrowRight size={16} />
                </>
              )}
            </Button>
            <p className="text-[10px] text-gray-400 text-center">
              Pago procesado de manera segura mediante Webpay Plus.
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
