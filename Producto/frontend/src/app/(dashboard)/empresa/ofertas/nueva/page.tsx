"use client";

import React from 'react';
import Link from 'next/link';
import { Card, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';

export default function NuevaOfertaPage() {
  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Publicar Nuevo Turno</h1>
        <p className="text-gray-500 text-sm mt-1">Completa los detalles para encontrar al worker ideal.</p>
      </div>

      <Card>
        <CardContent className="space-y-6 pt-6">
          <form className="space-y-4" onSubmit={(e) => e.preventDefault()}>
            <div className="space-y-2">
              <label htmlFor="titulo" className="block text-sm font-medium text-gray-700">Título del Turno/Oferta</label>
              <input 
                type="text" 
                id="titulo" 
                className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
                placeholder="Ej. Guardia de Seguridad Turno Noche"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label htmlFor="categoria" className="block text-sm font-medium text-gray-700">Categoría</label>
                <select id="categoria" className="w-full px-4 py-3 border border-gray-300 rounded-md bg-white focus:ring-primary focus:border-primary">
                  <option>Guardia</option>
                  <option>Garzón</option>
                  <option>Carga/Descarga</option>
                  <option>Aseo</option>
                </select>
              </div>
              <div className="space-y-2">
                <label htmlFor="remuneracion" className="block text-sm font-medium text-gray-700">Remuneración Líquida ($)</label>
                <input 
                  type="number" 
                  id="remuneracion" 
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
                  placeholder="35000"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label htmlFor="fechaInicio" className="block text-sm font-medium text-gray-700">Fecha de Inicio</label>
                <input 
                  type="date" 
                  id="fechaInicio" 
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
                />
              </div>
              <div className="space-y-2">
                <label htmlFor="fechaFin" className="block text-sm font-medium text-gray-700">Fecha de Fin</label>
                <input 
                  type="date" 
                  id="fechaFin" 
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" 
                />
              </div>
            </div>

            <div className="space-y-2">
              <label htmlFor="descripcion" className="block text-sm font-medium text-gray-700">Descripción y Requisitos</label>
              <textarea 
                id="descripcion" 
                rows={4}
                className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary resize-none" 
                placeholder="Describe el trabajo y lista los requisitos mínimos (ej. OS10 vigente, zapatos de seguridad)..."
              ></textarea>
            </div>

            <div className="pt-4 flex flex-col sm:flex-row gap-3">
              <Button variant="primary" fullWidth>Publicar Oferta</Button>
              <Link href="/empresa/ofertas" className="w-full sm:w-auto">
                <Button variant="ghost" fullWidth>Cancelar</Button>
              </Link>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
