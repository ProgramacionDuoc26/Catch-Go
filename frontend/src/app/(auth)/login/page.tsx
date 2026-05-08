"use client";

import React, { useState } from 'react';
import Link from "next/link";
import { useRouter } from "next/navigation";
import { LogIn } from "lucide-react";

export default function LoginPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.id]: e.target.value });
    if (errors[e.target.id]) {
      setErrors({ ...errors, [e.target.id]: '' });
    }
  };

  // Botones de acceso rápido para desarrollo (sin validación)
  const handleDevEmpresa = () => {
    localStorage.setItem('user_info', JSON.stringify({ id: '1', nombre: 'Empresa Test', tipo: 'EMPRESA' }));
    router.push('/empresa/ofertas');
  };
  const handleDevTrabajador = () => {
    localStorage.setItem('user_info', JSON.stringify({ id: '12', nombre: 'Trabajador Test', tipo: 'TRABAJADOR' }));
    router.push('/trabajador/ofertas');
  };

  const [isLoading, setIsLoading] = useState(false);
  const [globalError, setGlobalError] = useState<string>('');

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
        
        // Redirigir según el tipo de usuario devuelto por el backend
        if (res.data.usuario?.tipo === 'EMPRESA') {
          router.push('/empresa/ofertas');
        } else {
          router.push('/trabajador/ofertas');
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
        },
      });
    } catch (error) {
      setGlobalError('Error al conectar con Google');
    }
  };

  return (
    <div className="flex-grow flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full bg-surface p-8 rounded-xl shadow-sm border border-gray-100">
        <div className="text-center mb-8">
          <h2 className="text-3xl font-bold text-text-main">Iniciar Sesión</h2>
          <p className="mt-2 text-sm text-text-muted">
            O <Link href="/register" className="text-primary font-medium hover:underline">crea una cuenta nueva</Link>
          </p>
        </div>
        
        <form className="space-y-6" onSubmit={handleLogin}>
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-text-main mb-2">Correo Electrónico</label>
            <input 
              id="email" 
              name="email" 
              type="email" 
              autoComplete="email" 
              value={formData.email}
              onChange={handleInputChange}
              className={`w-full border ${errors.email ? 'border-red-500' : 'border-gray-300'} rounded-md px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-shadow`}
              placeholder="ejemplo@correo.com"
            />
            {errors.email && <p className="mt-1 text-sm text-red-500">{errors.email}</p>}
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-text-main mb-2">Contraseña</label>
            <input 
              id="password" 
              name="password" 
              type="password" 
              autoComplete="current-password" 
              value={formData.password}
              onChange={handleInputChange}
              className={`w-full border ${errors.password ? 'border-red-500' : 'border-gray-300'} rounded-md px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-shadow`}
              placeholder="••••••••"
            />
            {errors.password && <p className="mt-1 text-sm text-red-500">{errors.password}</p>}
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <input id="remember-me" name="remember-me" type="checkbox" className="h-4 w-4 text-primary focus:ring-primary border-gray-300 rounded" />
              <label htmlFor="remember-me" className="ml-2 block text-sm text-text-muted">
                Recordarme
              </label>
            </div>
            <div className="text-sm">
              <a href="#" className="font-medium text-primary hover:text-primary-dark hover:underline">
                ¿Olvidaste tu contraseña?
              </a>
            </div>
          </div>

          {globalError && (
            <div className="p-3 text-sm text-red-700 bg-red-100 rounded-lg" role="alert">
              {globalError}
            </div>
          )}

          <button 
            type="submit"
            disabled={isLoading}
            className="w-full flex justify-center items-center py-4 px-4 border border-transparent rounded-md shadow-sm text-sm font-semibold text-white bg-primary hover:bg-primary-dark focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary min-h-[48px] transition-colors gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? (
              <span className="animate-pulse">Ingresando...</span>
            ) : (
              <>
                <LogIn className="w-5 h-5" />
                Ingresar
              </>
            )}
          </button>
        </form>

        <div className="mt-6">
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-300"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-2 bg-surface text-text-muted">O continuar con</span>
            </div>
          </div>

          <div className="mt-6">
            <button
              type="button"
              onClick={handleGoogleLogin}
              className="w-full flex justify-center items-center py-4 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-semibold text-text-main bg-white hover:bg-gray-50 focus:outline-none min-h-[48px] transition-colors"
            >
              <svg className="w-5 h-5 mr-2" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M22.56 12.25C22.56 11.47 22.49 10.72 22.36 10H12V14.26H17.92C17.67 15.63 16.89 16.79 15.73 17.57V20.31H19.31C21.4 18.38 22.56 15.57 22.56 12.25Z" fill="#4285F4"/>
                <path d="M12 23C14.97 23 17.46 22.02 19.31 20.31L15.73 17.57C14.73 18.24 13.48 18.66 12 18.66C9.14 18.66 6.71 16.73 5.84 14.15H2.15V17.01C3.96 20.61 7.7 23 12 23Z" fill="#34A853"/>
                <path d="M5.84 14.15C5.62 13.49 5.49 12.77 5.49 12C5.49 11.23 5.62 10.51 5.84 9.85V6.99H2.15C1.41 8.47 1 10.18 1 12C1 13.82 1.41 15.53 2.15 17.01L5.84 14.15Z" fill="#FBBC05"/>
                <path d="M12 5.34C13.62 5.34 15.07 5.9 16.21 6.98L19.39 3.8C17.45 2.01 14.97 1 12 1C7.7 1 3.96 3.39 2.15 6.99L5.84 9.85C6.71 7.27 9.14 5.34 12 5.34Z" fill="#EA4335"/>
              </svg>
              Google
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
