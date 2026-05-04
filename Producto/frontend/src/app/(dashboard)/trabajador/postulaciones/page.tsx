import React from 'react';
import { Card, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';

// Mocked applied offers
const mockMisPostulaciones = [
  {
    id: 'p1',
    oferta: 'Guardia de Seguridad Turno Noche',
    empresa: 'Seguridad Integral SpA',
    fechaPostulacion: '2026-04-20',
    estado: 'ACEPTADO',
    remuneracion: 35000
  },
  {
    id: 'p2',
    oferta: 'Garzón para Evento Corporativo',
    empresa: 'Eventos Premium',
    fechaPostulacion: '2026-04-25',
    estado: 'PENDIENTE',
    remuneracion: 45000
  },
  {
    id: 'p3',
    oferta: 'Operario de Bodega Fin de Semana',
    empresa: 'Logística Rápida',
    fechaPostulacion: '2026-04-18',
    estado: 'RECHAZADO',
    remuneracion: 40000
  }
];

export default function TrabajadorPostulacionesPage() {
  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Mis Postulaciones</h1>
        <p className="text-gray-500 text-sm mt-1">Sigue el estado de tus postulaciones a turnos.</p>
      </div>

      <div className="space-y-4">
        {mockMisPostulaciones.map((postulacion) => (
          <Card key={postulacion.id}>
            <CardContent className="p-4 sm:p-6 flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
              <div className="flex-1 space-y-1">
                <div className="flex items-center gap-3">
                  <h3 className="font-bold text-lg text-gray-900">{postulacion.oferta}</h3>
                  <Badge 
                    variant={
                      postulacion.estado === 'ACEPTADO' ? 'success' : 
                      postulacion.estado === 'RECHAZADO' ? 'error' : 'warning'
                    }
                  >
                    {postulacion.estado}
                  </Badge>
                </div>
                <p className="text-sm font-medium text-gray-700">{postulacion.empresa}</p>
                <div className="text-sm text-gray-500 flex gap-4 mt-1">
                  <span>Postulado el: {postulacion.fechaPostulacion}</span>
                  <span className="font-medium text-green-700">${postulacion.remuneracion.toLocaleString('es-CL')}</span>
                </div>
              </div>
              
              <div className="flex flex-col gap-2 w-full sm:w-auto">
                {postulacion.estado === 'ACEPTADO' && (
                  <Button variant="primary" size="sm">Ver Detalles Turno</Button>
                )}
                {postulacion.estado === 'PENDIENTE' && (
                  <Button variant="outline" size="sm" className="text-red-600 border-red-200 hover:bg-red-50">Retractar</Button>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
