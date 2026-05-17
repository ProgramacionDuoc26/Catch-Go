"use client";

import React, { useEffect, useState, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { Card, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Loader2, CheckCircle, XCircle, AlertTriangle, CreditCard, ArrowRight, RefreshCw, Calendar } from 'lucide-react';
import { paymentApi, WebpayConfirmResponse } from '@/lib/api/payment';

export default function WebpayCallbackPage() {
  return (
    <Suspense fallback={
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-gray-500 animate-pulse">Esperando redirección de pago...</p>
      </div>
    }>
      <WebpayCallbackContent />
    </Suspense>
  );
}

function WebpayCallbackContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  
  const tokenWs = searchParams.get('token_ws');
  const tbkToken = searchParams.get('TBK_TOKEN');
  
  const [status, setStatus] = useState<'loading' | 'success' | 'failed' | 'aborted'>('loading');
  const [txDetails, setTxDetails] = useState<WebpayConfirmResponse | null>(null);
  const [errorMsg, setErrorMsg] = useState<string>('');

  useEffect(() => {
    const confirmPayment = async () => {
      // 1. Si el usuario canceló el pago en Transbank (retorna TBK_TOKEN)
      if (tbkToken && !tokenWs) {
        setStatus('aborted');
        return;
      }

      // 2. Si no hay token de ningún tipo en los parámetros
      if (!tokenWs) {
        setStatus('failed');
        setErrorMsg('No se detectó ningún token de pago. La transacción no pudo ser verificada.');
        return;
      }

      // 3. Si hay token_ws, llamar al backend Java para confirmación
      try {
        const response = await paymentApi.confirmWebpay(tokenWs);
        if (response.data && response.data.status === 'AUTHORIZED') {
          setStatus('success');
          setTxDetails(response.data);
          
          // Actualizar localStorage local para que se reconozca el nuevo plan de inmediato
          const storedUser = localStorage.getItem('user_info');
          if (storedUser) {
            const parsed = JSON.parse(storedUser);
            // Si el perfil está embebido, actualizarlo.
            if (parsed.perfil) {
              parsed.perfil.plan = 'ENTERPRISE';
              parsed.perfil.planExpiry = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString();
            }
            localStorage.setItem('user_info', JSON.stringify(parsed));
          }
        } else {
          setStatus('failed');
          setErrorMsg(
            response.data?.status === 'FAILED' 
              ? 'La transacción fue rechazada por el banco emisor o fondos insuficientes.' 
              : 'Error inesperado al validar el estado del pago con Transbank.'
          );
        }
      } catch (err) {
        console.error('Error al confirmar transacción:', err);
        setStatus('failed');
        setErrorMsg('Error de comunicación con el servicio de pagos. Por favor, verifica el estado de tu cuenta.');
      }
    };

    confirmPayment();
  }, [tokenWs, tbkToken]);

  // Pantalla de carga
  if (status === 'loading') {
    return (
      <div className="flex flex-col items-center justify-center min-h-[500px] max-w-lg mx-auto text-center space-y-6">
        <div className="relative flex items-center justify-center w-24 h-24">
          <div className="absolute inset-0 rounded-full border-4 border-primary/20 animate-pulse"></div>
          <Loader2 className="w-12 h-12 text-primary animate-spin" />
        </div>
        <div className="space-y-2">
          <h2 className="text-2xl font-extrabold text-gray-900">Verificando Pago</h2>
          <p className="text-gray-500 max-w-sm">
            Estamos validando tu pago con los servidores seguros de **Webpay Plus**. Por favor no cierres ni refresques esta pestaña.
          </p>
        </div>
      </div>
    );
  }

  // Pantalla de éxito premium
  if (status === 'success') {
    const formattedAmount = txDetails?.amount 
      ? new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(txDetails.amount)
      : '$99.000';

    return (
      <div className="min-h-[500px] max-w-xl mx-auto flex items-center justify-center py-8">
        <Card className="w-full border-none shadow-2xl rounded-[32px] overflow-hidden bg-white/80 backdrop-blur-md relative">
          {/* Fondo difuminado decorativo */}
          <div className="absolute -top-20 -left-20 w-48 h-48 bg-green-500/10 rounded-full blur-3xl"></div>
          <div className="absolute -bottom-20 -right-20 w-48 h-48 bg-primary/10 rounded-full blur-3xl"></div>
          
          <CardContent className="p-8 sm:p-10 text-center relative z-10 flex flex-col items-center space-y-6">
            {/* Animación del checkmark */}
            <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center text-green-600 shadow-inner animate-bounce">
              <CheckCircle size={44} className="stroke-[2.5]" />
            </div>

            <div className="space-y-1">
              <Badge variant="success" className="bg-green-100 text-green-700 font-bold px-3 py-1 rounded-full text-xs">
                Transacción Aprobada
              </Badge>
              <h2 className="text-3xl font-black text-gray-900 mt-2">¡Suscripción Activada!</h2>
              <p className="text-gray-500 text-sm max-w-xs mx-auto mt-1">
                Bienvenido al plan **Enterprise** de Match&Go. Tu cuenta ha sido actualizada con éxito.
              </p>
            </div>

            {/* Cuadro de Detalles Glassmorphism */}
            <div className="w-full bg-slate-50 border border-slate-100 rounded-[24px] p-6 space-y-3 text-left text-sm">
              <h3 className="font-bold text-gray-800 border-b border-gray-100 pb-2 mb-2 flex items-center gap-1.5">
                <CreditCard size={16} className="text-primary" />
                Detalle del Recibo
              </h3>
              <div className="flex justify-between">
                <span className="text-gray-500">Plan contratado</span>
                <span className="font-bold text-gray-800 uppercase">Enterprise Suscripción</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Monto Cobrado</span>
                <span className="font-extrabold text-primary">{formattedAmount}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Orden de compra</span>
                <span className="font-mono text-gray-700 font-bold">{txDetails?.buy_order || 'O-1002341'}</span>
              </div>
              {txDetails?.payment_type_code && (
                <div className="flex justify-between">
                  <span className="text-gray-500">Método de pago</span>
                  <span className="text-gray-700 font-medium">
                    Webpay Plus ({txDetails.payment_type_code === 'VD' ? 'Débito' : 'Crédito'})
                  </span>
                </div>
              )}
              <div className="flex justify-between border-t border-dashed border-gray-200 pt-3 mt-2">
                <span className="text-gray-500 flex items-center gap-1">
                  <Calendar size={14} />
                  Vencimiento
                </span>
                <span className="font-bold text-gray-800">
                  {new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toLocaleDateString('es-CL', {
                    day: '2-digit',
                    month: '2-digit',
                    year: 'numeric'
                  })}
                </span>
              </div>
            </div>

            <div className="w-full flex flex-col sm:flex-row gap-3 pt-2">
              <Button 
                variant="primary" 
                fullWidth 
                className="py-3.5 rounded-2xl font-bold flex items-center justify-center gap-2 shadow-lg hover:shadow-xl transition-all"
                onClick={() => router.push('/empresa/dashboard')}
              >
                <span>Ir al Dashboard</span>
                <ArrowRight size={16} />
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  // Pantalla de abortado / cancelado
  if (status === 'aborted') {
    return (
      <div className="min-h-[500px] max-w-md mx-auto flex items-center justify-center py-8">
        <Card className="w-full border border-amber-200 shadow-xl rounded-[32px] overflow-hidden bg-white">
          <CardContent className="p-8 text-center flex flex-col items-center space-y-6">
            <div className="w-20 h-20 bg-amber-100 rounded-full flex items-center justify-center text-amber-500 shadow-inner">
              <AlertTriangle size={40} className="stroke-[2]" />
            </div>

            <div className="space-y-2">
              <Badge variant="warning" className="bg-amber-100 text-amber-700 font-bold px-3 py-0.5 rounded-full text-xs">
                Pago Cancelado
              </Badge>
              <h2 className="text-2xl font-black text-gray-900 mt-2">Transacción Cancelada</h2>
              <p className="text-gray-500 text-sm max-w-xs mx-auto">
                Has abortado el proceso de pago de forma manual dentro del portal de Transbank Webpay.
              </p>
            </div>

            <div className="w-full space-y-2 pt-4">
              <Button 
                variant="primary" 
                fullWidth 
                className="bg-amber-500 hover:bg-amber-600 text-white py-3 rounded-2xl font-bold flex items-center justify-center gap-2"
                onClick={() => router.push('/empresa/suscripcion')}
              >
                <RefreshCw size={16} />
                <span>Reintentar Pago</span>
              </Button>
              <Button 
                variant="outline" 
                fullWidth 
                className="py-3 rounded-2xl font-bold"
                onClick={() => router.push('/empresa/dashboard')}
              >
                Volver a Inicio
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  // Pantalla de error / rechazo
  return (
    <div className="min-h-[500px] max-w-md mx-auto flex items-center justify-center py-8">
      <Card className="w-full border border-red-200 shadow-xl rounded-[32px] overflow-hidden bg-white">
        <CardContent className="p-8 text-center flex flex-col items-center space-y-6">
          <div className="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center text-red-500 shadow-inner">
            <XCircle size={40} className="stroke-[2]" />
          </div>

          <div className="space-y-2">
            <Badge variant="warning" className="bg-red-100 text-red-700 font-bold px-3 py-0.5 rounded-full text-xs">
              Transacción Rechazada
            </Badge>
            <h2 className="text-2xl font-black text-gray-900 mt-2">Error en la Transacción</h2>
            <p className="text-gray-500 text-sm max-w-xs mx-auto">
              {errorMsg || 'La transacción no pudo ser autorizada por el emisor. Por favor, reintenta.'}
            </p>
          </div>

          <div className="w-full space-y-2 pt-4">
            <Button 
              variant="primary" 
              fullWidth 
              className="bg-red-500 hover:bg-red-600 text-white py-3 rounded-2xl font-bold flex items-center justify-center gap-2"
              onClick={() => router.push('/empresa/suscripcion')}
            >
              <RefreshCw size={16} />
              <span>Volver a Reintentar</span>
            </Button>
            <Button 
              variant="outline" 
              fullWidth 
              className="py-3 rounded-2xl font-bold"
              onClick={() => router.push('/empresa/dashboard')}
            >
              Volver a Inicio
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
