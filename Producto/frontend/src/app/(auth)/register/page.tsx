"use client";

import React, { useState } from 'react';
import Link from "next/link";
import { useRouter } from "next/navigation";
import { UserPlus, Building2, User } from "lucide-react";

export default function RegisterPage() {
  const [accountType, setAccountType] = useState<'trabajador' | 'empresa'>('trabajador');
  const router = useRouter();

  const [formData, setFormData] = useState({
    name: '',
    rut: '',
    companyName: '',
    companyRut: '',
    email: '',
    confirmEmail: '',
    phone: '',
    password: ''
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.id]: e.target.value });
    // Limpiar el error cuando el usuario empieza a escribir de nuevo
    if (errors[e.target.id]) {
      setErrors({ ...errors, [e.target.id]: '' });
    }
  };

  const validateRut = (rut: string) => {
    // Basic RUT regex: 12345678-9 (sin puntos, con guión)
    return /^[0-9]+-[0-9kK]{1}$/.test(rut);
  };

  const validatePassword = (password: string) => {
    // Al menos 8 caracteres, 1 mayúscula, 1 número
    return /^(?=.*[A-Z])(?=.*\d).{8,}$/.test(password);
  };

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();
    const newErrors: Record<string, string> = {};

    if (accountType === 'trabajador') {
      if (!formData.name) newErrors.name = 'El nombre es obligatorio';
      if (!formData.rut || !validateRut(formData.rut)) newErrors.rut = 'RUT inválido (ej: 12345678-9 sin puntos)';
    } else {
      if (!formData.companyName) newErrors.companyName = 'La razón social es obligatoria';
      if (!formData.companyRut || !validateRut(formData.companyRut)) newErrors.companyRut = 'RUT inválido (ej: 76123456-7 sin puntos)';
    }

    if (!formData.email) newErrors.email = 'El correo es obligatorio';
    if (formData.email !== formData.confirmEmail) newErrors.confirmEmail = 'Los correos no coinciden';
    if (!formData.phone || formData.phone.length < 9) newErrors.phone = 'Teléfono inválido (mínimo 9 dígitos)';
    if (!formData.password || !validatePassword(formData.password)) {
      newErrors.password = 'La contraseña debe tener al menos 8 caracteres, 1 mayúscula y 1 número';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    if (accountType === 'trabajador') {
      router.push('/trabajador/ofertas');
    } else {
      router.push('/empresa/ofertas');
    }
  };

  return (
    <div className="flex-grow flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full bg-surface p-8 rounded-xl shadow-sm border border-gray-100">
        <div className="text-center mb-8">
          <h2 className="text-3xl font-bold text-text-main">Crea tu Cuenta</h2>
          <p className="mt-2 text-sm text-text-muted">
            O <Link href="/login" className="text-primary font-medium hover:underline">inicia sesión si ya tienes una</Link>
          </p>
        </div>
        
        <form className="space-y-6" onSubmit={handleRegister}>
          <div className="flex gap-4 mb-4">
            <label className="flex-1 cursor-pointer">
              <input 
                type="radio" 
                name="accountType" 
                value="trabajador" 
                className="peer sr-only" 
                checked={accountType === 'trabajador'}
                onChange={() => {
                  setAccountType('trabajador');
                  setErrors({});
                }}
              />
              <div className="p-4 text-center border rounded-md peer-checked:border-primary peer-checked:bg-blue-50 transition-colors flex flex-col items-center gap-2">
                <User className={`w-6 h-6 ${accountType === 'trabajador' ? 'text-primary' : 'text-gray-400'}`} />
                <div>
                  <span className="block font-semibold">Trabajador</span>
                  <span className="text-xs text-text-muted">Busco empleo</span>
                </div>
              </div>
            </label>
            <label className="flex-1 cursor-pointer">
              <input 
                type="radio" 
                name="accountType" 
                value="empresa" 
                className="peer sr-only" 
                checked={accountType === 'empresa'}
                onChange={() => {
                  setAccountType('empresa');
                  setErrors({});
                }}
              />
              <div className="p-4 text-center border rounded-md peer-checked:border-primary peer-checked:bg-blue-50 transition-colors flex flex-col items-center gap-2">
                <Building2 className={`w-6 h-6 ${accountType === 'empresa' ? 'text-primary' : 'text-gray-400'}`} />
                <div>
                  <span className="block font-semibold">Empresa</span>
                  <span className="text-xs text-text-muted">Busco talento</span>
                </div>
              </div>
            </label>
          </div>

          {accountType === 'trabajador' ? (
            <>
              <div>
                <label htmlFor="name" className="block text-sm font-medium text-text-main mb-2">Nombre Completo</label>
                <input id="name" type="text" value={formData.name} onChange={handleInputChange} className={`w-full border ${errors.name ? 'border-red-500' : 'border-gray-300'} rounded-md px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-shadow`} placeholder="Ej. Juan Pérez" />
                {errors.name && <p className="mt-1 text-sm text-red-500">{errors.name}</p>}
              </div>
              <div>
                <label htmlFor="rut" className="block text-sm font-medium text-text-main mb-2">RUT Personal</label>
                <input id="rut" type="text" value={formData.rut} onChange={handleInputChange} className={`w-full border ${errors.rut ? 'border-red-500' : 'border-gray-300'} rounded-md px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-shadow`} placeholder="12345678-9" />
                {errors.rut && <p className="mt-1 text-sm text-red-500">{errors.rut}</p>}
              </div>
            </>
          ) : (
            <>
              <div>
                <label htmlFor="companyName" className="block text-sm font-medium text-text-main mb-2">Razón Social de la Empresa</label>
                <input id="companyName" type="text" value={formData.companyName} onChange={handleInputChange} className={`w-full border ${errors.companyName ? 'border-red-500' : 'border-gray-300'} rounded-md px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-shadow`} placeholder="Ej. Constructora SPA" />
                {errors.companyName && <p className="mt-1 text-sm text-red-500">{errors.companyName}</p>}
              </div>
              <div>
                <label htmlFor="companyRut" className="block text-sm font-medium text-text-main mb-2">RUT Empresa</label>
                <input id="companyRut" type="text" value={formData.companyRut} onChange={handleInputChange} className={`w-full border ${errors.companyRut ? 'border-red-500' : 'border-gray-300'} rounded-md px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-shadow`} placeholder="76123456-7" />
                {errors.companyRut && <p className="mt-1 text-sm text-red-500">{errors.companyRut}</p>}
              </div>
            </>
          )}

          <div>
            <label htmlFor="email" className="block text-sm font-medium text-text-main mb-2">Correo Electrónico</label>
            <input id="email" type="email" value={formData.email} onChange={handleInputChange} className={`w-full border ${errors.email ? 'border-red-500' : 'border-gray-300'} rounded-md px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-shadow`} placeholder="correo@ejemplo.com" />
            {errors.email && <p className="mt-1 text-sm text-red-500">{errors.email}</p>}
          </div>

          <div>
            <label htmlFor="confirmEmail" className="block text-sm font-medium text-text-main mb-2">Confirmar Correo Electrónico</label>
            <input id="confirmEmail" type="email" value={formData.confirmEmail} onChange={handleInputChange} className={`w-full border ${errors.confirmEmail ? 'border-red-500' : 'border-gray-300'} rounded-md px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-shadow`} placeholder="correo@ejemplo.com" />
            {errors.confirmEmail && <p className="mt-1 text-sm text-red-500">{errors.confirmEmail}</p>}
          </div>

          <div>
            <label htmlFor="phone" className="block text-sm font-medium text-text-main mb-2">Teléfono de Contacto</label>
            <input id="phone" type="tel" value={formData.phone} onChange={handleInputChange} className={`w-full border ${errors.phone ? 'border-red-500' : 'border-gray-300'} rounded-md px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-shadow`} placeholder="+56 9 1234 5678" />
            {errors.phone && <p className="mt-1 text-sm text-red-500">{errors.phone}</p>}
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-text-main mb-2">Contraseña</label>
            <input id="password" type="password" value={formData.password} onChange={handleInputChange} className={`w-full border ${errors.password ? 'border-red-500' : 'border-gray-300'} rounded-md px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-shadow`} placeholder="••••••••" />
            {errors.password && <p className="mt-1 text-sm text-red-500">{errors.password}</p>}
          </div>

          <button 
            type="submit" 
            className="w-full flex justify-center items-center py-4 px-4 border border-transparent rounded-md shadow-sm text-sm font-semibold text-white bg-primary hover:bg-primary-dark focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary min-h-[48px] transition-colors gap-2"
          >
            <UserPlus className="w-5 h-5" />
            Registrarse de Prueba ({accountType === 'trabajador' ? 'Trabajador' : 'Empresa'})
          </button>
        </form>

        <div className="mt-6">
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-300"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-2 bg-surface text-text-muted">O registrarse con</span>
            </div>
          </div>

          <div className="mt-6">
            <button
              type="button"
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

