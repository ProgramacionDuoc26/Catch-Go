"use client";

import React from 'react';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { mockPerfilEmpresa } from '@/lib/mockData';

export default function EmpresaPerfilPage() {
  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Perfil de la Empresa</h1>
        <p className="text-gray-500 text-sm mt-1">Actualiza los datos de facturación y contacto.</p>
      </div>

      <Card>
        <CardHeader>
          <h2 className="font-semibold text-lg">Información Comercial</h2>
        </CardHeader>
        <CardContent className="space-y-6 pt-4">
          <form className="space-y-4" onSubmit={(e) => e.preventDefault()}>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">Razón Social</label>
                <input 
                  type="text" 
                  defaultValue={mockPerfilEmpresa.razonSocial}
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
                />
              </div>
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">RUT</label>
                <input 
                  type="text" 
                  defaultValue={mockPerfilEmpresa.rut}
                  disabled
                  className="w-full px-4 py-3 border border-gray-200 bg-gray-50 rounded-md text-gray-500" 
                />
              </div>
            </div>

            <div className="space-y-2">
              <label className="block text-sm font-medium text-gray-700">Giro Comercial</label>
              <input 
                type="text" 
                defaultValue={mockPerfilEmpresa.giro}
                className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
              />
            </div>

            <div className="space-y-2">
              <label className="block text-sm font-medium text-gray-700">Dirección</label>
              <input 
                type="text" 
                defaultValue={mockPerfilEmpresa.direccion}
                className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">Nombre de Contacto</label>
                <input 
                  type="text" 
                  defaultValue={mockPerfilEmpresa.contactoNombre}
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
                />
              </div>
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">Teléfono</label>
                <input 
                  type="tel" 
                  defaultValue={mockPerfilEmpresa.telefono}
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
                />
              </div>
            </div>

            <div className="pt-4 flex justify-end">
              <Button variant="primary">Guardar Cambios</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
