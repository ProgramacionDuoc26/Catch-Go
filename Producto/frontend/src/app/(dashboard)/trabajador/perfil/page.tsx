"use client";

import React from 'react';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { mockPerfilTrabajador } from '@/lib/mockData';

export default function TrabajadorPerfilPage() {
  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Mi Perfil Profesional</h1>
        <p className="text-gray-500 text-sm mt-1">Completa tu perfil para aumentar tus probabilidades de match.</p>
      </div>

      <Card>
        <CardHeader>
          <h2 className="font-semibold text-lg">Información Personal</h2>
        </CardHeader>
        <CardContent className="space-y-6 pt-4">
          <form className="space-y-4" onSubmit={(e) => e.preventDefault()}>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">Nombre Completo</label>
                <input 
                  type="text" 
                  defaultValue={mockPerfilTrabajador.nombre}
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
                />
              </div>
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">RUT</label>
                <input 
                  type="text" 
                  defaultValue={mockPerfilTrabajador.rut}
                  disabled
                  className="w-full px-4 py-3 border border-gray-200 bg-gray-50 rounded-md text-gray-500" 
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">Teléfono</label>
                <input 
                  type="tel" 
                  defaultValue={mockPerfilTrabajador.telefono}
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
                />
              </div>
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">Región</label>
                <select className="w-full px-4 py-3 border border-gray-300 rounded-md bg-white focus:ring-primary focus:border-primary">
                  <option selected={mockPerfilTrabajador.region === 'Metropolitana'}>Metropolitana</option>
                  <option>Valparaíso</option>
                  <option>O'Higgins</option>
                </select>
              </div>
            </div>

            <div className="space-y-2">
              <label className="block text-sm font-medium text-gray-700">Experiencia Breve</label>
              <textarea 
                rows={3}
                defaultValue={mockPerfilTrabajador.experiencia}
                className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary resize-none" 
              ></textarea>
            </div>

            <div className="space-y-2">
              <label className="block text-sm font-medium text-gray-700">Certificaciones (Separadas por coma)</label>
              <input 
                type="text" 
                defaultValue={mockPerfilTrabajador.certificaciones.join(', ')}
                className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
              />
            </div>

            <div className="pt-4 flex justify-end">
              <Button variant="primary">Guardar Perfil</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
