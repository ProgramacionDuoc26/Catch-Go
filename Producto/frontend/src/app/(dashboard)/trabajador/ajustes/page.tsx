import React from 'react';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';

export default function TrabajadorAjustesPage() {
  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Ajustes de Cuenta</h1>
        <p className="text-gray-500 text-sm mt-1">Configura tus preferencias y notificaciones.</p>
      </div>

      <Card>
        <CardHeader>
          <h2 className="font-semibold text-lg">Notificaciones</h2>
        </CardHeader>
        <CardContent className="space-y-4 pt-4">
          <div className="flex items-center justify-between py-2">
            <div>
              <p className="font-medium text-gray-900">Nuevas ofertas (Match &gt; 80%)</p>
              <p className="text-sm text-gray-500">Recibe un correo cuando haya un turno muy compatible.</p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" className="sr-only peer" defaultChecked />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary/30 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
            </label>
          </div>
          
          <div className="flex items-center justify-between py-2 border-t border-gray-100">
            <div>
              <p className="font-medium text-gray-900">Estado de postulaciones</p>
              <p className="text-sm text-gray-500">Notificarme cuando una empresa me acepte o rechace.</p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" className="sr-only peer" defaultChecked />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary/30 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
            </label>
          </div>
        </CardContent>
      </Card>

      <Card className="border-red-200">
        <CardHeader className="bg-red-50/50">
          <h2 className="font-semibold text-red-700">Zona de Peligro</h2>
        </CardHeader>
        <CardContent className="space-y-4 pt-4">
          <p className="text-sm text-gray-600">Una vez que elimines tu cuenta, no hay vuelta atrás. Por favor asegúrate.</p>
          <Button variant="danger">Eliminar mi cuenta</Button>
        </CardContent>
      </Card>
    </div>
  );
}
