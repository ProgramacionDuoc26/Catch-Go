"use client";

import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Loader2, CreditCard, Sparkles, CheckCircle2, Zap, ArrowRight } from 'lucide-react';
import { profileApi, Profile } from '@/lib/api/profile';
import { paymentApi } from '@/lib/api/payment';

export default function EmpresaSuscripcionPage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [userId, setUserId] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [upgrading, setUpgrading] = useState<boolean>(false);

  useEffect(() => {
    const fetchProfile = async () => {
      setLoading(true);
      try {
        const storedUser = localStorage.getItem('user_info');
        if (storedUser) {
          const parsed = JSON.parse(storedUser);
          const uid = parsed.id?.toString() || '';
          setUserId(uid);
          
          if (uid) {
            const res = await profileApi.getByUserId(uid);
            if (res.data) {
              setProfile(res.data);
            }
          }
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
      // 1. Inicializar la transacción en el backend Java
      // Definimos la URL de retorno apuntando al callback en el frontend
      const returnUrl = `${window.location.origin}/empresa/suscripcion/callback`;
      const amount = 99000; // Plan Enterprise: $99.000 COP/CLP
      
      const response = await paymentApi.initWebpay(userId, amount, returnUrl);
      
      if (response.data && response.data.token && response.data.url) {
        const { token, url } = response.data;
        
        // 2. Realizar POST redirect a Transbank Webpay Plus de forma transparente
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

  const currentPlan = profile?.plan || 'TRIAL';
  const planExpiryDate = profile?.planExpiry 
    ? new Date(profile.planExpiry).toLocaleDateString('es-CL', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      })
    : 'No expira';

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <CreditCard className="text-primary" />
          Mi Suscripción
        </h1>
        <p className="text-gray-500 text-sm mt-1">Gestiona tu plan y método de pago a través de Webpay Plus.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="md:col-span-2 space-y-6">
          <Card className="overflow-hidden border border-gray-100 shadow-sm rounded-3xl">
            <CardHeader className="bg-gradient-to-r from-slate-50 to-white flex justify-between items-center p-6 border-b border-gray-50">
              <h2 className="font-bold text-lg text-gray-800">Plan Actual</h2>
              <Badge variant={currentPlan === 'ENTERPRISE' ? 'success' : 'info'} className="px-3 py-1 rounded-full text-xs font-semibold">
                {currentPlan === 'ENTERPRISE' ? 'Activo (Suscrito)' : 'Prueba Gratuita'}
              </Badge>
            </CardHeader>
            <CardContent className="p-6 pt-4 space-y-6">
              <div className="flex items-center gap-4">
                <div className="p-4 bg-primary/10 rounded-2xl flex items-center justify-center text-primary">
                  {currentPlan === 'ENTERPRISE' ? (
                    <Sparkles className="w-8 h-8 text-amber-500 animate-pulse" />
                  ) : (
                    <Zap className="w-8 h-8 text-primary" />
                  )}
                </div>
                <div>
                  <h3 className="text-xl font-bold text-gray-900">
                    Plan {currentPlan === 'ENTERPRISE' ? 'Enterprise' : 'Trial de 30 Días'}
                  </h3>
                  <p className="text-gray-500 text-sm mt-0.5">
                    {currentPlan === 'ENTERPRISE' 
                      ? `Suscripción válida hasta el ${planExpiryDate}` 
                      : 'Acceso temporal de demostración'}
                  </p>
                </div>
              </div>
              
              <div className="border-t border-gray-100 pt-6 mt-6">
                <h4 className="font-semibold text-gray-900 mb-2 flex items-center gap-2">
                  <CheckCircle2 size={16} className="text-green-600" />
                  Beneficios del Plan
                </h4>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm text-gray-600 mt-3">
                  <div className="flex items-center gap-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-primary"></span>
                    <span>{currentPlan === 'ENTERPRISE' ? 'Publicaciones ilimitadas' : 'Hasta 5 publicaciones mensuales'}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-primary"></span>
                    <span>Matching automático con IA</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-primary"></span>
                    <span>Soporte prioritario {currentPlan === 'ENTERPRISE' ? '24/7' : 'en horario hábil'}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-primary"></span>
                    <span>Verificación de OS10</span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-4">
          <Card className="border border-amber-200 shadow-lg rounded-3xl overflow-hidden relative">
            <div className="absolute top-0 right-0 bg-amber-500 text-white text-[10px] font-bold px-3 py-1 rounded-bl-xl uppercase tracking-wider flex items-center gap-1 shadow-sm">
              <Sparkles size={10} />
              Recomendado
            </div>
            <CardHeader className="bg-amber-500/5 border-b border-amber-100 p-6">
              <h2 className="font-extrabold text-amber-800 text-lg flex items-center gap-2">
                <Zap size={18} className="text-amber-500" />
                Mejorar Plan
              </h2>
            </CardHeader>
            <CardContent className="p-6 pt-4 space-y-4">
              <p className="text-sm text-gray-600">
                Actualiza al plan **Enterprise** hoy mismo para obtener todos los privilegios ilimitados y optimizar tu reclutamiento.
              </p>
              <div className="text-3xl font-extrabold text-gray-900 flex items-baseline gap-1">
                $99.000
                <span className="text-sm text-gray-500 font-normal">/mes</span>
              </div>
              <ul className="text-sm text-gray-600 space-y-2 border-t border-dashed border-gray-100 pt-3">
                <li className="flex items-center gap-2">
                  <CheckCircle2 size={14} className="text-amber-500" />
                  <span>Publicaciones ilimitadas</span>
                </li>
                <li className="flex items-center gap-2">
                  <CheckCircle2 size={14} className="text-amber-500" />
                  <span>Match instantáneo IA</span>
                </li>
                <li className="flex items-center gap-2">
                  <CheckCircle2 size={14} className="text-amber-500" />
                  <span>Soporte dedicado</span>
                </li>
              </ul>
              
              <Button 
                variant="primary" 
                fullWidth 
                className="bg-amber-500 hover:bg-amber-600 text-white font-bold py-3 rounded-2xl flex items-center justify-center gap-2 border-none shadow-md mt-4 transition-all"
                onClick={handleUpgrade}
                disabled={currentPlan === 'ENTERPRISE' || upgrading}
              >
                {upgrading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>Conectando Webpay...</span>
                  </>
                ) : currentPlan === 'ENTERPRISE' ? (
                  <span>Plan Máximo Activo</span>
                ) : (
                  <>
                    <span>Mejorar a Enterprise</span>
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
    </div>
  );
}
