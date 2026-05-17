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
