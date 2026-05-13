"use client";

import React, { useState, useEffect } from 'react';
import Link from "next/link";
import { motion, AnimatePresence } from "framer-motion";
import { ArrowRight, CheckCircle2, Users, Building2, MapPin, Mail, Phone, ChevronLeft, ChevronRight, MessageSquare, Star, Shield, Zap, TrendingUp } from "lucide-react";

const CAROUSEL_IMAGES = [
  {
    url: "https://images.unsplash.com/photo-1521737711867-e3b97375f902?auto=format&fit=crop&q=80&w=1920",
    title: "Conectamos Oportunidades",
    subtitle: "La plataforma líder para encontrar turnos flexibles y talento verificado en tiempo real."
  },
  {
    url: "https://images.unsplash.com/photo-1556761175-b413da4baf72?auto=format&fit=crop&q=80&w=1920",
    title: "Impulsa tu Empresa",
    subtitle: "Gestiona tu personal temporal con eficiencia, seguridad y trazabilidad total."
  },
  {
    url: "https://images.unsplash.com/photo-1542744173-8e7e53415bb0?auto=format&fit=crop&q=80&w=1920",
    title: "Crece Profesionalmente",
    subtitle: "Accede a los mejores turnos part-time y consolida tu carrera con flexibilidad."
  }
];

