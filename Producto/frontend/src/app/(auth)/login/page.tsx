"use client";

import React, { useState } from 'react';
import Link from "next/link";
import { useRouter } from "next/navigation";
import { LogIn, Loader2, ShieldCheck, ChevronLeft, ArrowRight } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";

export default function LoginPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [globalError, setGlobalError] = useState<string>('');
  const [isAdminMode, setIsAdminMode] = useState(false);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.id]: e.target.value });
    if (errors[e.target.id]) {
      setErrors({ ...errors, [e.target.id]: '' });
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setGlobalError('');
    if (!validateForm()) return;

    setIsLoading(true);
    try {
      const { authApi } = await import('@/lib/api/auth');
      const res = await authApi.login({
        email: formData.email,
        password: formData.password
      });

      if (res.error) {
        setGlobalError(res.error || 'Correo o contraseña incorrectos');
      } else if (res.data) {
        if (res.data.token) {
          localStorage.setItem('auth_token', res.data.token);
        }
        if (res.data.usuario) {
          localStorage.setItem('user_info', JSON.stringify(res.data.usuario));
        }
        
        if (res.data.usuario?.tipo === 'ADMIN') {
          router.push('/admin');
        } else if (res.data.usuario?.tipo === 'EMPRESA') {
          router.push('/empresa/perfil');
        } else {
          router.push('/trabajador/perfil');
        }
      }
    } catch (error) {
      setGlobalError('Error de conexión con el servidor');
    } finally {
      setIsLoading(false);
    }
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};
    if (!formData.email) newErrors.email = 'El correo es obligatorio';
    if (!formData.password) newErrors.password = 'La contraseña es obligatoria';
    
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return false;
    }
    return true;
  };

  const handleGoogleLogin = async () => {
    try {
      const { createClient } = await import('@/lib/supabase/client');
      const supabase = createClient();
      await supabase.auth.signInWithOAuth({
        provider: 'google',
        options: {
          redirectTo: `${window.location.origin}/auth/callback`,
          queryParams: {
            prompt: 'select_account',
            access_type: 'offline',
          }
        },
      });
    } catch (error) {
      setGlobalError('Error al conectar con Google');
    }
  };

  return (
    <div className="flex-grow flex items-center justify-center py-16 px-4 bg-slate-50 transition-colors duration-500">
      <motion.div 
        layout
        className={`w-full transition-all duration-700 ${isAdminMode ? 'max-w-md' : 'max-w-xl'} bg-white p-10 lg:p-14 rounded-3xl shadow-xl border border-gray-100 relative`}
      >
        <AnimatePresence mode="wait">
          <motion.div
            key={isAdminMode ? 'admin' : 'user'}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.3 }}
          >
            <div className="text-center mb-10">
              <div className="flex justify-center mb-6">
                <div className={`p-4 rounded-2xl transition-colors duration-500 ${isAdminMode ? 'bg-blue-50 text-blue-600' : 'bg-primary/5 text-primary'}`}>
                  {isAdminMode ? <ShieldCheck size={40} /> : <LogIn size={40} />}
                </div>
              </div>
              
              <h2 className="text-3xl font-bold text-slate-900 tracking-tight mb-2">
                {isAdminMode ? 'Panel Administrativo' : 'Iniciar Sesión'}
              </h2>
              <p className="text-slate-500 font-medium">
                {isAdminMode ? (
                  <button 
                    onClick={() => setIsAdminMode(false)}
                    className="flex items-center gap-1 mx-auto text-primary hover:gap-2 transition-all font-semibold"
                  >
                    <ChevronLeft size={18} /> Volver al inicio general
                  </button>
                ) : (
                  <>
                    Gestiona tus turnos y talento verificado.
                  </>
                )}
              </p>
            </div>
            
            <form className="space-y-6" onSubmit={handleLogin}>
              <div>
                <label htmlFor="email" className="block text-sm font-semibold text-slate-700 mb-2 ml-1">
                  {isAdminMode ? 'Correo de Administrador' : 'Correo Electrónico'}
                </label>
                <input 
                  id="email" 
                  name="email" 
                  type="email" 
                  autoComplete="email" 
                  value={formData.email}
                  onChange={handleInputChange}
                  className="w-full bg-white border border-slate-200 rounded-2xl px-5 py-4 focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 placeholder:text-slate-400 shadow-sm"
                  placeholder={isAdminMode ? "admin@catchandgo.cl" : "tu@correo.com"}
                />
                {errors.email && <p className="mt-2 text-sm text-red-500 font-semibold ml-1">{errors.email}</p>}
              </div>

              <div>
                <label htmlFor="password" className="block text-sm font-semibold text-slate-700 mb-2 ml-1">Contraseña</label>
                <input 
                  id="password" 
                  name="password" 
                  type="password" 
                  autoComplete="current-password" 
                  value={formData.password}
                  onChange={handleInputChange}
                  className="w-full bg-white border border-slate-200 rounded-2xl px-5 py-4 focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 placeholder:text-slate-400 shadow-sm"
                  placeholder="••••••••"
                />
                {errors.password && <p className="mt-2 text-sm text-red-500 font-semibold ml-1">{errors.password}</p>}
              </div>

              {!isAdminMode && (
                <div className="flex items-center justify-between px-1">
                  <div className="flex items-center">
                    <input id="remember-me" name="remember-me" type="checkbox" className="h-4 w-4 text-primary focus:ring-primary border-slate-300 rounded cursor-pointer" />
                    <label htmlFor="remember-me" className="ml-2 block text-sm font-medium text-slate-600 cursor-pointer">
                      Recordarme
                    </label>
                  </div>
                  <Link href="/register" className="text-sm font-semibold text-primary hover:underline">
                    ¿No tienes cuenta? Regístrate
                  </Link>
                </div>
              )}

              {globalError && (
                <motion.div 
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  className="p-4 text-sm text-red-600 bg-red-50 border border-red-100 rounded-2xl font-bold text-center" 
                  role="alert"
                >
                  {globalError}
                </motion.div>
              )}

              <button 
                type="submit"
                disabled={isLoading}
                className={`w-full flex justify-center items-center py-5 px-6 rounded-2xl shadow-lg text-base font-bold text-white transition-all gap-2 disabled:opacity-50 disabled:cursor-not-allowed
                  ${isAdminMode 
                    ? 'bg-slate-900 hover:bg-black shadow-slate-200' 
                    : 'bg-primary hover:bg-primary-dark shadow-primary/20 hover:-translate-y-0.5 active:translate-y-0'}`}
              >
                {isLoading ? (
                  <Loader2 className="w-6 h-6 animate-spin" />
                ) : (
                  <>
                    {isAdminMode ? 'Acceder al Panel' : 'Ingresar ahora'}
                    <ArrowRight size={20} className="ml-1" />
                  </>
                )}
              </button>
            </form>

            {!isAdminMode && (
              <div className="mt-10">
                <div className="relative">
                  <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-slate-100"></div>
                  </div>
                  <div className="relative flex justify-center text-xs uppercase font-bold tracking-widest">
                    <span className="px-4 bg-white text-slate-400 font-semibold">O continuar con</span>
                  </div>
                </div>

                <div className="mt-8">
                  <button
                    type="button"
                    onClick={handleGoogleLogin}
                    disabled={isLoading}
                    className="group relative w-full flex items-center justify-center gap-3 rounded-2xl border border-slate-200 bg-white px-6 py-4 font-bold text-slate-700 transition-all hover:bg-slate-50 hover:border-primary/30 hover:shadow-lg hover:shadow-primary/5 active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed overflow-hidden min-h-[60px]"
                  >
                    <div className="absolute inset-0 bg-gradient-to-r from-transparent via-primary/5 to-transparent translate-x-[-100%] group-hover:translate-x-[100%] transition-transform duration-1000" />
                    
                    {isLoading ? (
                      <Loader2 className="h-5 w-5 animate-spin text-primary" />
                    ) : (
                      <>
                        <svg className="w-5 h-5 transition-transform group-hover:scale-110" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <path d="M22.56 12.25C22.56 11.47 22.49 10.72 22.36 10H12V14.26H17.92C17.67 15.63 16.89 16.79 15.73 17.57V20.31H19.31C21.4 18.38 22.56 15.57 22.56 12.25Z" fill="#4285F4"/>
                          <path d="M12 23C14.97 23 17.46 22.02 19.31 20.31L15.73 17.57C14.73 18.24 13.48 18.66 12 18.66C9.14 18.66 6.71 16.73 5.84 14.15H2.15V17.01C3.96 20.61 7.7 23 12 23Z" fill="#34A853"/>
                          <path d="M5.84 14.15C5.62 13.49 5.49 12.77 5.49 12C5.49 11.23 5.62 10.51 5.84 9.85V6.99H2.15C1.41 8.47 1 10.18 1 12C1 13.82 1.41 15.53 2.15 17.01L5.84 14.15Z" fill="#FBBC05"/>
                          <path d="M12 5.34C13.62 5.34 15.07 5.9 16.21 6.98L19.39 3.8C17.45 2.01 14.97 1 12 1C7.7 1 3.96 3.39 2.15 6.99L5.84 9.85C6.71 7.27 9.14 5.34 12 5.34Z" fill="#EA4335"/>
                        </svg>
                        <span>Continuar con Google</span>
                      </>
                    )}
                  </button>
                </div>
              </div>
            )}

            <div className="mt-12 text-center">
              {!isAdminMode && (
                <button 
                  onClick={() => setIsAdminMode(true)}
                  className="text-xs font-bold text-slate-300 hover:text-primary transition-colors py-2 px-6 border border-transparent hover:border-slate-100 rounded-full"
                >
                  Configuración del Sistema
                </button>
              )}
            </div>
          </motion.div>
        </AnimatePresence>
      </motion.div>
    </div>
  );
}
