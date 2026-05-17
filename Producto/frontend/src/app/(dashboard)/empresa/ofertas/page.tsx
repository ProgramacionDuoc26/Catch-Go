"use client";

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardFooter } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { jobsApi } from '@/lib/api/jobs';
import { Oferta } from '@/lib/api/types';
import { Plus, Users, Calendar, MapPin, Loader2, Trash2, Edit } from 'lucide-react';

export default function EmpresaOfertasPage() {
  const [ofertas, setOfertas] = useState<Oferta[]>([]);
  const [loading, setLoading] = useState(true);
  const [appsCount, setAppsCount] = useState(0);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        // Obtener el ID real de la empresa
        let realEmpresaId = '';
        const storedUser = localStorage.getItem('user_info');
        if (storedUser) {
          const parsed = JSON.parse(storedUser);
          realEmpresaId = parsed.id?.toString() || '';
        }

        if (!realEmpresaId) {
          console.warn('No se encontró sesión de empresa.');
          setLoading(false);
          return;
        }

        // Fetch ofertas
        const response = await jobsApi.list();
        if (response.data) {
          setOfertas(response.data.filter(o => o.empresaId === realEmpresaId));
        }

        // Fetch applications count
        const res = await jobsApi.getApplicationsByEmployerId(realEmpresaId);
        if (res.data) {
          setAppsCount(res.data.length);
        }
      } catch (error) {
        console.error('Error fetching data:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleDelete = async (id: string) => {
    if (!confirm('¿Estás seguro de que deseas eliminar esta oferta?')) return;
    try {
      const res = await jobsApi.delete(id);
      if (!res.error) {
        setOfertas(prev => prev.filter(o => o.id !== id));
        alert('Oferta eliminada con éxito.');
      } else {
        alert('Error al eliminar: ' + JSON.stringify(res.error));
      }
    } catch (error) {
      console.error('Error deleting offer:', error);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-gray-500 animate-pulse">Cargando tus ofertas...</p>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Mis Ofertas Publicadas</h1>
          <p className="text-gray-500 text-sm mt-1">Gestiona los turnos que has publicado para tu empresa.</p>
        </div>
        <Link href="/empresa/ofertas/nueva">
          <Button variant="primary" className="gap-2 shadow-lg shadow-primary/20">
            <Plus className="w-4 h-4" />
            Publicar Nuevo Turno
          </Button>
        </Link>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
        <Link href="/empresa/ofertas" className="block transition-all hover:scale-[1.02]">
          <Card className="bg-white border-none shadow-sm hover:shadow-md cursor-pointer h-full">
            <CardContent className="p-4 flex flex-col items-center justify-center">
              <span className="block text-2xl font-bold text-primary">{ofertas.length}</span>
              <span className="text-xs text-gray-500 font-medium uppercase tracking-wider">Turnos Activos</span>
            </CardContent>
          </Card>
        </Link>
        
        <Link href="/empresa/candidatos" className="block transition-all hover:scale-[1.02]">
          <Card className="bg-white border-none shadow-sm hover:shadow-md cursor-pointer h-full">
            <CardContent className="p-4 flex flex-col items-center justify-center">
              <span className="block text-2xl font-bold text-primary">{appsCount}</span>
              <span className="text-xs text-gray-500 font-medium uppercase tracking-wider">Candidatos Totales</span>
            </CardContent>
          </Card>
        </Link>

        <Link href="/empresa/candidatos" className="block transition-all hover:scale-[1.02]">
          <Card className="bg-white border-none shadow-sm hover:shadow-md cursor-pointer h-full">
            <CardContent className="p-4 flex flex-col items-center justify-center">
              <span className="block text-2xl font-bold text-green-600">0</span>
              <span className="text-xs text-gray-500 font-medium uppercase tracking-wider">Por Pagar</span>
            </CardContent>
          </Card>
        </Link>

        <Link href="/empresa/candidatos" className="block transition-all hover:scale-[1.02]">
          <Card className="bg-white border-none shadow-sm hover:shadow-md cursor-pointer h-full">
            <CardContent className="p-4 flex flex-col items-center justify-center">
              <span className="block text-2xl font-bold text-amber-600">0</span>
              <span className="text-xs text-gray-500 font-medium uppercase tracking-wider">En Revisión</span>
            </CardContent>
          </Card>
        </Link>
      </div>

      <div className="grid grid-cols-1 gap-4">
        {ofertas.length > 0 ? ofertas.map((oferta) => (
          <Card key={oferta.id} className="hover:border-primary/30 transition-all border-gray-100 shadow-sm overflow-hidden">
            <CardHeader className="flex justify-between items-start pb-2">
              <div>
                <h3 className="font-semibold text-xl text-gray-900">{oferta.titulo}</h3>
                <div className="flex items-center gap-4 text-sm text-gray-500 mt-1">
                  <span className="flex items-center gap-1"><MapPin size={14} /> {oferta.ubicacion}</span>
                  <span className="flex items-center gap-1"><Calendar size={14} /> {oferta.fechaInicio}</span>
                </div>
              </div>
              <Badge variant={oferta.estado === 'ABIERTA' ? 'success' : 'info'}>
                {oferta.estado}
              </Badge>
            </CardHeader>
            <CardContent className="py-2">
              <p className="text-gray-600 text-sm line-clamp-2">{oferta.descripcion}</p>
              <div className="mt-4 flex gap-6 border-t pt-4">
                <div className="text-center">
                  <span className="block text-lg font-bold text-primary">
                    <Users className="inline w-4 h-4 mr-1 mb-1" />
                    ?
                  </span>
                  <span className="text-[10px] text-gray-500 uppercase tracking-widest font-bold">Candidatos</span>
                </div>
                <div className="text-center border-l pl-6">
                  <span className="block text-lg font-bold text-green-700">${oferta.remuneracion.toLocaleString('es-CL')}</span>
                  <span className="text-[10px] text-gray-500 uppercase tracking-widest font-bold">Pago Turno</span>
                </div>
              </div>
            </CardContent>
            <CardFooter className="flex justify-end gap-2 pt-3 border-t bg-gray-50/30">
              <Button variant="ghost" size="sm" className="text-gray-500" onClick={() => alert('Detalles pronto disponibles para ID: ' + oferta.id)}>Ver Detalles</Button>
              <Link href={`/empresa/ofertas/${oferta.id}/editar`}>
                <Button variant="outline" size="sm" className="text-amber-600 border-amber-200 hover:bg-amber-50">
                  <Edit className="w-3 h-3 mr-1" /> Editar
                </Button>
              </Link>
              <Button variant="ghost" size="sm" className="text-red-600 hover:bg-red-50" onClick={() => handleDelete(oferta.id)}>
                <Trash2 className="w-3 h-3 mr-1" /> Eliminar
              </Button>
            </CardFooter>
          </Card>
        )) : (
          <div className="text-center py-20 bg-white rounded-xl border-2 border-dashed border-gray-100">
            <div className="max-w-xs mx-auto space-y-4">
              <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto text-gray-300">
                <Plus size={32} />
              </div>
              <h3 className="text-lg font-medium text-gray-900">No tienes turnos publicados</h3>
              <p className="text-gray-500 text-sm">Comienza a publicar para encontrar a los mejores workers.</p>
              <Link href="/empresa/ofertas/nueva">
                <Button variant="primary">Publicar Primer Turno</Button>
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
