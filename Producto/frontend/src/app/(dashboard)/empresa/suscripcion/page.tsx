import React from 'react';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { mockPerfilEmpresa } from '@/lib/mockData';

export default function EmpresaSuscripcionPage() {
  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Mi Suscripción</h1>
        <p className="text-gray-500 text-sm mt-1">Gestiona tu plan y método de pago.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="md:col-span-2 space-y-6">
          <Card>
            <CardHeader className="flex justify-between items-center">
              <h2 className="font-semibold text-lg">Plan Actual</h2>
              <Badge variant="success">Activo</Badge>
            </CardHeader>
            <CardContent className="pt-4 space-y-4">
              <div className="flex items-center gap-4">
                <div className="p-4 bg-primary/10 rounded-lg">
                  <svg className="w-8 h-8 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <div>
                  <h3 className="text-xl font-bold text-gray-900">Plan {mockPerfilEmpresa.planActual}</h3>
                  <p className="text-gray-500">Renovación automática el 01/06/2026</p>
                </div>
              </div>
              
              <div className="border-t border-gray-100 pt-4 mt-4">
                <h4 className="font-medium text-gray-900 mb-2">Publicaciones disponibles</h4>
                <div className="w-full bg-gray-200 rounded-full h-2.5">
                  <div className="bg-primary h-2.5 rounded-full" style={{ width: '45%' }}></div>
                </div>
                <p className="text-sm text-gray-500 mt-2">{mockPerfilEmpresa.publicacionesRestantes} de 20 publicaciones restantes este mes</p>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-4">
          <Card className="border-primary shadow-sm">
            <CardHeader className="bg-primary/5 border-b-primary/10">
              <h2 className="font-semibold text-primary">Mejorar Plan</h2>
            </CardHeader>
            <CardContent className="pt-4 space-y-4">
              <p className="text-sm text-gray-600">Actualiza al plan Enterprise para obtener publicaciones ilimitadas y soporte prioritario.</p>
              <div className="text-3xl font-bold text-gray-900">$99.000<span className="text-sm text-gray-500 font-normal">/mes</span></div>
              <ul className="text-sm text-gray-600 space-y-2">
                <li>✓ Publicaciones ilimitadas</li>
                <li>✓ Match prioritario con IA</li>
                <li>✓ Soporte 24/7</li>
              </ul>
              <Button variant="primary" fullWidth>Actualizar a Enterprise</Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
