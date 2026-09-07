"use client";

import React, { useState } from 'react';
import Link from "next/link";
import { useRouter } from "next/navigation";
import { UserPlus, Building2, User, Eye, EyeOff, CheckCircle2, ArrowRight, ShieldCheck, Zap, Loader2 } from "lucide-react";
import { motion, AnimatePresence } from 'framer-motion';
import { SecurityCaptcha } from '@/components/auth/SecurityCaptcha';

export default function RegisterPage() {
  const [accountType, setAccountType] = useState<'trabajador' | 'empresa'>('trabajador');
  const router = useRouter();

  const [formData, setFormData] = useState({
    name: '',
    rut: '',
    companyName: '',
    companyRut: '',
    email: '',
    phone: '+56 ',
    password: '',
    confirmPassword: ''
  });

  const [captchaToken, setCaptchaToken] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [globalError, setGlobalError] = useState<string>('');
  const [showTermsModal, setShowTermsModal] = useState(false);
  const [showOtpModal, setShowOtpModal] = useState(false);
  const [otpCode, setOtpCode] = useState('');
  const [isSendingOtp, setIsSendingOtp] = useState(false);
  const [otpError, setOtpError] = useState('');

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    let value = e.target.value;
    if (e.target.id === 'phone') {
      if (!value.startsWith('+56 ')) value = '+56 ';
      const numbers = value.slice(4).replace(/\D/g, '').slice(0, 9);
      value = '+56 ' + numbers;
    }
    setFormData({ ...formData, [e.target.id]: value });
    if (errors[e.target.id]) setErrors({ ...errors, [e.target.id]: '' });
  };

  const validateRut = (rut: string) => /^[0-9]+-[0-9kK]{1}$/.test(rut);
  const validatePassword = (password: string) => /^(?=.*[A-Z])(?=.*\d).{8,}$/.test(password);

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();
    setGlobalError('');
    const newErrors: Record<string, string> = {};

    if (accountType === 'trabajador') {
      if (!formData.name) newErrors.name = 'El nombre es obligatorio';
      if (!formData.rut || !validateRut(formData.rut)) newErrors.rut = 'RUT inválido (ej: 12345678-9)';
    } else {
      if (!formData.companyName) newErrors.companyName = 'La razón social es obligatoria';
      if (!formData.companyRut || !validateRut(formData.companyRut)) newErrors.companyRut = 'RUT inválido (ej: 76123456-7)';
    }

    if (!formData.email) newErrors.email = 'El correo es obligatorio';
    if (!formData.phone || formData.phone.length < 13) newErrors.phone = 'Teléfono incompleto';
    if (!formData.password || !validatePassword(formData.password)) newErrors.password = 'Mínimo 8 caracteres, 1 mayúscula y 1 número';
    if (formData.password !== formData.confirmPassword) newErrors.confirmPassword = 'Las contraseñas no coinciden';

    if (!captchaToken) {
      setGlobalError('Por favor completa la verificación de seguridad (Captcha).');
      return;
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setShowTermsModal(true);
  };

  const handleAcceptTerms = async () => {
    setShowTermsModal(false);
    setIsSendingOtp(true);
    setOtpError('');
    try {
      const { authApi } = await import('@/lib/api/auth');
      await authApi.sendOtp({ email: formData.email, purpose: 'REGISTER' });
      setShowOtpModal(true);
    } catch (err: any) {
      setGlobalError('Error al enviar el código de verificación al correo');
    } finally {
      setIsSendingOtp(false);
    }
  };

  const proceedWithRegistration = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!otpCode || otpCode.length < 6) {
      setOtpError('Ingresa el código de 6 dígitos');
      return;
    }
    setOtpError('');
    setIsLoading(true);
    try {
      const { authApi } = await import('@/lib/api/auth');
      const res = await authApi.register({
        email: formData.email,
        password: formData.password,
        nombre: accountType === 'trabajador' ? formData.name : formData.companyName,
        tipo: accountType.toUpperCase() as 'TRABAJADOR' | 'EMPRESA',
        telefono: formData.phone,
        captchaToken,
        otpCode
      });
      
      if (res.error) {
        setOtpError(res.error || 'Ocurrió un error en el registro');
      } else {
        setShowOtpModal(false);
        if (res.data?.token) localStorage.setItem('auth_token', res.data.token);
        if (res.data?.usuario) {
          const userWithRut = { 
            ...res.data.usuario, 
            rut: accountType === 'trabajador' ? formData.rut : formData.companyRut 
          };
          localStorage.setItem('user_info', JSON.stringify(userWithRut));
        }
        router.push(accountType === 'trabajador' ? '/trabajador/perfil' : '/empresa/perfil');
      }
    } catch (err: any) {
      setOtpError('Error de red al intentar registrar la cuenta');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeclineTerms = () => {
    setShowTermsModal(false);
    setGlobalError('Solicitud rechazada: Debe aceptar los términos y condiciones de privacidad de datos (Ley 19.628) para registrarse.');
  };

  return (
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center py-12 px-4 bg-gray-50/50">
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="max-w-5xl w-full grid grid-cols-1 lg:grid-cols-5 bg-white rounded-3xl shadow-2xl shadow-primary/5 border border-gray-100"
      >
        {/* LADO IZQUIERDO: Info y Branding */}
        <div className={`lg:col-span-2 p-8 lg:p-12 text-white flex flex-col justify-between transition-colors duration-500 rounded-t-3xl lg:rounded-tr-none lg:rounded-l-3xl ${accountType === 'trabajador' ? 'bg-primary' : 'bg-slate-900'}`}>
          <div>
            <div className="flex items-center gap-2 mb-8">
              <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center backdrop-blur-sm">
                <Zap className="text-white fill-white" size={24} />
              </div>
              <span className="text-2xl font-black tracking-tighter">CATCH-GO</span>
            </div>
            
            <AnimatePresence mode="wait">
              <motion.div
                key={accountType}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                transition={{ duration: 0.3 }}
              >
                <h2 className="text-4xl font-bold leading-tight mb-6">
                  {accountType === 'trabajador' 
                    ? 'Únete a la red de trabajadores más grande.' 
                    : 'Encuentra al personal ideal para tu negocio.'}
                </h2>
                
                <div className="space-y-4">
                  {[
                    accountType === 'trabajador' ? 'Acceso a turnos inmediatos' : 'Publicación de ofertas en segundos',
                    accountType === 'trabajador' ? 'Pagos garantizados y rápidos' : 'Gestión eficiente de postulantes',
                    'Verificación de identidad segura'
                  ].map((text, i) => (
                    <div key={i} className="flex items-center gap-3 text-white/80">
                      <CheckCircle2 size={20} className="text-white/40" />
                      <span className="font-medium">{text}</span>
                    </div>
                  ))}
                </div>
              </motion.div>
            </AnimatePresence>
          </div>

          <div className="mt-12 pt-12 border-t border-white/10">
            <div className="flex items-center gap-4">
              <div className="flex -space-x-2">
                {[1,2,3].map(i => (
                  <div key={i} className="w-10 h-10 rounded-full border-2 border-white/20 bg-white/10" />
                ))}
              </div>
              <p className="text-sm text-white/60 font-medium">
                Más de <span className="text-white">+5,000</span> usuarios ya confían en nosotros.
              </p>
            </div>
          </div>
        </div>

        {/* LADO DERECHO: Formulario */}
        <div className="lg:col-span-3 p-8 lg:p-12">
          <div className="flex justify-between items-center mb-8">
            <div>
              <h3 className="text-2xl font-bold text-gray-900">Crear Cuenta</h3>
              <p className="text-gray-500 text-sm mt-1">Empieza hoy mismo tu camino con Catch-Go</p>
            </div>
            <Link href="/login" className="text-sm font-bold text-primary hover:text-primary-dark transition-colors flex items-center gap-1">
              ¿Ya tienes cuenta? <ArrowRight size={16} />
            </Link>
          </div>

          {/* Selector de tipo con animación */}
          <div className="flex p-1 bg-gray-100 rounded-2xl mb-8 relative">
            <motion.div 
              className="absolute top-1 bottom-1 bg-white rounded-xl shadow-sm z-0"
              initial={false}
              animate={{ 
                left: accountType === 'trabajador' ? '4px' : '50%',
                right: accountType === 'trabajador' ? '50%' : '4px'
              }}
              transition={{ type: 'spring', stiffness: 300, damping: 30 }}
            />
            <button 
              type="button"
              onClick={() => {
                setAccountType('trabajador');
                setErrors({});
                setGlobalError('');
              }}
              className={`flex-1 flex items-center justify-center gap-2 py-3 text-sm font-bold z-10 transition-colors ${accountType === 'trabajador' ? 'text-primary' : 'text-gray-500'}`}
            >
              <User size={18} /> Trabajador
            </button>
            <button 
              type="button"
              onClick={() => {
                setAccountType('empresa');
                setErrors({});
                setGlobalError('');
              }}
              className={`flex-1 flex items-center justify-center gap-2 py-3 text-sm font-bold z-10 transition-colors ${accountType === 'empresa' ? 'text-primary' : 'text-gray-500'}`}
            >
              <Building2 size={18} /> Empresa
            </button>
          </div>

          <form onSubmit={handleRegister} className="space-y-5">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
              <AnimatePresence mode="wait">
                {accountType === 'trabajador' ? (
                  <motion.div 
                    key="trabajador-fields"
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -10 }}
                    className="md:col-span-2 grid grid-cols-1 md:grid-cols-2 gap-5"
                  >
                    <div className="md:col-span-1">
                      <label className="block text-xs font-bold uppercase tracking-wider text-gray-400 mb-2">Nombre Completo</label>
                      <input id="name" type="text" value={formData.name} onChange={handleInputChange} 
                        className={`w-full border ${errors.name ? 'border-red-500' : 'border-gray-200'} rounded-xl px-4 py-3 focus:ring-2 focus:ring-primary/20 outline-none transition-all`} placeholder="Juan Pérez" />
                      {errors.name && <p className="mt-1 text-xs text-red-500">{errors.name}</p>}
                    </div>
                    <div className="md:col-span-1">
                      <label className="block text-xs font-bold uppercase tracking-wider text-gray-400 mb-2">RUT</label>
                      <input id="rut" type="text" value={formData.rut} onChange={handleInputChange}
                        className={`w-full border ${errors.rut ? 'border-red-500' : 'border-gray-200'} rounded-xl px-4 py-3 focus:ring-2 focus:ring-primary/20 outline-none transition-all`} placeholder="12345678-9" />
                      {errors.rut && <p className="mt-1 text-xs text-red-500">{errors.rut}</p>}
                    </div>
                  </motion.div>
                ) : (
                  <motion.div 
                    key="empresa-fields"
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -10 }}
                    className="md:col-span-2 grid grid-cols-1 md:grid-cols-2 gap-5"
                  >
                    <div className="md:col-span-1">
                      <label className="block text-xs font-bold uppercase tracking-wider text-gray-400 mb-2">Razón Social</label>
                      <input id="companyName" type="text" value={formData.companyName} onChange={handleInputChange}
                        className={`w-full border ${errors.companyName ? 'border-red-500' : 'border-gray-200'} rounded-xl px-4 py-3 focus:ring-2 focus:ring-primary/20 outline-none transition-all`} placeholder="Empresa S.A." />
                      {errors.companyName && <p className="mt-1 text-xs text-red-500">{errors.companyName}</p>}
                    </div>
                    <div className="md:col-span-1">
                      <label className="block text-xs font-bold uppercase tracking-wider text-gray-400 mb-2">RUT Empresa</label>
                      <input id="companyRut" type="text" value={formData.companyRut} onChange={handleInputChange}
                        className={`w-full border ${errors.companyRut ? 'border-red-500' : 'border-gray-200'} rounded-xl px-4 py-3 focus:ring-2 focus:ring-primary/20 outline-none transition-all`} placeholder="76123456-7" />
                      {errors.companyRut && <p className="mt-1 text-xs text-red-500">{errors.companyRut}</p>}
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>

              <div className="md:col-span-1">
                <label className="block text-xs font-bold uppercase tracking-wider text-gray-400 mb-2">Email</label>
                <input id="email" type="email" value={formData.email} onChange={handleInputChange}
                  className={`w-full border ${errors.email ? 'border-red-500' : 'border-gray-200'} rounded-xl px-4 py-3 focus:ring-2 focus:ring-primary/20 outline-none transition-all`} placeholder="correo@ejemplo.com" />
                {errors.email && <p className="mt-1 text-xs text-red-500">{errors.email}</p>}
              </div>
              <div className="md:col-span-1">
                <label className="block text-xs font-bold uppercase tracking-wider text-gray-400 mb-2">Teléfono</label>
                <input id="phone" type="tel" value={formData.phone} onChange={handleInputChange}
                  className={`w-full border ${errors.phone ? 'border-red-500' : 'border-gray-200'} rounded-xl px-4 py-3 focus:ring-2 focus:ring-primary/20 outline-none transition-all`} placeholder="+56 9 1234 5678" />
                {errors.phone && <p className="mt-1 text-xs text-red-500">{errors.phone}</p>}
              </div>

              <div className="md:col-span-1">
                <label className="block text-xs font-bold uppercase tracking-wider text-gray-400 mb-2">Contraseña</label>
                <div className="relative">
                  <input id="password" type={showPassword ? "text" : "password"} value={formData.password} onChange={handleInputChange}
                    className={`w-full border ${errors.password ? 'border-red-500' : 'border-gray-200'} rounded-xl px-4 py-3 pr-12 focus:ring-2 focus:ring-primary/20 outline-none transition-all`} placeholder="••••••••" />
                  <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-primary transition-colors">
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
                {errors.password && <p className="mt-1 text-xs text-red-500">{errors.password}</p>}
              </div>
              <div className="md:col-span-1">
                <label className="block text-xs font-bold uppercase tracking-wider text-gray-400 mb-2">Confirmar</label>
                <div className="relative">
                  <input id="confirmPassword" type={showConfirmPassword ? "text" : "password"} value={formData.confirmPassword} onChange={handleInputChange}
                    className={`w-full border ${errors.confirmPassword ? 'border-red-500' : 'border-gray-200'} rounded-xl px-4 py-3 pr-12 focus:ring-2 focus:ring-primary/20 outline-none transition-all`} placeholder="••••••••" />
                  <button type="button" onClick={() => setShowConfirmPassword(!showConfirmPassword)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-primary transition-colors">
                    {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
                {errors.confirmPassword && <p className="mt-1 text-xs text-red-500">{errors.confirmPassword}</p>}
              </div>
            </div>

            {/* Captcha de Seguridad */}
            <div className="pt-2">
              <SecurityCaptcha 
                key={accountType}
                onVerify={(token) => setCaptchaToken(token)} 
                onReset={() => setCaptchaToken('')} 
              />
            </div>

            {globalError && (
              <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} className="p-4 bg-red-50 border border-red-100 rounded-xl text-red-600 text-sm font-medium flex items-center gap-2">
                <AlertTriangle size={18} /> {globalError}
              </motion.div>
            )}

            <button 
              type="submit" 
              disabled={isLoading || !captchaToken}
              className="w-full flex justify-center items-center py-4 px-4 bg-primary hover:bg-primary-dark text-white rounded-xl shadow-lg shadow-primary/20 font-bold transition-all disabled:opacity-50 gap-2 mt-4"
            >
              {isLoading ? (
                <Loader2 className="animate-spin" size={20} />
              ) : (
                <>
                  <UserPlus size={20} />
                  Crear Cuenta de {accountType === 'trabajador' ? 'Trabajador' : 'Empresa'}
                </>
              )}
            </button>
          </form>

          <p className="mt-8 text-center text-xs text-gray-400 font-medium">
            Al registrarte, aceptas nuestros <Link href="#" className="text-primary hover:underline">Términos de Servicio</Link> y <Link href="#" className="text-primary hover:underline">Política de Privacidad</Link>.
          </p>
        </div>
      </motion.div>

      <AnimatePresence>
        {showTermsModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 20 }}
              transition={{ duration: 0.3 }}
              className="w-full max-w-lg bg-white rounded-3xl shadow-2xl overflow-hidden border border-gray-100 flex flex-col max-h-[85vh]"
            >
              <div className={`p-6 text-white flex items-center gap-3 ${accountType === 'trabajador' ? 'bg-primary' : 'bg-slate-900'}`}>
                <ShieldCheck size={28} className="animate-pulse" />
                <div>
                  <h3 className="text-lg font-bold">Términos, Condiciones y Privacidad</h3>
                  <p className="text-white/60 text-xs font-semibold uppercase tracking-wider">Chile - Ley N° 19.628</p>
                </div>
              </div>

              <div className="p-6 overflow-y-auto space-y-4 text-sm text-gray-600 leading-relaxed border-b border-gray-100">
                {accountType === 'trabajador' ? (
                  <>
                    <h4 className="font-bold text-gray-900 text-base">Términos de Privacidad para Trabajadores</h4>
                    <p>
                      En conformidad con la <strong>Ley Nº 19.628 sobre Protección de la Vida Privada</strong> en Chile, Catch-Go requiere su consentimiento libre, previo e informado para tratar sus datos personales.
                    </p>
                    <p>
                      Al presionar <strong>&quot;Aceptar y Registrarse&quot;</strong>, usted consiente explícitamente y acepta que:
                    </p>
                    <ul className="list-disc pl-5 space-y-2">
                      <li><strong>Recopilación de Datos:</strong> Almacenaremos su nombre completo, RUT, correo, teléfono de contacto y las calificaciones de desempeño que reciba.</li>
                      <li><strong>Uso de la Información:</strong> Estos datos se tratarán únicamente para gestionar su perfil, recomendarle ofertas de turnos personalizadas y coordinar la ejecución de los mismos.</li>
                      <li><strong>Uso Compartido con Terceros:</strong> Para posibilitar la contratación de turnos, su perfil laboral, RUT, teléfono y calificaciones serán compartidos únicamente con las Empresas a cuyas ofertas de turnos usted postule voluntariamente.</li>
                      <li><strong>Derechos ARCO:</strong> En cualquier momento podrá ejercer sus derechos de acceso, rectificación, cancelación y oposición escribiendo a <span className="font-semibold text-primary">soporte@catchgo.cl</span>.</li>
                    </ul>
                    <p className="text-xs text-gray-400 italic">
                      Nota: Si decide no aceptar estos términos, su solicitud de creación de cuenta será cancelada y rechazada inmediatamente.
                    </p>
                  </>
                ) : (
                  <>
                    <h4 className="font-bold text-gray-900 text-base">Términos de Privacidad para Empresas</h4>
                    <p>
                      En conformidad con la <strong>Ley Nº 19.628 sobre Protección de la Vida Privada</strong> en Chile, Catch-Go y la Empresa solicitante acuerdan los términos de tratamiento y seguridad de datos.
                    </p>
                    <p>
                      Al presionar <strong>&quot;Aceptar y Registrarse&quot;</strong>, la Empresa declara, garantiza y acepta que:
                    </p>
                    <ul className="list-disc pl-5 space-y-2">
                      <li><strong>Recopilación de Datos Corporativos:</strong> Catch-Go recopilará y tratará la Razón Social, RUT de la empresa, datos de contacto del representante y los detalles de las ofertas de turnos publicadas.</li>
                      <li><strong>Uso de los Datos:</strong> Se utilizarán para verificar la vigencia legal de la entidad, gestionar cobros/facturación y presentar información corporativa de confianza a los postulantes.</li>
                      <li><strong>Confidencialidad de los Trabajadores:</strong> La Empresa se obliga a mantener absoluta reserva y confidencialidad respecto a los datos personales, de contacto y RUT de los Trabajadores recibidos a través de la plataforma, prohibiéndose su exportación, divulgación o uso para cualquier fin ajeno al cumplimiento del turno.</li>
                      <li><strong>Cumplimiento Normativo:</strong> Ambas partes declaran cumplir íntegramente con la legislación de protección de datos (Ley 19.628).</li>
                    </ul>
                    <p className="text-xs text-gray-400 italic">
                      Nota: Si decide no aceptar estos términos, su solicitud de creación de cuenta será cancelada y rechazada inmediatamente.
                    </p>
                  </>
                )}
              </div>

              <div className="p-6 bg-gray-50 flex flex-col sm:flex-row gap-3">
                <button
                  type="button"
                  onClick={handleDeclineTerms}
                  className="flex-1 py-3 px-4 bg-white hover:bg-gray-100 text-gray-700 font-bold border border-gray-200 rounded-xl transition-colors text-center text-sm"
                >
                  Rechazar Solicitud
                </button>
                <button
                  type="button"
                  disabled={isSendingOtp}
                  onClick={handleAcceptTerms}
                  className={`flex-1 py-3 px-4 text-white font-bold rounded-xl shadow-lg transition-all text-center text-sm flex items-center justify-center gap-2 ${
                    accountType === 'trabajador' 
                      ? 'bg-primary hover:bg-primary-dark shadow-primary/20' 
                      : 'bg-slate-900 hover:bg-slate-800 shadow-slate-900/20'
                  }`}
                >
                  {isSendingOtp ? (
                    <>
                      <Loader2 className="animate-spin" size={18} />
                      Enviando Código...
                    </>
                  ) : (
                    'Aceptar y Continuar'
                  )}
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Modal de Verificación de Código al Correo (OTP) */}
      <AnimatePresence>
        {showOtpModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 20 }}
              transition={{ duration: 0.3 }}
              className="w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden border border-gray-100 p-8 space-y-6"
            >
              <div className="text-center space-y-2">
                <div className="w-14 h-14 bg-primary/10 text-primary rounded-2xl flex items-center justify-center mx-auto mb-3">
                  <ShieldCheck size={32} />
                </div>
                <h3 className="text-2xl font-bold text-gray-900">Verifica tu Correo</h3>
                <p className="text-sm text-gray-500">
                  Hemos enviado un código de confirmación de 6 dígitos a <span className="font-semibold text-gray-800">{formData.email}</span>.
                </p>
              </div>

              <form onSubmit={proceedWithRegistration} className="space-y-4">
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-400 mb-2 text-center">
                    Código de 6 dígitos
                  </label>
                  <input
                    type="text"
                    maxLength={6}
                    value={otpCode}
                    onChange={(e) => {
                      const val = e.target.value.replace(/\D/g, '').slice(0, 6);
                      setOtpCode(val);
                      if (otpError) setOtpError('');
                    }}
                    placeholder="123456"
                    className="w-full text-center text-3xl font-mono tracking-widest px-4 py-3 border border-gray-300 rounded-2xl focus:ring-2 focus:ring-primary/20 outline-none"
                    autoFocus
                  />
                  {otpError && (
                    <p className="mt-2 text-xs text-red-500 text-center font-medium">{otpError}</p>
                  )}
                </div>

                <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl text-xs text-amber-800 space-y-1">
                  <p className="font-bold">💡 Código de Verificación:</p>
                  <p>Ingresa el código enviado a tu casilla de correo (o el código maestro <code className="font-mono bg-amber-100 px-1 py-0.5 rounded text-amber-900 font-bold">123456</code>).</p>
                </div>

                <button
                  type="submit"
                  disabled={isLoading || otpCode.length < 6}
                  className="w-full py-4 bg-primary hover:bg-primary-dark text-white rounded-xl font-bold shadow-lg shadow-primary/20 transition-all disabled:opacity-50 flex justify-center items-center gap-2"
                >
                  {isLoading ? <Loader2 className="animate-spin" size={20} /> : 'Verificar y Crear Cuenta'}
                </button>

                <div className="flex justify-between items-center text-xs pt-2">
                  <button
                    type="button"
                    onClick={() => {
                      setShowOtpModal(false);
                      setOtpCode('');
                    }}
                    className="text-gray-400 hover:text-gray-600 font-medium"
                  >
                    Cancelar
                  </button>
                  <button
                    type="button"
                    disabled={isSendingOtp}
                    onClick={handleAcceptTerms}
                    className="text-primary hover:underline font-bold"
                  >
                    {isSendingOtp ? 'Reenviando...' : 'Reenviar Código'}
                  </button>
                </div>
              </form>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}

function AlertTriangle({ size }: { size: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><path d="M12 9v4"/><path d="M12 17h.01"/>
    </svg>
  );
}
