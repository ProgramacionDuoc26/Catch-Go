import React from 'react';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardFooter } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { mockOfertas } from '@/lib/mockData';

export default function EmpresaOfertasPage() {
  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Mis Ofertas Publicadas</h1>
          <p className="text-gray-500 text-sm mt-1">Gestiona los turnos y trabajos que has publicado.</p>
        </div>
        <Link href="/empresa/ofertas/nueva">
          <Button variant="primary">Publicar Nuevo Turno</Button>
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {mockOfertas.map((oferta) => (
          <Card key={oferta.id}>
            <CardHeader className="flex justify-between items-start pb-2">
              <div>
                <h3 className="font-semibold text-lg text-gray-900">{oferta.titulo}</h3>
                <p className="text-sm text-gray-500">{oferta.categoria} • {oferta.ubicacion}</p>
              </div>
              <Badge variant={oferta.estado === 'ABIERTA' ? 'success' : 'warning'}>
                {oferta.estado}
              </Badge>
            </CardHeader>
            <CardContent className="py-2">
              <div className="flex flex-col gap-2 text-sm text-gray-700">
                <div className="flex justify-between">
                  <span className="text-gray-500">Fecha:</span>
                  <span className="font-medium">{oferta.fechaInicio} al {oferta.fechaFin}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Remuneración:</span>
                  <span className="font-medium">${oferta.remuneracion.toLocaleString('es-CL')}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Postulantes:</span>
                  <span className="font-medium text-primary">{oferta.postulantes} candidatos</span>
                </div>
              </div>
            </CardContent>
            <CardFooter className="flex justify-end gap-2 pt-3">
              <Link href={`/empresa/candidatos?oferta=${oferta.id}`}>
                <Button variant="outline" size="sm">Ver Candidatos</Button>
              </Link>
            </CardFooter>
          </Card>
        ))}
      </div>
    </div>
  );
}
