"use client";

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { motion } from 'framer-motion';
import { User, Briefcase, ChevronRight, ArrowRight, Sparkles } from 'lucide-react';
import { createClient } from '@/lib/supabase/client';

export default function RoleSelectionPage() {
  const router = useRouter();
  const [selected, setSelected] = useState<'TRABAJADOR' | 'EMPRESA' | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [userData, setUserData] = useState<any>(null);

  const supabase = createClient();

  React.useEffect(() => {
    const getUserData = async () => {
      const { data: { user } } = await supabase.auth.getUser();
      if (user) {
        setUserData({
          name: user.user_metadata?.full_name || user.user_metadata?.name || '',
          email: user.email || '',
          photo: user.user_metadata?.avatar_url || '',
          id: user.id
        });
      }
    };
    getUserData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSelection = (role: 'TRABAJADOR' | 'EMPRESA') => {
    setSelected(role);
  };

  const handleContinue = async () => {
    if (!selected) return;
    
    setIsLoading(true);
    try {
      // Aquí simularemos el guardado del rol. 
      // En una implementación real, haríamos un fetch al backend o a Supabase
      // para crear el perfil del usuario.
      
      console.log(`Configurando perfil como: ${selected}`);
      
      // Limpiar datos antiguos para evitar conflictos
      localStorage.removeItem('user_info');
      
      // Guardar el nuevo perfil en localStorage para que el resto de la app lo reconozca
      const mockUser = {
        id: userData?.id || 'google-user-' + Math.random().toString(36).substr(2, 9),
        tipo: selected,
        nombre: userData?.name || 'Usuario de Google',
        email: userData?.email || '',
        foto: userData?.photo || ''
      };
      
      localStorage.setItem('user_info', JSON.stringify(mockUser));
      localStorage.setItem('user_role', selected);
      
      // Redirigir con un pequeño delay para asegurar que el localStorage se guarde
      setTimeout(() => {
        if (selected === 'EMPRESA') {
          console.log("Redirigiendo a Perfil de Empresa...");
          router.push('/empresa/perfil');
        } else {
          console.log("Redirigiendo a Perfil de Trabajador...");
          router.push('/trabajador/perfil');
        }
      }, 100);
    } catch (error) {
      console.error('Error al guardar el rol:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-6">
      <div className="max-w-4xl w-full">
        <div className="text-center mb-12">
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 text-primary text-sm font-bold mb-4"
          >
            <Sparkles size={16} />
            ¡Te damos la bienvenida{userData?.name ? `, ${userData.name}` : ''} a Catch-Go!
          </motion.div>
          <motion.h1 
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="text-4xl lg:text-5xl font-black text-slate-900 mb-4 tracking-tight"
          >
            ¿Cómo quieres usar la plataforma?
          </motion.h1>
          <motion.p 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.2 }}
            className="text-lg text-slate-500 font-medium max-w-xl mx-auto"
          >
            Ayúdanos a personalizar tu experiencia configurando tu perfil inicial.
          </motion.p>
        </div>

        <div className="grid md:grid-cols-2 gap-8 mb-12">
          {/* Opción Trabajador */}
          <motion.div
            initial={{ opacity: 0, x: -30 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.3 }}
            onClick={() => handleSelection('TRABAJADOR')}
            className={`group relative cursor-pointer p-8 rounded-3xl border-2 transition-all duration-500 overflow-hidden
              ${selected === 'TRABAJADOR' 
                ? 'border-primary bg-primary/5 shadow-2xl shadow-primary/10 scale-[1.02]' 
                : 'border-white bg-white hover:border-slate-200 hover:shadow-xl hover:shadow-slate-200/50'}`}
          >
            <div className={`p-4 rounded-2xl w-fit mb-6 transition-colors duration-500 
              ${selected === 'TRABAJADOR' ? 'bg-primary text-white' : 'bg-slate-100 text-slate-500 group-hover:bg-primary/10 group-hover:text-primary'}`}>
              <User size={32} />
            </div>
            
            <h3 className="text-2xl font-bold text-slate-900 mb-3">Busco Trabajo</h3>
            <p className="text-slate-500 font-medium leading-relaxed">
              Encuentra turnos verificados, gestiona tus pagos y construye tu reputación profesional.
            </p>
            
            <div className={`mt-8 flex items-center gap-2 font-bold transition-all
              ${selected === 'TRABAJADOR' ? 'text-primary' : 'text-slate-400 group-hover:text-primary group-hover:translate-x-2'}`}>
              Elegir esta opción <ChevronRight size={20} />
            </div>

            {selected === 'TRABAJADOR' && (
              <motion.div layoutId="check" className="absolute top-6 right-6 text-primary">
                <div className="bg-primary text-white p-1 rounded-full">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
              </motion.div>
            )}
          </motion.div>

          {/* Opción Empresa */}
          <motion.div
            initial={{ opacity: 0, x: 30 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.4 }}
            onClick={() => handleSelection('EMPRESA')}
            className={`group relative cursor-pointer p-8 rounded-3xl border-2 transition-all duration-500 overflow-hidden
              ${selected === 'EMPRESA' 
                ? 'border-primary bg-primary/5 shadow-2xl shadow-primary/10 scale-[1.02]' 
                : 'border-white bg-white hover:border-slate-200 hover:shadow-xl hover:shadow-slate-200/50'}`}
          >
            <div className={`p-4 rounded-2xl w-fit mb-6 transition-colors duration-500 
              ${selected === 'EMPRESA' ? 'bg-primary text-white' : 'bg-slate-100 text-slate-500 group-hover:bg-primary/10 group-hover:text-primary'}`}>
              <Briefcase size={32} />
            </div>
            
            <h3 className="text-2xl font-bold text-slate-900 mb-3">Busco Personal</h3>
            <p className="text-slate-500 font-medium leading-relaxed">
              Publica ofertas, gestiona postulantes y encuentra al mejor talento para tus necesidades.
            </p>
            
            <div className={`mt-8 flex items-center gap-2 font-bold transition-all
              ${selected === 'EMPRESA' ? 'text-primary' : 'text-slate-400 group-hover:text-primary group-hover:translate-x-2'}`}>
              Elegir esta opción <ChevronRight size={20} />
            </div>

            {selected === 'EMPRESA' && (
              <motion.div layoutId="check" className="absolute top-6 right-6 text-primary">
                <div className="bg-primary text-white p-1 rounded-full">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
              </motion.div>
            )}
          </motion.div>
        </div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
          className="flex justify-center"
        >
          <button
            onClick={handleContinue}
            disabled={!selected || isLoading}
            className="group relative flex items-center justify-center gap-3 px-12 py-5 rounded-2xl bg-slate-900 text-white font-bold text-lg shadow-xl shadow-slate-200 transition-all hover:bg-black hover:-translate-y-1 active:translate-y-0 disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:translate-y-0"
          >
            {isLoading ? (
              <div className="w-6 h-6 border-2 border-white/30 border-t-white rounded-full animate-spin" />
            ) : (
              <>
                Confirmar y continuar
                <ArrowRight size={22} className="group-hover:translate-x-1 transition-transform" />
              </>
            )}
          </button>
        </motion.div>
      </div>
    </div>
  );
}
