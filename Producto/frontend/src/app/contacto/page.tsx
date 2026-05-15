"use client";

import React, { useState } from 'react';

export default function ContactoPage() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    message: ''
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitted, setSubmitted] = useState(false);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData({ ...formData, [e.target.id]: e.target.value });
    if (errors[e.target.id]) {
      setErrors({ ...errors, [e.target.id]: '' });
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const newErrors: Record<string, string> = {};

    if (!formData.name) newErrors.name = 'El nombre es obligatorio';
    if (!formData.email) newErrors.email = 'El correo es obligatorio';
    if (!formData.message) newErrors.message = 'El mensaje es obligatorio';

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    // Aquí iría la lógica de envío real a un backend
    setSubmitted(true);
    setFormData({ name: '', email: '', message: '' });
    setTimeout(() => setSubmitted(false), 5000);
  };

  return (
    <div className="max-w-5xl mx-auto px-4 py-16">
      <h1 className="text-4xl font-bold text-text-main mb-8 text-center">Contacto</h1>
      <div className="bg-surface rounded-2xl shadow-xl border border-gray-100 p-8 md:p-12">
        {submitted && (
          <div className="mb-8 p-4 bg-green-50 text-green-700 border border-green-200 rounded-xl flex items-center gap-3">
            <span className="text-2xl">✓</span>
            <p className="font-medium">¡Mensaje enviado con éxito! Nos pondremos en contacto pronto.</p>
          </div>
        )}
        <form className="space-y-8" onSubmit={handleSubmit}>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div>
              <label htmlFor="name" className="block text-sm font-semibold text-text-main mb-3 uppercase tracking-wider">Nombre Completo</label>
              <input 
                type="text" 
                id="name" 
                value={formData.name}
                onChange={handleInputChange}
                className={`w-full bg-gray-50 border ${errors.name ? 'border-red-500' : 'border-gray-200'} rounded-xl px-6 py-4 focus:outline-none focus:ring-2 focus:ring-primary focus:bg-white transition-all`}
                placeholder="Ej. Juan Pérez"
              />
              {errors.name && <p className="mt-2 text-sm text-red-500 font-medium">{errors.name}</p>}
            </div>
            <div>
              <label htmlFor="email" className="block text-sm font-semibold text-text-main mb-3 uppercase tracking-wider">Correo Electrónico</label>
              <input 
                type="email" 
                id="email" 
                value={formData.email}
                onChange={handleInputChange}
                className={`w-full bg-gray-50 border ${errors.email ? 'border-red-500' : 'border-gray-200'} rounded-xl px-6 py-4 focus:outline-none focus:ring-2 focus:ring-primary focus:bg-white transition-all`}
                placeholder="tucorreo@ejemplo.com"
              />
              {errors.email && <p className="mt-2 text-sm text-red-500 font-medium">{errors.email}</p>}
            </div>
          </div>
          <div>
            <label htmlFor="message" className="block text-sm font-semibold text-text-main mb-3 uppercase tracking-wider">Mensaje</label>
            <textarea 
              id="message" 
              rows={6}
              value={formData.message}
              onChange={handleInputChange}
              className={`w-full bg-gray-50 border ${errors.message ? 'border-red-500' : 'border-gray-200'} rounded-xl px-6 py-4 focus:outline-none focus:ring-2 focus:ring-primary focus:bg-white transition-all resize-none`}
              placeholder="¿En qué podemos ayudarte?"
            />
            {errors.message && <p className="mt-2 text-sm text-red-500 font-medium">{errors.message}</p>}
          </div>
          <div className="pt-4">
            <button 
              type="submit" 
              className="w-full bg-primary text-white font-bold rounded-xl py-5 hover:bg-primary-dark shadow-lg shadow-primary/20 transform active:scale-[0.98] transition-all text-lg"
            >
              Enviar Mensaje
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
