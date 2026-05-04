import React from 'react';
import { Card, CardContent, CardHeader, CardFooter } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { mockOfertas } from '@/lib/mockData';

export default function TrabajadorOfertasPage() {
  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Explorar Ofertas</h1>
          <p className="text-gray-500 text-sm mt-1">Turnos compatibles con tu perfil.</p>
        </div>
        <div className="flex gap-2 w-full sm:w-auto">
          <input 
            type="text" 
            placeholder="Buscar por cargo..." 
            className="flex-1 sm:w-64 px-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary text-sm"
          />
          <Button variant="outline" size="sm">Filtros</Button>
        </div>
      </div>

      <div className="space-y-4">
        {mockOfertas.filter(o => o.estado !== 'CERRADA').map((oferta) => (
          <Card key={oferta.id} className="hover:border-primary/30 transition-colors">
            <CardHeader className="flex justify-between items-start pb-2">
              <div>
                <h3 className="font-semibold text-xl text-gray-900">{oferta.titulo}</h3>
                <p className="text-sm text-gray-500 font-medium">{oferta.empresa} • {oferta.ubicacion}</p>
              </div>
              <Badge variant="info">Match: 85%</Badge>
            </CardHeader>
            <CardContent className="py-2">
              <p className="text-gray-700 text-sm mb-4">{oferta.descripcion}</p>
              <div className="grid grid-cols-2 gap-4 text-sm bg-gray-50 p-3 rounded-md">
                <div>
                  <span className="text-gray-500 block text-xs uppercase tracking-wider">Fechas</span>
                  <span className="font-medium">{oferta.fechaInicio} al {oferta.fechaFin}</span>
                </div>
                <div>
                  <span className="text-gray-500 block text-xs uppercase tracking-wider">Pago Líquido</span>
                  <span className="font-medium text-green-700">${oferta.remuneracion.toLocaleString('es-CL')}</span>
                </div>
              </div>
            </CardContent>
            <CardFooter className="flex justify-end gap-2 pt-3">
              <Button variant="primary">Postular Ahora</Button>
            </CardFooter>
          </Card>
        ))}
      </div>
    </div>
  );
}
