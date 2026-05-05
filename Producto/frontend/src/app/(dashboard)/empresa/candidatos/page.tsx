import React from 'react';
import { Card, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { mockCandidatos } from '@/lib/mockData';

export default function EmpresaCandidatosPage() {
  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Candidatos (Matches)</h1>
        <p className="text-gray-500 text-sm mt-1">Revisa los trabajadores que hacen match con tus ofertas y selecciónalos.</p>
      </div>

      <div className="space-y-4">
        {mockCandidatos.map((candidato) => (
          <Card key={candidato.id} className="hover:border-primary/50 transition-colors">
            <CardContent className="p-4 sm:p-6 flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
              <div className="flex-1 space-y-2">
                <div className="flex items-center gap-3">
                  <h3 className="font-bold text-lg text-gray-900">{candidato.nombre}</h3>
                  <Badge variant={candidato.score >= 90 ? 'success' : candidato.score >= 70 ? 'info' : 'warning'}>
                    Match: {candidato.score}%
                  </Badge>
                  {candidato.estado === 'ACEPTADO' && <Badge variant="success">Seleccionado</Badge>}
                </div>
                
                <p className="text-sm text-gray-600 font-medium">Postula a: {candidato.ofertaPostulada}</p>
                
                <div className="text-sm text-gray-500">
                  <span className="font-medium">Experiencia:</span> {candidato.experiencia}
                </div>
                
                {candidato.certificaciones.length > 0 && (
                  <div className="flex gap-2 mt-2 flex-wrap">
                    {candidato.certificaciones.map((cert, i) => (
                      <span key={i} className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800 border border-gray-200">
                        ✓ {cert}
                      </span>
                    ))}
                  </div>
                )}
              </div>
              
              <div className="flex flex-col sm:flex-row gap-2 w-full sm:w-auto">
                <Button variant="outline" size="sm">Ver Perfil</Button>
                {candidato.estado === 'PENDIENTE' && (
                  <>
                    <Button variant="primary" size="sm">Aceptar</Button>
                    <Button variant="ghost" size="sm" className="text-red-600 hover:text-red-700 hover:bg-red-50">Rechazar</Button>
                  </>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
