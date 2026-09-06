"use client";

import React, { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { KeyRound, Loader2, ArrowRight, CheckCircle2, ChevronLeft, Mail, Lock } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { SecurityCaptcha } from '@/components/auth/SecurityCaptcha';
import { authApi } from '@/lib/api/auth';

export default function ForgotPasswordPage() {
  const router = useRouter();
  const [step, setStep] = useState<1 | 2>(1);
  const [email, setEmail] = useState('');
  const [captchaToken, setCaptchaToken] = useState('');
  const [otpCode, setOtpCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const handleRequestOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    if (!email) {
      setError('Por favor ingresa tu correo electrónico.');
      return;
    }

    if (!captchaToken) {
      setError('Por favor completa la verificación de seguridad (Captcha).');
      return;
    }

    setIsLoading(true);
    try {
      const res = await authApi.forgotPassword({ email, captchaToken });
      if (res.error) {
        setError(res.error || 'No fue posible enviar el código de recuperación.');
      } else {
        setSuccessMessage(res.data?.message || 'Código enviado a tu correo. Revisa tu bandeja de entrada.');
        setStep(2);
      }
    } catch (err) {
      setError('Error al conectar con el servidor.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!otpCode || otpCode.trim().length < 6) {
      setError('Ingresa el código numérico de 6 dígitos enviado a tu correo.');
      return;
    }

    if (!newPassword || newPassword.length < 6) {
      setError('La nueva contraseña debe tener al menos 6 caracteres.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('Las contraseñas no coinciden.');
      return;
    }

    setIsLoading(true);
    try {
      const res = await authApi.resetPassword({
        email,
        code: otpCode.trim(),
        newPassword
      });

      if (res.error) {
        setError(res.error || 'El código es inválido o ha expirado.');
      } else {
        setSuccessMessage('¡Contraseña restablecida exitosamente! Redirigiendo al inicio de sesión...');
        setTimeout(() => {
          router.push('/login');
        }, 2000);
      }
    } catch (err) {
      setError('Error al actualizar la contraseña.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex-grow flex items-center justify-center py-16 px-4 bg-slate-50">
      <motion.div
        initial={{ opacity: 0, y: 15 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-xl bg-white p-10 lg:p-14 rounded-3xl shadow-xl border border-gray-100 relative"
      >
        <div className="text-center mb-8">
          <div className="flex justify-center mb-5">
            <div className="p-4 rounded-2xl bg-amber-50 text-amber-600">
              <KeyRound size={40} />
            </div>
          </div>

          <h2 className="text-3xl font-bold text-slate-900 tracking-tight mb-2">
            Recuperar Contraseña
          </h2>
          <p className="text-slate-500 font-medium">
            {step === 1 
              ? 'Ingresa tu correo y valida la verificación para recibir un código de acceso.' 
              : 'Ingresa el código numérico de 6 dígitos enviado a tu correo y tu nueva clave.'}
          </p>
        </div>

        {successMessage && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="mb-6 p-4 text-sm text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-2xl font-bold flex items-center gap-3"
          >
            <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0" />
            <span>{successMessage}</span>
          </motion.div>
        )}

        {error && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="mb-6 p-4 text-sm text-red-600 bg-red-50 border border-red-100 rounded-2xl font-bold text-center"
          >
            {error}
          </motion.div>
        )}

        <AnimatePresence mode="wait">
          {step === 1 ? (
            <motion.form
              key="step1"
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 10 }}
              onSubmit={handleRequestOtp}
              className="space-y-6"
            >
              <div>
                <label htmlFor="email" className="block text-sm font-semibold text-slate-700 mb-2 ml-1">
                  Correo Electrónico
                </label>
                <div className="relative">
                  <Mail className="absolute left-4 top-4 text-slate-400 w-5 h-5" />
                  <input
                    id="email"
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full bg-white border border-slate-200 rounded-2xl pl-12 pr-5 py-4 focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 placeholder:text-slate-400 shadow-sm"
                    placeholder="tu@correo.com"
                  />
                </div>
              </div>

              <SecurityCaptcha 
                onVerify={(token) => setCaptchaToken(token)} 
                onReset={() => setCaptchaToken('')}
              />

              <button
                type="submit"
                disabled={isLoading || !captchaToken}
                className="w-full flex justify-center items-center py-5 px-6 rounded-2xl shadow-lg text-base font-bold text-white bg-primary hover:bg-primary-dark transition-all gap-2 disabled:opacity-50 shadow-primary/20 hover:-translate-y-0.5 active:translate-y-0"
              >
                {isLoading ? (
                  <Loader2 className="w-6 h-6 animate-spin" />
                ) : (
                  <>
                    <span>Enviar Código al Correo</span>
                    <ArrowRight size={20} />
                  </>
                )}
              </button>
            </motion.form>
          ) : (
            <motion.form
              key="step2"
              initial={{ opacity: 0, x: 10 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -10 }}
              onSubmit={handleResetPassword}
              className="space-y-6"
            >
              <div>
                <label htmlFor="otpCode" className="block text-sm font-semibold text-slate-700 mb-2 ml-1">
                  Código de Verificación (OTP)
                </label>
                <input
                  id="otpCode"
                  type="text"
                  maxLength={6}
                  value={otpCode}
                  onChange={(e) => setOtpCode(e.target.value)}
                  className="w-full bg-white border border-slate-200 rounded-2xl px-5 py-4 text-center text-2xl font-mono tracking-widest font-bold text-slate-900 focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all shadow-sm"
                  placeholder="123456"
                />
                <p className="mt-2 text-xs text-slate-400 font-medium ml-1">
                  Ingresa el código numérico de 6 dígitos enviado a <strong className="text-slate-700">{email}</strong>.
                </p>
              </div>

              <div>
                <label htmlFor="newPassword" className="block text-sm font-semibold text-slate-700 mb-2 ml-1">
                  Nueva Contraseña
                </label>
                <div className="relative">
                  <Lock className="absolute left-4 top-4 text-slate-400 w-5 h-5" />
                  <input
                    id="newPassword"
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    className="w-full bg-white border border-slate-200 rounded-2xl pl-12 pr-5 py-4 focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 placeholder:text-slate-400 shadow-sm"
                    placeholder="••••••••"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="confirmPassword" className="block text-sm font-semibold text-slate-700 mb-2 ml-1">
                  Confirmar Nueva Contraseña
                </label>
                <div className="relative">
                  <Lock className="absolute left-4 top-4 text-slate-400 w-5 h-5" />
                  <input
                    id="confirmPassword"
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="w-full bg-white border border-slate-200 rounded-2xl pl-12 pr-5 py-4 focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 placeholder:text-slate-400 shadow-sm"
                    placeholder="••••••••"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="w-full flex justify-center items-center py-5 px-6 rounded-2xl shadow-lg text-base font-bold text-white bg-emerald-600 hover:bg-emerald-700 transition-all gap-2 disabled:opacity-50 shadow-emerald-200 hover:-translate-y-0.5 active:translate-y-0"
              >
                {isLoading ? (
                  <Loader2 className="w-6 h-6 animate-spin" />
                ) : (
                  <>
                    <span>Restablecer Contraseña</span>
                    <CheckCircle2 size={20} />
                  </>
                )}
              </button>
            </motion.form>
          )}
        </AnimatePresence>

        <div className="mt-8 text-center border-t border-slate-100 pt-6">
          <Link
            href="/login"
            className="inline-flex items-center gap-2 text-sm font-bold text-primary hover:underline"
          >
            <ChevronLeft size={16} /> Volver al Inicio de Sesión
          </Link>
        </div>
      </motion.div>
    </div>
  );
}