export default function LandingPage() {
  const [currentSlide, setCurrentSlide] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % CAROUSEL_IMAGES.length);
    }, 6000);
    return () => clearInterval(timer);
  }, []);

  return (
    <div className="flex flex-col min-h-screen bg-slate-50">
      
      {/* ═══ HERO CAROUSEL ═══ */}
      <section className="relative h-[700px] w-full overflow-hidden">
        <AnimatePresence mode="wait">
          <motion.div
            key={currentSlide}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 1.2 }}
            className="absolute inset-0"
          >
            <div 
              className="absolute inset-0 bg-cover bg-center transition-transform duration-[12s] scale-110"
              style={{ backgroundImage: `url(${CAROUSEL_IMAGES[currentSlide].url})` }}
            />
            <div className="absolute inset-0 bg-gradient-to-r from-slate-950 via-slate-900/80 to-transparent" />
          </motion.div>
        </AnimatePresence>

        <div className="relative z-10 max-w-7xl mx-auto h-full flex flex-col justify-center px-6 lg:px-8">
          <motion.div
            initial={{ y: 30, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            key={`content-${currentSlide}`}
            className="max-w-3xl text-white"
          >
            <motion.div 
              initial={{ scaleX: 0 }}
              animate={{ scaleX: 1 }}
              className="h-1 w-24 bg-primary mb-8 origin-left"
            />
            <h1 className="text-6xl md:text-8xl font-black mb-8 leading-[0.9] tracking-tight">
              {CAROUSEL_IMAGES[currentSlide].title.split(' ').map((word, i) => (
                <span key={i} className={i === 1 ? 'text-primary' : ''}>{word} </span>
              ))}
            </h1>
            <p className="text-xl md:text-2xl mb-12 text-slate-300 font-medium max-w-xl leading-relaxed">
              {CAROUSEL_IMAGES[currentSlide].subtitle}
            </p>
            <div className="flex flex-col sm:flex-row gap-5">
              <Link 
                href="/register?type=trabajador" 
                className="bg-primary hover:bg-primary-light text-white px-10 py-5 rounded-2xl font-black uppercase tracking-widest text-sm flex items-center justify-center gap-3 transition-all shadow-2xl shadow-primary/40 hover:-translate-y-1"
              >
                Postular ahora <ArrowRight size={20} />
              </Link>
              <Link 
                href="/register?type=empresa" 
                className="bg-white/10 hover:bg-white/20 backdrop-blur-xl text-white border border-white/20 px-10 py-5 rounded-2xl font-black uppercase tracking-widest text-sm flex items-center justify-center gap-3 transition-all hover:border-white/40"
              >
                Soy Empresa <Building2 size={20} />
              </Link>
            </div>
          </motion.div>
        </div>

        {/* Indicators */}
        <div className="absolute bottom-12 right-12 z-20 flex flex-col gap-4">
          {CAROUSEL_IMAGES.map((_, idx) => (
            <button
              key={idx}
              onClick={() => setCurrentSlide(idx)}
              className={`w-1 transition-all duration-700 rounded-full ${currentSlide === idx ? 'h-12 bg-primary' : 'h-3 bg-white/20'}`}
            />
          ))}
        </div>
      </section>

      {/* ═══ QUIENES SOMOS ═══ */}
      <section id="quienes-somos" className="py-32 overflow-hidden">
        <div className="max-w-7xl mx-auto px-6 lg:px-8">
          <div className="flex flex-col lg:flex-row items-center gap-24">
            <div className="lg:w-1/2 relative">
              <motion.div 
                initial={{ opacity: 0, scale: 0.8 }}
                whileInView={{ opacity: 1, scale: 1 }}
                viewport={{ once: true }}
                className="relative"
              >
                <div className="absolute -top-12 -left-12 w-64 h-64 bg-primary/10 rounded-full blur-3xl animate-pulse" />
                <div className="absolute -bottom-12 -right-12 w-64 h-64 bg-accent-red/10 rounded-full blur-3xl animate-pulse" />
                <img 
                  src="https://images.unsplash.com/photo-1552664730-d307ca884978?auto=format&fit=crop&q=80&w=800" 
                  alt="Equipo Catch-Go" 
                  className="relative rounded-[3rem] shadow-[0_32px_64px_-12px_rgba(0,0,0,0.15)] z-10 object-cover aspect-[4/5] lg:aspect-square"
                />
                <div className="absolute -bottom-8 -right-8 bg-white p-8 rounded-[2rem] shadow-2xl z-20 max-w-xs hidden md:block">
                  <div className="flex items-center gap-4 mb-4">
                    <div className="p-3 bg-emerald-50 rounded-xl text-emerald-600">
                      <Shield size={24} />
                    </div>
                    <p className="font-black text-slate-900 leading-tight">Verificación Garantizada</p>
                  </div>
                  <p className="text-sm text-slate-500 font-medium leading-relaxed">
                    Auditamos cada perfil para asegurar la mayor calidad en cada turno.
                  </p>
                </div>
              </motion.div>
            </div>
            
            <div className="lg:w-1/2">
              <div className="space-y-8">
                <div className="inline-flex items-center gap-2 px-4 py-2 bg-slate-100 rounded-full text-slate-500 text-xs font-black uppercase tracking-widest">
                  <TrendingUp size={14} className="text-primary" />
                  Nuestra Trayectoria
                </div>
                <h2 className="text-5xl md:text-7xl font-black text-slate-950 leading-[0.95] tracking-tighter">
                  Reinventando la conexión laboral <span className="text-primary italic">humana</span>.
                </h2>
                <div className="space-y-6 text-slate-600 text-lg font-medium leading-relaxed">
                  <p>
                    Catch-Go nace con una visión clara: <strong className="text-slate-900">democratizar el acceso al trabajo</strong> mediante la tecnología. Entendemos que en el mercado actual, la agilidad no es una opción, es una necesidad.
                  </p>
                  <p>
                    Hemos creado un ecosistema donde la <span className="text-primary">geolocalización</span> inteligente y la confianza mutua permiten que empresas y talentos se encuentren en minutos, no en días.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ═══ CONTACTO ═══ */}
      <section id="contacto" className="py-32 bg-slate-950 relative overflow-hidden">
        <div className="absolute top-0 right-0 w-1/2 h-full bg-primary/10 -skew-x-12 translate-x-1/2 blur-3xl opacity-50" />
        <div className="absolute bottom-0 left-0 w-1/2 h-full bg-blue-600/5 skew-x-12 -translate-x-1/2 blur-3xl opacity-50" />
        
        <div className="max-w-7xl mx-auto px-6 lg:px-8 relative z-10">
          <div className="flex flex-col lg:flex-row gap-24">
            <div className="lg:w-1/3">
              <div className="sticky top-32 space-y-12">
                <div className="space-y-4">
                  <h2 className="text-5xl font-black text-white leading-tight">Hablemos de tu <span className="text-primary">impacto</span>.</h2>
                  <p className="text-slate-400 text-lg font-medium">¿Tienes un desafío para nosotros? Nuestro equipo experto está listo para asesorarte.</p>
                </div>

                <div className="space-y-6">
                  <a href="mailto:contacto@catchandgo.cl" className="group flex items-center gap-6 p-6 bg-white/5 hover:bg-white/10 rounded-3xl border border-white/10 transition-all">
                    <div className="w-14 h-14 bg-primary text-white rounded-2xl flex items-center justify-center transition-transform group-hover:scale-110">
                      <Mail size={24} />
                    </div>
                    <div>
                      <p className="text-[10px] font-black text-slate-500 uppercase tracking-widest mb-1">Escríbenos</p>
                      <p className="text-lg font-bold text-white">hola@catchandgo.cl</p>
                    </div>
                  </a>

                  <a href="https://wa.me/56912345678" className="group flex items-center gap-6 p-6 bg-white/5 hover:bg-white/10 rounded-3xl border border-white/10 transition-all">
                    <div className="w-14 h-14 bg-emerald-500 text-white rounded-2xl flex items-center justify-center transition-transform group-hover:scale-110">
                      <Phone size={24} />
                    </div>
                    <div>
                      <p className="text-[10px] font-black text-slate-500 uppercase tracking-widest mb-1">WhatsApp</p>
                      <p className="text-lg font-bold text-white">+56 9 1234 5678</p>
                    </div>
                  </a>
                </div>
              </div>
            </div>

            <div className="lg:w-2/3">
              <motion.div 
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                className="bg-white p-12 rounded-[3.5rem] shadow-2xl relative"
              >
                <form className="space-y-8">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div className="space-y-3">
                      <label className="text-xs font-black uppercase tracking-widest text-slate-400 ml-1">Identificación</label>
                      <input type="text" className="w-full bg-slate-50 border-none rounded-2xl px-6 py-5 focus:ring-4 focus:ring-primary/10 outline-none transition-all font-medium text-slate-900" placeholder="Nombre completo" />
                    </div>
                    <div className="space-y-3">
                      <label className="text-xs font-black uppercase tracking-widest text-slate-400 ml-1">Organización</label>
                      <input type="text" className="w-full bg-slate-50 border-none rounded-2xl px-6 py-5 focus:ring-4 focus:ring-primary/10 outline-none transition-all font-medium text-slate-900" placeholder="Nombre de tu empresa" />
                    </div>
                  </div>
                  <div className="space-y-3">
                    <label className="text-xs font-black uppercase tracking-widest text-slate-400 ml-1">Tu Mensaje</label>
                    <textarea rows={5} className="w-full bg-slate-50 border-none rounded-2xl px-6 py-5 focus:ring-4 focus:ring-primary/10 outline-none transition-all resize-none font-medium text-slate-900" placeholder="¿En qué podemos colaborar?" />
                  </div>
                  <button type="submit" className="w-full bg-primary hover:bg-primary-dark text-white font-black uppercase tracking-[0.2em] py-6 rounded-2xl shadow-2xl shadow-primary/30 transition-all flex items-center justify-center gap-3 text-sm">
                    Enviar mensaje <ArrowRight size={22} />
                  </button>
                </form>
              </motion.div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
