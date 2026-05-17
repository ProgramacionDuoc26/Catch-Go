"use client";

import React, { useEffect, useState } from 'react';
import { 
  Users, 
  Building2, 
  Briefcase, 
  ClipboardList, 
  BarChart3, 
  ShieldCheck, 
  LogOut, 
  Clock, 
  ArrowUpRight,
  FileText,
  Star,
  Wallet,
  Loader2,
  Plus,
  Trash2,
  Search,
  UserPlus,
  Settings,
  Mail,
  Phone,
  LayoutDashboard,
  TrendingUp
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { adminApi, AdminStats } from '@/lib/api/admin';
import { Profile } from '@/lib/api/profile';
import { Button } from '@/components/ui/Button';
import { useSearchParams } from 'next/navigation';

type View = 'overview' | 'reports' | 'admins';

const KpiCard = ({ title, value, subtext, icon: Icon, color }: any) => (
  <motion.div 
    whileHover={{ y: -5 }}
    className="bg-white p-6 rounded-3xl shadow-sm border border-slate-100 flex flex-col justify-between"
  >
    <div className="flex justify-between items-start mb-4">
      <div>
        <p className="text-slate-500 text-sm font-medium">{title}</p>
        <h3 className="text-3xl font-bold mt-1 text-slate-800">{value}</h3>
      </div>
      <div className={`p-3 rounded-2xl ${color} bg-opacity-10 text-opacity-100`}>
        <Icon size={24} className={color.replace('bg-', 'text-')} />
      </div>
    </div>
    {subtext && (
      <p className="text-xs text-slate-400 font-medium">
        {subtext}
      </p>
    )}
  </motion.div>
);

export default function AdminDashboard() {
  const searchParams = useSearchParams();
  const initialTab = (searchParams.get('tab') as View) || 'overview';
  const [activeView, setActiveView] = useState<View>(initialTab);
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [admins, setAdmins] = useState<Profile[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  // Form states for new admin
  const [showAddAdmin, setShowAddAdmin] = useState(false);
  const [newAdmin, setNewAdmin] = useState({ name: '', email: '', password: '', phone: '', tipo: 'ADMIN' });

  useEffect(() => {
    const tab = searchParams.get('tab') as View;
    if (tab && (tab === 'overview' || tab === 'reports' || tab === 'admins')) {
      setActiveView(tab);
    }
  }, [searchParams]);

  useEffect(() => {
    fetchInitialData();
  }, []);

  const fetchInitialData = async () => {
    setLoading(true);
    try {
      const [statsData, adminsData] = await Promise.all([
        adminApi.getStats(),
        adminApi.getAdmins()
      ]);
      setStats(statsData);
      setAdmins(adminsData);
    } catch (error) {
      console.error("Error fetching admin data:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateAdmin = async (e: React.FormEvent) => {
    e.preventDefault();
    setActionLoading(true);
    try {
      await adminApi.createAdmin(newAdmin);
      await fetchInitialData();
      setShowAddAdmin(false);
      setNewAdmin({ name: '', email: '', password: '', phone: '', tipo: 'ADMIN' });
    } catch (error) {
      alert("Error al crear administrador");
    } finally {
      setActionLoading(false);
    }
  };

  const handleDeleteAdmin = async (userId: string) => {
    if (!confirm("¿Estás seguro de eliminar este administrador?")) return;
    setActionLoading(true);
    try {
      await adminApi.deleteAdmin(userId);
      await fetchInitialData();
    } catch (error) {
      alert("Error al eliminar");
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="text-center">
          <Loader2 className="w-12 h-12 text-blue-600 animate-spin mx-auto mb-4" />
          <p className="text-slate-600 font-medium font-outfit">Sincronizando datos...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 p-6 lg:p-10">
      {/* ═══ TOP NAVBAR ═══ */}
      <div className="max-w-7xl mx-auto mb-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <h1 className="text-3xl font-black text-slate-900 tracking-tight flex items-center gap-3">
            <ShieldCheck size={32} className="text-blue-600" />
            Centro de Control
          </h1>
          <p className="text-slate-500 font-medium">Bienvenido, Miguel Admin</p>
        </div>

        <div className="flex bg-white p-1.5 rounded-2xl shadow-sm border border-slate-200">
          <button 
            onClick={() => setActiveView('overview')}
            className={`px-6 py-2.5 rounded-xl text-sm font-bold transition-all flex items-center gap-2 ${activeView === 'overview' ? 'bg-blue-600 text-white shadow-lg' : 'text-slate-500 hover:bg-slate-50'}`}
          >
            <LayoutDashboard size={18} /> Resumen
          </button>
          <button 
            onClick={() => setActiveView('reports')}
            className={`px-6 py-2.5 rounded-xl text-sm font-bold transition-all flex items-center gap-2 ${activeView === 'reports' ? 'bg-blue-600 text-white shadow-lg' : 'text-slate-500 hover:bg-slate-50'}`}
          >
            <BarChart3 size={18} /> Reportes
          </button>
          <button 
            onClick={() => setActiveView('admins')}
            className={`px-6 py-2.5 rounded-xl text-sm font-bold transition-all flex items-center gap-2 ${activeView === 'admins' ? 'bg-blue-600 text-white shadow-lg' : 'text-slate-500 hover:bg-slate-50'}`}
          >
            <Users size={18} /> Cuentas Admin
          </button>
        </div>
      </div>

      <div className="max-w-7xl mx-auto">
        <AnimatePresence mode="wait">
          {/* ═══ VIEW: OVERVIEW ═══ */}
          {activeView === 'overview' && (
            <motion.div 
              key="overview"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="space-y-10"
            >
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <KpiCard title="Empresas" value={stats?.totalCompanies || 0} icon={Building2} color="bg-blue-600" />
                <KpiCard title="Trabajadores" value={stats?.totalWorkers || 0} icon={Users} color="bg-emerald-600" />
                <KpiCard title="Ofertas" value={stats?.totalOffers || 0} icon={Briefcase} color="bg-amber-600" />
                <KpiCard title="Postulaciones" value={stats?.totalApplications || 0} icon={ClipboardList} color="bg-purple-600" />
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                <div className="bg-white p-8 rounded-[2.5rem] shadow-sm border border-slate-100">
                  <h3 className="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
                    <Clock size={20} className="text-blue-600" /> Registros Recientes
                  </h3>
                  <div className="space-y-4">
                    {stats?.recentProfiles.map((p, i) => (
                      <div key={i} className="flex items-center justify-between p-4 rounded-2xl bg-slate-50/50">
                        <div className="flex items-center gap-4">
                          <div className="w-10 h-10 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center font-bold">
                            {p.name?.charAt(0)}
                          </div>
                          <div>
                            <p className="font-bold text-slate-800">{p.name}</p>
                            <p className="text-xs text-slate-500">{p.email}</p>
                          </div>
                        </div>
                        <span className={`px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider ${p.type === 'TRABAJADOR' ? 'bg-emerald-100 text-emerald-700' : 'bg-blue-100 text-blue-700'}`}>
                          {p.type}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="bg-white p-8 rounded-[2.5rem] shadow-sm border border-slate-100">
                  <h3 className="text-xl font-bold text-slate-800 mb-6">Estado del Sistema</h3>
                  <div className="space-y-6">
                    <div className="p-6 bg-blue-50 rounded-3xl border border-blue-100">
                      <div className="flex justify-between items-center mb-2">
                        <span className="text-sm font-bold text-blue-900">Postulaciones Pendientes</span>
                        <span className="text-xl font-black text-blue-600">{stats?.pendingApplications}</span>
                      </div>
                      <div className="h-2 w-full bg-blue-100 rounded-full overflow-hidden">
                        <div className="h-full bg-blue-600" style={{ width: '45%' }} />
                      </div>
                    </div>
                    <div className="p-6 bg-emerald-50 rounded-3xl border border-emerald-100">
                      <div className="flex justify-between items-center mb-2">
                        <span className="text-sm font-bold text-emerald-900">Tasa de Conversión</span>
                        <span className="text-xl font-black text-emerald-600">88%</span>
                      </div>
                      <div className="h-2 w-full bg-emerald-100 rounded-full overflow-hidden">
                        <div className="h-full bg-emerald-600" style={{ width: '88%' }} />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </motion.div>
          )}

          {/* ═══ VIEW: REPORTS ═══ */}
          {activeView === 'reports' && (
            <motion.div 
              key="reports"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="bg-white p-10 rounded-[3rem] shadow-sm border border-slate-100"
            >
              <div className="max-w-4xl mx-auto text-center space-y-8 py-10">
                <div className="w-24 h-24 bg-rose-50 text-rose-600 rounded-3xl flex items-center justify-center mx-auto mb-6">
                  <BarChart3 size={48} />
                </div>
                <h2 className="text-4xl font-black text-slate-900">Reportes Ejecutivos</h2>
                <p className="text-slate-500 text-lg max-w-xl mx-auto font-medium">
                  Genera informes detallados sobre la actividad de trabajadores, empresas y el rendimiento de las ofertas en tiempo real.
                </p>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-12 text-left">
                  <div className="p-8 bg-slate-50 rounded-[2rem] border border-slate-100 hover:border-blue-200 transition-all cursor-pointer group">
                    <FileText className="text-blue-600 mb-4 group-hover:scale-110 transition-transform" size={32} />
                    <h4 className="text-xl font-bold text-slate-800">Reporte de Usuarios</h4>
                    <p className="text-sm text-slate-500 mt-2">PDF detallado con el crecimiento mensual de la comunidad.</p>
                  </div>
                  <div className="p-8 bg-slate-50 rounded-[2rem] border border-slate-100 hover:border-blue-200 transition-all cursor-pointer group">
                    <TrendingUp className="text-emerald-600 mb-4 group-hover:scale-110 transition-transform" size={32} />
                    <h4 className="text-xl font-bold text-slate-800">Métricas de Ofertas</h4>
                    <p className="text-sm text-slate-500 mt-2">Análisis de los sectores más demandados y tiempos de cobertura.</p>
                  </div>
                </div>
              </div>
            </motion.div>
          )}

          {/* ═══ VIEW: ADMINS (CRUD) ═══ */}
          {activeView === 'admins' && (
            <motion.div 
              key="admins"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="space-y-6"
            >
              <div className="bg-white p-8 rounded-[2.5rem] shadow-sm border border-slate-100">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
                  <div>
                    <h3 className="text-xl font-bold text-slate-800">Gestión de Cuentas Administrativas</h3>
                    <p className="text-sm text-slate-500 mt-1">Administra quién tiene acceso al panel de control.</p>
                  </div>
                  <Button 
                    onClick={() => setShowAddAdmin(true)}
                    className="bg-blue-600 hover:bg-blue-700 text-white rounded-2xl flex items-center gap-2 px-6"
                  >
                    <UserPlus size={18} /> Nuevo Administrador
                  </Button>
                </div>

                <div className="overflow-x-auto">
                  <table className="w-full text-left">
                    <thead>
                      <tr className="border-b border-slate-100">
                        <th className="pb-4 font-bold text-slate-400 text-xs uppercase tracking-widest px-4">Nombre / Email</th>
                        <th className="pb-4 font-bold text-slate-400 text-xs uppercase tracking-widest px-4">Teléfono</th>
                        <th className="pb-4 font-bold text-slate-400 text-xs uppercase tracking-widest px-4">Rol</th>
                        <th className="pb-4 font-bold text-slate-400 text-xs uppercase tracking-widest px-4 text-right">Acciones</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-50">
                      {admins.map((admin, i) => (
                        <tr key={i} className="group hover:bg-slate-50/50 transition-colors">
                          <td className="py-5 px-4">
                            <div className="flex items-center gap-3">
                              <div className="w-10 h-10 rounded-full bg-indigo-50 text-indigo-600 flex items-center justify-center font-bold">
                                {admin.name?.charAt(0)}
                              </div>
                              <div>
                                <p className="font-bold text-slate-800">{admin.name}</p>
                                <p className="text-xs text-slate-500">{admin.email}</p>
                              </div>
                            </div>
                          </td>
                          <td className="py-5 px-4">
                            <div className="flex items-center gap-2 text-slate-600 text-sm font-medium">
                              <Phone size={14} className="text-slate-400" /> {admin.phone || 'N/A'}
                            </div>
                          </td>
                          <td className="py-5 px-4">
                            <span className={`px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider ${admin.type === 'ADMIN' ? 'bg-indigo-100 text-indigo-700' : 'bg-slate-100 text-slate-700'}`}>
                              {admin.type === 'ADMIN' ? 'Full Admin' : 'Sub Admin'}
                            </span>
                          </td>
                          <td className="py-5 px-4 text-right">
                            <button 
                              onClick={() => handleDeleteAdmin(admin.userId)}
                              disabled={actionLoading}
                              className="p-2.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-xl transition-all"
                            >
                              <Trash2 size={18} />
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* ═══ ADD ADMIN MODAL ═══ */}
      {showAddAdmin && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm">
          <motion.div 
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            className="bg-white rounded-[2.5rem] shadow-2xl p-10 max-w-md w-full border border-slate-100"
          >
            <h3 className="text-2xl font-black text-slate-900 mb-2">Nuevo Administrador</h3>
            <p className="text-slate-500 mb-8 font-medium">Asigna permisos de gestión a un nuevo usuario.</p>
            
            <form onSubmit={handleCreateAdmin} className="space-y-5">
              <div className="space-y-2">
                <label className="text-xs font-black uppercase tracking-widest text-slate-400 ml-1">Nombre Completo</label>
                <input 
                  required
                  value={newAdmin.name}
                  onChange={(e) => setNewAdmin({...newAdmin, name: e.target.value})}
                  className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 focus:ring-4 focus:ring-blue-100 outline-none transition-all font-medium" 
                  placeholder="Ej: Andres Lopez"
                />
              </div>
              <div className="space-y-2">
                <label className="text-xs font-black uppercase tracking-widest text-slate-400 ml-1">Correo Electrónico</label>
                <input 
                  required
                  type="email"
                  value={newAdmin.email}
                  onChange={(e) => setNewAdmin({...newAdmin, email: e.target.value})}
                  className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 focus:ring-4 focus:ring-blue-100 outline-none transition-all font-medium" 
                  placeholder="admin@catchandgo.cl"
                />
              </div>
              <div className="space-y-2">
                <label className="text-xs font-black uppercase tracking-widest text-slate-400 ml-1">Contraseña</label>
                <input 
                  required
                  type="password"
                  value={newAdmin.password}
                  onChange={(e) => setNewAdmin({...newAdmin, password: e.target.value})}
                  className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 focus:ring-4 focus:ring-blue-100 outline-none transition-all font-medium" 
                  placeholder="••••••••"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-black uppercase tracking-widest text-slate-400 ml-1">Rol de Acceso</label>
                <select 
                  value={newAdmin.tipo}
                  onChange={(e) => setNewAdmin({...newAdmin, tipo: e.target.value})}
                  className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 focus:ring-4 focus:ring-blue-100 outline-none transition-all font-medium appearance-none"
                >
                  <option value="ADMIN">Full Admin (Acceso Total)</option>
                  <option value="SUB_ADMIN">Sub Admin (Sólo Lectura/Limitado)</option>
                </select>
              </div>
              
              <div className="flex gap-4 pt-4">
                <Button 
                  type="button"
                  variant="outline"
                  onClick={() => setShowAddAdmin(false)}
                  className="flex-1 rounded-2xl py-4 h-auto"
                >
                  Cancelar
                </Button>
                <Button 
                  disabled={actionLoading}
                  className="flex-1 bg-blue-600 hover:bg-blue-700 text-white rounded-2xl py-4 h-auto font-bold"
                >
                  {actionLoading ? <Loader2 className="animate-spin" /> : 'Crear Admin'}
                </Button>
              </div>
            </form>
          </motion.div>
        </div>
      )}
    </div>
  );
}

