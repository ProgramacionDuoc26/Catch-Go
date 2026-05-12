"use client";

import React, { useState, useEffect } from 'react';
import Link from "next/link";
import { motion, AnimatePresence } from "framer-motion";
import { ArrowRight, CheckCircle2, Users, Building2, MapPin, Mail, Phone, ChevronLeft, ChevronRight, MessageSquare } from "lucide-react";

const CAROUSEL_IMAGES = [
  {
    url: "https://images.unsplash.com/photo-1521737711867-e3b97375f902?auto=format&fit=crop&q=80&w=1920",
    title: "Conectamos Oportunidades",
    subtitle: "La plataforma líder para encontrar turnos flexibles y talento verificado."
  },
  {
    url: "https://images.unsplash.com/photo-1556761175-b413da4baf72?auto=format&fit=crop&q=80&w=1920",
    title: "Impulsa tu Empresa",
    subtitle: "Gestiona tu personal temporal con eficiencia y seguridad total."
  },
  {
    url: "https://images.unsplash.com/photo-1542744173-8e7e53415bb0?auto=format&fit=crop&q=80&w=1920",
    title: "Crece Profesionalmente",
    subtitle: "Accede a los mejores turnos part-time y consolida tu carrera."
  }
];

export default function LandingPage() {
  const [currentSlide, setCurrentSlide] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % CAROUSEL_IMAGES.length);
    }, 5000);
    return () => clearInterval(timer);
  }, []);

  return (
    <div className="flex flex-col min-h-screen">
      
      {/* ═══ HERO CAROUSEL ═══ */}
      <section className="relative h-[600px] w-full overflow-hidden">
        <AnimatePresence mode="wait">
          <motion.div
            key={currentSlide}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 1 }}
            className="absolute inset-0"
          >
            <div 
              className="absolute inset-0 bg-cover bg-center transition-transform duration-[10s] scale-110"
              style={{ backgroundImage: `url(${CAROUSEL_IMAGES[currentSlide].url})` }}
            />
            <div className="absolute inset-0 bg-gradient-to-r from-primary-dark/80 to-transparent" />
          </motion.div>
        </AnimatePresence>

        <div className="relative z-10 container mx-auto h-full flex flex-col justify-center px-6">
          <motion.div
            initial={{ x: -50, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            key={`content-${currentSlide}`}
            className="max-w-2xl text-white"
          >
            <h1 className="text-5xl md:text-7xl font-bold mb-6 leading-tight">
              {CAROUSEL_IMAGES[currentSlide].title}
            </h1>
            <p className="text-xl md:text-2xl mb-8 opacity-90 font-light">
              {CAROUSEL_IMAGES[currentSlide].subtitle}
            </p>
            <div className="flex flex-col sm:flex-row gap-4">
              <Link 
                href="/register?type=trabajador" 
                className="bg-primary hover:bg-primary-light text-white px-8 py-4 rounded-xl font-bold flex items-center justify-center gap-2 transition-all shadow-lg hover:shadow-primary/30"
              >
                Empezar a Trabajar <ArrowRight size={20} />
              </Link>
              <Link 
                href="/register?type=empresa" 
                className="bg-white/10 hover:bg-white/20 backdrop-blur-md text-white border border-white/30 px-8 py-4 rounded-xl font-bold flex items-center justify-center gap-2 transition-all"
              >
                Publicar Turnos <Building2 size={20} />
              </Link>
            </div>
          </motion.div>
        </div>

        {/* Indicators */}
        <div className="absolute bottom-8 left-1/2 -translate-x-1/2 z-20 flex gap-2">
          {CAROUSEL_IMAGES.map((_, idx) => (
            <button
              key={idx}
              onClick={() => setCurrentSlide(idx)}
              className={`h-1 transition-all duration-500 rounded-full ${currentSlide === idx ? 'w-8 bg-white' : 'w-2 bg-white/30'}`}
            />
          ))}
        </div>
      </section>

      {/* ═══ QUIENES SOMOS (Ancho Completo) ═══ */}
      <section id="quienes-somos" className="py-24 bg-white">
        <div className="container mx-auto px-6">
          <div className="flex flex-col lg:flex-row items-center gap-16">
            <div className="lg:w-1/2">
              <span className="text-accent-red font-bold tracking-widest uppercase text-sm mb-4 block">Nuestra Misión</span>
              <h2 className="text-4xl md:text-5xl font-bold text-primary-dark mb-8 leading-tight">
                Reinventando la conexión laboral en tiempo real.
              </h2>
              <div className="space-y-6 text-gray-600 text-lg leading-relaxed">
                <p>
                  Catch-Go nace con una misión clara: **conectar oportunidades de forma inmediata**. Entendemos que en el mercado actual, la agilidad es la clave del éxito. Por eso, hemos creado un ecosistema donde la confianza y la velocidad se encuentran.
                </p>
                <p>
                  Nuestra plataforma utiliza tecnología de geolocalización para asegurar que el talento esté donde se necesita, cuando se necesita. Estamos comprometidos con crear un espacio seguro y eficiente para que empresas y trabajadores crezcan juntos.
                </p>
              </div>
            </div>
            <div className="lg:w-1/2 relative">
              <div className="absolute -inset-4 bg-primary/5 rounded-[40px] rotate-2" />
              <img 
                src="https://images.unsplash.com/photo-1552664730-d307ca884978?auto=format&fit=crop&q=80&w=800" 
                alt="Equipo Catch-Go" 
                className="relative rounded-[32px] shadow-2xl z-10"
              />
            </div>
          </div>
        </div>
      </section>

      {/* ═══ CONTACTO (Amplio y Elegante) ═══ */}
      <section id="contacto" className="py-24 bg-gray-50 relative overflow-hidden">
        <div className="absolute top-0 right-0 w-1/3 h-full bg-primary/5 -skew-x-12 translate-x-1/2" />
        
        <div className="container mx-auto px-6 relative z-10">
          <div className="max-w-5xl mx-auto">
            <div className="text-center mb-16">
              <h2 className="text-4xl font-bold text-primary-dark mb-4">Hablemos de tu Futuro</h2>
              <p className="text-gray-500 text-lg">¿Tienes dudas o quieres integrar Catch-Go en tu empresa? Estamos para ayudarte.</p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-5 gap-12">
              <div className="lg:col-span-2 space-y-8">
                <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100 flex items-start gap-4">
                  <div className="w-12 h-12 bg-primary text-white rounded-2xl flex items-center justify-center shrink-0 shadow-lg shadow-primary/20">
                    <Mail size={24} />
                  </div>
                  <div>
                    <p className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">Email</p>
                    <p className="text-lg font-bold text-primary-dark">contacto@catchandgo.cl</p>
                  </div>
                </div>
                
                <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100 flex items-start gap-4">
                  <div className="w-12 h-12 bg-accent-red text-white rounded-2xl flex items-center justify-center shrink-0 shadow-lg shadow-red-200">
                    <Phone size={24} />
                  </div>
                  <div>
                    <p className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">WhatsApp</p>
                    <p className="text-lg font-bold text-primary-dark">+56 9 1234 5678</p>
                  </div>
                </div>

                <div className="bg-primary-dark p-8 rounded-3xl text-white shadow-xl">
                  <p className="font-bold mb-4 flex items-center gap-2 italic">
                    <MessageSquare size={20} /> Soporte Activo
                  </p>
                  <p className="text-sm opacity-70">
                    Nuestro equipo está disponible para ayudarte con cualquier consulta sobre el funcionamiento de la plataforma.
                  </p>
                </div>
              </div>

              <div className="lg:col-span-3">
                <form className="bg-white p-10 rounded-[40px] shadow-xl border border-gray-100 space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="space-y-2">
                      <label className="text-sm font-bold text-gray-700 ml-1">Nombre Completo</label>
                      <input type="text" className="w-full bg-gray-50 border-none rounded-2xl px-5 py-4 focus:ring-2 focus:ring-primary/20 outline-none transition-all" placeholder="Juan Pérez" />
                    </div>
                    <div className="space-y-2">
                      <label className="text-sm font-bold text-gray-700 ml-1">Empresa (Opcional)</label>
                      <input type="text" className="w-full bg-gray-50 border-none rounded-2xl px-5 py-4 focus:ring-2 focus:ring-primary/20 outline-none transition-all" placeholder="Nombre de tu empresa" />
                    </div>
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-bold text-gray-700 ml-1">Mensaje o Consulta</label>
                    <textarea rows={4} className="w-full bg-gray-50 border-none rounded-2xl px-5 py-4 focus:ring-2 focus:ring-primary/20 outline-none transition-all resize-none" placeholder="¿En qué podemos ayudarte?" />
                  </div>
                  <button type="submit" className="w-full bg-primary hover:bg-primary-dark text-white font-bold py-5 rounded-2xl shadow-lg shadow-primary/20 transition-all flex items-center justify-center gap-2 text-lg">
                    Enviar Mensaje <ArrowRight size={22} />
                  </button>
                </form>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
