import Link from "next/link";
import { PlusCircle, Users, Briefcase, FileText } from "lucide-react";

export default function EmpresaDashboard() {
  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-text-main">Dashboard Empresa</h1>
          <p className="text-text-muted mt-1">Bienvenido, gestiona tus ofertas y candidatos.</p>
        </div>
        <button className="bg-primary text-white px-4 py-2 rounded-md hover:bg-primary-dark transition-colors font-semibold flex items-center gap-2 min-h-[44px]">
          <PlusCircle className="w-5 h-5" />
          Nueva Oferta
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-surface p-6 rounded-xl border border-gray-100 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-blue-50 text-primary rounded-lg">
            <Briefcase className="w-8 h-8" />
          </div>
          <div>
            <p className="text-text-muted text-sm font-medium">Ofertas Activas</p>
            <p className="text-2xl font-bold text-text-main">3</p>
          </div>
        </div>
        <div className="bg-surface p-6 rounded-xl border border-gray-100 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-green-50 text-semantic-success rounded-lg">
            <Users className="w-8 h-8" />
          </div>
          <div>
            <p className="text-text-muted text-sm font-medium">Nuevos Candidatos</p>
            <p className="text-2xl font-bold text-text-main">12</p>
          </div>
        </div>
        <div className="bg-surface p-6 rounded-xl border border-gray-100 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-purple-50 text-purple-600 rounded-lg">
            <FileText className="w-8 h-8" />
          </div>
          <div>
            <p className="text-text-muted text-sm font-medium">Turnos Completados</p>
            <p className="text-2xl font-bold text-text-main">45</p>
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
          {[1, 2, 3].map((i) => (
            <div key={i} className="p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <h3 className="font-semibold text-text-main">Guardia de Seguridad - Evento Masivo</h3>
                <p className="text-sm text-text-muted mt-1">Movistar Arena, Santiago • 12 Octubre</p>
              </div>
              <div className="flex items-center gap-4">
                <span className="px-3 py-1 bg-green-100 text-green-800 text-xs font-semibold rounded-full">
                  Abierta
                </span>
                <button className="text-sm font-medium text-primary hover:underline">
                  Ver Candidatos (4)
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
