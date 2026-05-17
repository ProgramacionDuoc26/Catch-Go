"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { PlusCircle, Users, Briefcase, FileText, Loader2 } from "lucide-react";
import { jobsApi } from "@/lib/api/jobs";

export default function EmpresaDashboard() {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    activeJobs: 0,
    newCandidates: 0,
    completedJobs: 0
  });
  const [recentOffers, setRecentOffers] = useState<any[]>([]);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        let realEmpresaId = '';
        
        // 1. Intentar primero con localStorage (fuente de verdad de la sesión activa en el frontend)
        const storedUser = localStorage.getItem('user_info');
        if (storedUser) {
          try {
            const parsed = JSON.parse(storedUser);
            realEmpresaId = parsed.id?.toString() || '';
          } catch (e) {
            console.error('Error al parsear user_info de localStorage:', e);
          }
        }

        // 2. Si no hay en localStorage, usar Supabase como fallback
        if (!realEmpresaId) {
          const { createClient } = await import('@/lib/supabase/client');
          const supabase = createClient();
          const { data: { user } } = await supabase.auth.getUser();
          if (user) {
            realEmpresaId = user.id;
          }
        }

        if (!realEmpresaId) return;

        const offersRes = await jobsApi.list();
        if (offersRes.data) {
          const myOffers = offersRes.data.filter((o: any) => o.empresaId === realEmpresaId);
          setRecentOffers(myOffers.slice(0, 3));
          
          const appsRes = await jobsApi.getApplicationsByEmployerId(realEmpresaId);
          setStats({
            activeJobs: myOffers.length,
            newCandidates: appsRes.data?.length || 0,
            completedJobs: 0 // Placeholder for now
          });
        }
      } catch (error) {
        console.error("Error loading dashboard data:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-gray-500">Cargando dashboard...</p>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-text-main">Dashboard Empresa</h1>
          <p className="text-text-muted mt-1">Bienvenido, gestiona tus ofertas y candidatos.</p>
        </div>
        <Link href="/empresa/ofertas/nueva">
          <button className="bg-primary text-white px-4 py-2 rounded-md hover:bg-primary-dark transition-colors font-semibold flex items-center gap-2 min-h-[44px]">
            <PlusCircle className="w-5 h-5" />
            Nueva Oferta
          </button>
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-surface p-6 rounded-xl border border-gray-100 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-blue-50 text-primary rounded-lg">
            <Briefcase className="w-8 h-8" />
          </div>
          <div>
            <p className="text-text-muted text-sm font-medium">Ofertas Activas</p>
            <p className="text-2xl font-bold text-text-main">{stats.activeJobs}</p>
          </div>
        </div>
        <div className="bg-surface p-6 rounded-xl border border-gray-100 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-green-50 text-semantic-success rounded-lg">
            <Users className="w-8 h-8" />
          </div>
          <div>
            <p className="text-text-muted text-sm font-medium">Candidatos</p>
            <p className="text-2xl font-bold text-text-main">{stats.newCandidates}</p>
          </div>
        </div>
        <div className="bg-surface p-6 rounded-xl border border-gray-100 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-purple-50 text-purple-600 rounded-lg">
            <FileText className="w-8 h-8" />
          </div>
          <div>
            <p className="text-text-muted text-sm font-medium">Turnos Completados</p>
            <p className="text-2xl font-bold text-text-main">{stats.completedJobs}</p>
          </div>
        </div>
      </div>

      <div className="bg-surface rounded-xl border border-gray-100 shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 bg-gray-50 flex justify-between items-center">
          <h2 className="text-lg font-semibold text-text-main">Ofertas Recientes</h2>
          <Link href="/empresa/ofertas" className="text-sm font-medium text-primary hover:underline">
            Ver todas
          </Link>
        </div>
        <div className="divide-y divide-gray-100">
          {recentOffers.length > 0 ? recentOffers.map((oferta) => (
            <div key={oferta.id} className="p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <h3 className="font-semibold text-text-main">{oferta.titulo}</h3>
                <p className="text-sm text-text-muted mt-1">{oferta.ubicacion} • {oferta.fechaInicio}</p>
              </div>
              <div className="flex items-center gap-4">
                <span className="px-3 py-1 bg-green-100 text-green-800 text-xs font-semibold rounded-full">
                  {oferta.estado}
                </span>
                <Link href={`/empresa/candidatos`} className="text-sm font-medium text-primary hover:underline">
                  Ver Candidatos
                </Link>
              </div>
            </div>
          )) : (
            <p className="p-6 text-center text-gray-500 text-sm">No hay ofertas recientes.</p>
          )}
        </div>
      </div>
    </div>
  );
}
