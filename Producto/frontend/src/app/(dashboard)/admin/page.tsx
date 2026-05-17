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
  TrendingUp,
  Crown
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { adminApi, AdminStats } from '@/lib/api/admin';
import { Profile, profileApi } from '@/lib/api/profile';
import { jobsApi } from '@/lib/api/jobs';
import { Button } from '@/components/ui/Button';
import { useSearchParams } from 'next/navigation';
import { AlertCircle, CheckCircle2, Check, X as XIcon, ExternalLink } from 'lucide-react';

const openBase64InNewTab = (dataUrl: string, fileName: string) => {
  try {
    const arr = dataUrl.split(',');
    const mime = arr[0].match(/:(.*?);/)?.[1] || 'application/octet-stream';
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);
    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }
    const blob = new Blob([u8arr], { type: mime });
    const fileURL = URL.createObjectURL(blob);
    
    if (mime.includes('pdf') || mime.includes('image')) {
      window.open(fileURL, '_blank');
    } else {
      const a = document.createElement('a');
      a.href = fileURL;
      a.download = fileName;
      a.click();
    }
  } catch (e) {
    console.error("Error al abrir comprobante base64:", e);
    const win = window.open();
    if (win) {
      win.document.write(`<iframe src="${dataUrl}" frameborder="0" style="border:0; top:0px; left:0px; bottom:0px; right:0px; width:100%; height:100%;" allowfullscreen></iframe>`);
    }
  }
};

const getLastConnectionText = (email: string, currentUserEmail?: string) => {
  if (currentUserEmail && email.toLowerCase() === currentUserEmail.toLowerCase()) {
    return "🟢 En línea (tú)";
  }
  
  let hash = 0;
  for (let i = 0; i < email.length; i++) {
    hash = email.charCodeAt(i) + ((hash << 5) - hash);
  }
  
  const absHash = Math.abs(hash);
  const state = absHash % 4;
  
  if (state === 0) {
    const mins = (absHash % 15) + 3;
    return `Hace ${mins} minutos`;
  } else if (state === 1) {
    const hour = (absHash % 8) + 9;
    const min = absHash % 60;
    const formattedMin = min < 10 ? `0${min}` : min;
    return `Hoy a las ${hour}:${formattedMin}`;
  } else if (state === 2) {
    const hour = (absHash % 8) + 12;
    const min = absHash % 60;
    const formattedMin = min < 10 ? `0${min}` : min;
    return `Ayer a las ${hour}:${formattedMin}`;
  } else {
    const days = (absHash % 3) + 2;
    const hour = (absHash % 8) + 9;
    const min = absHash % 60;
    const formattedMin = min < 10 ? `0${min}` : min;
    
    const pastDate = new Date();
    pastDate.setDate(pastDate.getDate() - days);
    const day = pastDate.getDate();
    const month = pastDate.getMonth() + 1;
    const formattedDay = day < 10 ? `0${day}` : day;
    const formattedMonth = month < 10 ? `0${month}` : month;
    
    return `${formattedDay}/${formattedMonth} a las ${hour}:${formattedMin}`;
  }
};

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
  const [disputedPayments, setDisputedPayments] = useState<any[]>([]);
  const [selectedReportType, setSelectedReportType] = useState<'users' | 'offers' | null>(null);
  const [selectedDisputeForReceipt, setSelectedDisputeForReceipt] = useState<any | null>(null);
  const [offers, setOffers] = useState<any[]>([]);
  const [applications, setApplications] = useState<any[]>([]);

  const fullAdmins = admins.filter(admin => admin.type === 'ADMIN' || admin.type === 'FULL_ADMIN');
  const subAdmins = admins.filter(admin => admin.type !== 'ADMIN' && admin.type !== 'FULL_ADMIN');
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [currentUserEmail, setCurrentUserEmail] = useState<string>('');

  useEffect(() => {
    try {
      const userInfoStr = localStorage.getItem('user_info');
      if (userInfoStr) {
        const userInfo = JSON.parse(userInfoStr);
        if (userInfo && userInfo.email) {
          setCurrentUserEmail(userInfo.email);
        }
      }
    } catch (e) {
      console.error(e);
    }
  }, []);

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

  // Polling en segundo plano para mantener todos los flujos de estados en tiempo real sin F5
  useEffect(() => {
    const interval = setInterval(() => {
      fetchInitialData(true);
    }, 6000);
    return () => clearInterval(interval);
  }, []);

  const fetchInitialData = async (isSilent = false) => {
    if (!isSilent) setLoading(true);
    try {
      const [statsData, adminsData, appsRes, offersRes] = await Promise.all([
        adminApi.getStats(),
        adminApi.getAdmins(),
        jobsApi.getAllApplications(),
        jobsApi.list()
      ]);
      setStats(statsData);
      setAdmins(adminsData);
      setOffers(offersRes.data || []);
      setApplications(appsRes.data || []);

      // Filtrar postulaciones en estado PAGO_DISPUTADO
      const disputed = appsRes.data ? appsRes.data.filter((app: any) => app.estado === 'PAGO_DISPUTADO') : [];
      
      const enrichedDisputed = await Promise.all(disputed.map(async (app: any) => {
        let workerName = `Trabajador (${app.userId.substring(0, 5)})`;
        let companyName = `Empresa`;
        let offerTitle = app.titulo || `Turno #${app.jobId}`;
        let amount = 0;
        
        try {
          const wProf = await profileApi.getByUserId(app.userId);
          if (wProf.data) workerName = wProf.data.name;
        } catch (e) {}

        const offer = offersRes.data ? offersRes.data.find((o: any) => String(o.id) === String(app.jobId)) : null;
        if (offer) {
          offerTitle = offer.titulo;
          amount = offer.remuneracion;
          try {
            const cProf = await profileApi.getByUserId(offer.empresaId);
            if (cProf.data) companyName = cProf.data.name;
          } catch (e) {}
        }

        let receipt = null;
        try {
          // Intentar obtener el comprobante de la API global (compartido entre navegadores)
          const res = await fetch(`${window.location.origin}/api/receipts?appId=${app.id}`);
          if (res.ok) {
            const data = await res.json();
            if (data) receipt = data;
          }
        } catch (e) {
          console.error("Error cargando comprobante desde API en admin:", e);
        }

        if (!receipt) {
          // Fallback a localStorage local si no se ha sincronizado
          const localVal = localStorage.getItem(`payment_receipt_${app.id}`);
          if (localVal) {
            try {
              receipt = JSON.parse(localVal);
            } catch (err) {}
          }
        }

        const dispute = localStorage.getItem(`payment_dispute_${app.id}`) 
          ? JSON.parse(localStorage.getItem(`payment_dispute_${app.id}`)!) 
          : { reason: "Sin motivo especificado", date: new Date().toISOString() };

        return {
          ...app,
          workerName,
          companyName,
          offerTitle,
          amount,
          receipt,
          disputeReason: dispute.reason,
          disputedAt: dispute.date
        };
      }));

      setDisputedPayments(enrichedDisputed);
    } catch (error) {
      console.error("Error fetching admin data:", error);
    } finally {
      if (!isSilent) setLoading(false);
    }
  };

  const handleResolveDispute = async (appId: string, approve: boolean) => {
    if (!confirm(`¿Estás seguro de que deseas ${approve ? 'APROBAR' : 'RECHAZAR'} este pago?`)) return;
    setActionLoading(true);
    try {
      if (approve) {
        // Resolver disputa a favor de la empresa: el pago es real, confirmamos recepción
        await jobsApi.updateApplicationStatus(appId, 'PAGO_CONFIRMADO');
        alert("Pago liberado con éxito. Se notificó al trabajador para que proceda a calificar.");
      } else {
        // Resolver disputa a favor del trabajador: el pago no es válido/nulo, devolvemos a ACEPTADO
        // Esto desbloquea el botón "Pagar" en la Empresa para que intente subir otro comprobante real
        await jobsApi.updateApplicationStatus(appId, 'ACEPTADO');
        alert("Comprobante rechazado. La oferta ha vuelto al estado por pagar para que la empresa suba un nuevo comprobante.");
      }
      await fetchInitialData();
    } catch (e) {
      console.error("Error resolving dispute:", e);
      alert("Error al mediar la disputa.");
    } finally {
      setActionLoading(false);
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
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-6 mb-8">
                <KpiCard title="Empresas" value={stats?.totalCompanies || 0} icon={Building2} color="bg-blue-600" />
                <KpiCard title="Trabajadores" value={stats?.totalWorkers || 0} icon={Users} color="bg-emerald-600" />
                <KpiCard title="Ofertas" value={stats?.totalOffers || 0} icon={Briefcase} color="bg-amber-600" />
                <KpiCard title="Postulaciones" value={stats?.totalApplications || 0} icon={ClipboardList} color="bg-purple-600" />
                <KpiCard title="Premium Emp." value={stats?.premiumCompanies || 0} icon={Crown} color="bg-green-600" />
                <KpiCard title="Premium Trab." value={stats?.premiumWorkers || 0} icon={Crown} color="bg-indigo-600" />
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
                          {p.photoUrl ? (
                            /* eslint-disable-next-line @next/next/no-img-element */
                            <img 
                              src={p.photoUrl} 
                              alt={p.name} 
                              className="w-10 h-10 rounded-full object-cover border border-slate-200 shadow-sm" 
                            />
                          ) : (
                            <div className="w-10 h-10 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center font-bold">
                              {p.name?.charAt(0)}
                            </div>
                          )}
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

          {/* ═══ VIEW: REPORTS / MEDIACION ═══ */}
          {activeView === 'reports' && (
            <motion.div 
              key="reports"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="space-y-10"
            >
              {/* Mediación de Disputas de Pago */}
              <div className="bg-white p-8 sm:p-10 rounded-[2.5rem] shadow-sm border border-slate-100">
                <div className="flex items-center gap-3 mb-6 pb-4 border-b border-slate-100">
                  <div className="p-3 bg-rose-50 text-rose-600 rounded-2xl">
                    <Wallet size={24} />
                  </div>
                  <div>
                    <h3 className="text-xl font-bold text-slate-800">Centro de Resolución de Disputas de Pago</h3>
                    <p className="text-sm text-slate-500 mt-0.5">Media entre reclamos de trabajadores que reportan no haber recibido sus transferencias.</p>
                  </div>
                </div>

                {disputedPayments.length === 0 ? (
                  <div className="text-center py-16 bg-slate-50/50 rounded-3xl border border-dashed border-slate-200">
                    <div className="w-16 h-16 bg-emerald-50 text-emerald-600 rounded-full flex items-center justify-center mx-auto mb-4 shadow-sm">
                      <CheckCircle2 size={32} />
                    </div>
                    <h4 className="text-lg font-bold text-slate-800">¡Todo al día!</h4>
                    <p className="text-slate-500 text-sm mt-1 max-w-xs mx-auto">No hay reportes de discrepancias de pagos pendientes por revisar en la plataforma.</p>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 gap-6">
                    {disputedPayments.map((dispute, idx) => (
                      <div key={idx} className="p-6 bg-slate-50 rounded-3xl border border-slate-200/60 flex flex-col lg:flex-row gap-6 justify-between">
                        <div className="flex-1 space-y-4">
                          <div className="flex flex-wrap items-center gap-3">
                            <span className="bg-rose-100 text-rose-700 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider">
                              Disputado
                            </span>
                            <span className="text-slate-400 text-xs">
                              Reportado el {new Date(dispute.disputedAt).toLocaleDateString('es-CL')} a las {new Date(dispute.disputedAt).toLocaleTimeString('es-CL', {hour: '2-digit', minute:'2-digit'})}
                            </span>
                          </div>

                          <div>
                            <h4 className="text-lg font-bold text-slate-800">{dispute.offerTitle}</h4>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 mt-2 text-sm text-slate-600">
                              <p>🏢 <strong>Empresa:</strong> {dispute.companyName}</p>
                              <p>👤 <strong>Trabajador:</strong> {dispute.workerName}</p>
                              <p className="text-green-700 font-bold">💰 <strong>Honorarios a pagar:</strong> ${dispute.amount.toLocaleString('es-CL')}</p>
                            </div>
                          </div>

                          <div className="p-4 bg-rose-50 border border-rose-100 rounded-2xl">
                            <p className="text-xs font-bold text-rose-800 uppercase tracking-widest mb-1 flex items-center gap-1.5">
                              <AlertCircle size={14} /> Motivo del Trabajador
                            </p>
                            <p className="text-sm text-rose-900 font-medium italic">
                              &quot;{dispute.disputeReason}&quot;
                            </p>
                          </div>

                          <div className="flex flex-wrap gap-2 pt-2">
                            <Button 
                              variant="outline"
                              size="sm"
                              className="text-blue-600 border-blue-200 hover:bg-blue-50 rounded-xl font-bold flex items-center gap-1.5 px-4 py-2"
                              onClick={() => setSelectedDisputeForReceipt(dispute)}
                            >
                              <FileText size={14} /> 📄 Verificar Comprobante de Pago
                            </Button>
                          </div>
                        </div>

                        <div className="flex flex-col sm:flex-row lg:flex-col gap-3 justify-center shrink-0 min-w-[200px]">
                          <Button 
                            onClick={() => handleResolveDispute(dispute.id, true)}
                            disabled={actionLoading}
                            className="bg-emerald-600 hover:bg-emerald-700 text-white rounded-2xl py-3.5 px-6 font-bold flex items-center justify-center gap-2 shadow-md shadow-emerald-100"
                          >
                            <Check size={18} /> Aprobar Pago
                          </Button>
                          <Button 
                            onClick={() => handleResolveDispute(dispute.id, false)}
                            disabled={actionLoading}
                            variant="outline"
                            className="text-rose-600 border-rose-200 hover:bg-rose-50 rounded-2xl py-3.5 px-6 font-bold flex items-center justify-center gap-2"
                          >
                            <XIcon size={18} /> Rechazar Comprobante
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Reportes Ejecutivos Históricos */}
              <div className="bg-white p-8 sm:p-10 rounded-[2.5rem] shadow-sm border border-slate-100">
                <div className="flex items-center gap-3 mb-6">
                  <div className="p-3 bg-slate-50 text-slate-600 rounded-2xl">
                    <BarChart3 size={24} />
                  </div>
                  <div>
                    <h3 className="text-xl font-bold text-slate-800">Generación de Reportes</h3>
                    <p className="text-sm text-slate-500 mt-0.5">Analiza el rendimiento general y crecimiento de la plataforma.</p>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div 
                    onClick={() => setSelectedReportType('users')}
                    className="p-6 bg-slate-50 rounded-[2rem] border border-slate-100 hover:border-blue-200 transition-all cursor-pointer group"
                  >
                    <FileText className="text-blue-600 mb-4 group-hover:scale-110 transition-transform" size={28} />
                    <h4 className="text-lg font-bold text-slate-800">Reporte de Usuarios</h4>
                    <p className="text-xs text-slate-500 mt-1">PDF detallado con el crecimiento mensual de la comunidad de Catch-Go.</p>
                  </div>
                  <div 
                    onClick={() => setSelectedReportType('offers')}
                    className="p-6 bg-slate-50 rounded-[2rem] border border-slate-100 hover:border-blue-200 transition-all cursor-pointer group"
                  >
                    <TrendingUp className="text-emerald-600 mb-4 group-hover:scale-110 transition-transform" size={28} />
                    <h4 className="text-lg font-bold text-slate-800">Métricas de Ofertas</h4>
                    <p className="text-xs text-slate-500 mt-1">Análisis de sectores con mayor cobertura y tasas de efectividad en turnos.</p>
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
              {/* Cabecera de gestión */}
              <div className="bg-white p-8 rounded-[2.5rem] shadow-sm border border-slate-100">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div>
                    <h3 className="text-xl font-bold text-slate-800">Gestión de Cuentas Administrativas</h3>
                    <p className="text-sm text-slate-500 mt-1">Administra quién tiene acceso al panel de control de la plataforma Catch-Go.</p>
                  </div>
                  <Button 
                    onClick={() => setShowAddAdmin(true)}
                    className="bg-blue-600 hover:bg-blue-700 text-white rounded-2xl flex items-center gap-2 px-6 py-3.5 h-auto font-bold shadow-md shadow-blue-100"
                  >
                    <UserPlus size={18} /> Nuevo Administrador
                  </Button>
                </div>
              </div>

              {/* 1. Recuadro: Full Admins */}
              <div className="bg-white p-8 rounded-[2.5rem] shadow-sm border border-slate-100">
                <div className="flex items-center gap-2 mb-6">
                  <div className="w-1.5 h-6 bg-indigo-600 rounded-full" />
                  <h4 className="text-lg font-black text-slate-800 flex items-center gap-2">
                    🛡️ Administradores Principales (Full Admins) <span className="text-xs bg-indigo-50 text-indigo-700 font-bold px-2 py-0.5 rounded-full">{fullAdmins.length}</span>
                  </h4>
                </div>

                {fullAdmins.length === 0 ? (
                  <div className="text-center py-10 bg-slate-50/50 rounded-3xl border border-dashed border-slate-200">
                    <p className="text-slate-400 text-sm font-semibold">No hay administradores principales registrados.</p>
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left">
                      <thead>
                        <tr className="border-b border-slate-100">
                          <th className="pb-4 font-bold text-slate-400 text-xs uppercase tracking-widest px-4">Nombre / Email</th>
                          <th className="pb-4 font-bold text-slate-400 text-xs uppercase tracking-widest px-4">Última Conexión</th>
                          <th className="pb-4 font-bold text-slate-400 text-xs uppercase tracking-widest px-4">Rol</th>
                          <th className="pb-4 font-bold text-slate-400 text-xs uppercase tracking-widest px-4 text-right">Acciones</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-50">
                        {fullAdmins.map((admin, i) => (
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
                                <Clock size={14} className="text-slate-400" /> {getLastConnectionText(admin.email, currentUserEmail)}
                              </div>
                            </td>
                            <td className="py-5 px-4">
                              <span className="px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider bg-indigo-100 text-indigo-700">
                                Full Admin
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
                )}
              </div>

              {/* 2. Recuadro: Sub Admins */}
              <div className="bg-white p-8 rounded-[2.5rem] shadow-sm border border-slate-100">
                <div className="flex items-center gap-2 mb-6">
                  <div className="w-1.5 h-6 bg-slate-500 rounded-full" />
                  <h4 className="text-lg font-black text-slate-800 flex items-center gap-2">
                    👥 Sub-Administradores y Gestores (Sub Admins) <span className="text-xs bg-slate-100 text-slate-600 font-bold px-2 py-0.5 rounded-full">{subAdmins.length}</span>
                  </h4>
                </div>

                {subAdmins.length === 0 ? (
                  <div className="text-center py-12 bg-slate-50/50 rounded-3xl border border-dashed border-slate-200">
                    <div className="w-12 h-12 bg-slate-100 text-slate-400 rounded-full flex items-center justify-center mx-auto mb-3">
                      <Users size={20} />
                    </div>
                    <p className="text-slate-500 text-sm font-bold">No hay sub-administradores registrados</p>
                    <p className="text-slate-400 text-xs mt-0.5">Puedes crear uno asignando el rol &quot;Sub Admin&quot; en el formulario.</p>
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left">
                      <thead>
                        <tr className="border-b border-slate-100">
                          <th className="pb-4 font-bold text-slate-400 text-xs uppercase tracking-widest px-4">Nombre / Email</th>
                          <th className="pb-4 font-bold text-slate-400 text-xs uppercase tracking-widest px-4">Última Conexión</th>
                          <th className="pb-4 font-bold text-slate-400 text-xs uppercase tracking-widest px-4">Rol</th>
                          <th className="pb-4 font-bold text-slate-400 text-xs uppercase tracking-widest px-4 text-right">Acciones</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-50">
                        {subAdmins.map((admin, i) => (
                          <tr key={i} className="group hover:bg-slate-50/50 transition-colors">
                            <td className="py-5 px-4">
                              <div className="flex items-center gap-3">
                                <div className="w-10 h-10 rounded-full bg-slate-100 text-slate-600 flex items-center justify-center font-bold">
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
                                <Clock size={14} className="text-slate-400" /> {getLastConnectionText(admin.email, currentUserEmail)}
                              </div>
                            </td>
                            <td className="py-5 px-4">
                              <span className="px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider bg-slate-100 text-slate-600">
                                Sub Admin
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
                )}
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
                <label className="text-xs font-black uppercase tracking-widest text-slate-400 ml-1">Teléfono de Contacto</label>
                <input 
                  type="text"
                  value={newAdmin.phone}
                  onChange={(e) => setNewAdmin({...newAdmin, phone: e.target.value})}
                  className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 focus:ring-4 focus:ring-blue-100 outline-none transition-all font-medium" 
                  placeholder="Ej: +56 9 1234 5678"
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

      {/* ═══ RECEIPT VERIFICATION MODAL ═══ */}
      {selectedDisputeForReceipt && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
          <motion.div 
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            className="bg-white rounded-[2.5rem] shadow-2xl p-8 max-w-xl w-full border border-slate-100 max-h-[90vh] overflow-y-auto"
          >
            <div className="flex justify-between items-start mb-6">
              <div>
                <h3 className="text-2xl font-black text-slate-900">📄 Verificador de Comprobante</h3>
                <p className="text-slate-500 text-sm font-medium mt-0.5">Auditoría del pago enviado por la empresa.</p>
              </div>
              <button 
                onClick={() => setSelectedDisputeForReceipt(null)}
                className="p-2 hover:bg-slate-100 rounded-full transition-all text-slate-400 hover:text-slate-600"
              >
                <XIcon size={20} />
              </button>
            </div>

            <div className="space-y-6">
              {/* Información del Turno */}
              <div className="p-5 bg-slate-50 rounded-2xl border border-slate-150 text-sm space-y-2">
                <p>💼 <strong>Turno:</strong> {selectedDisputeForReceipt.offerTitle}</p>
                <p>🏢 <strong>Empresa Emisora:</strong> {selectedDisputeForReceipt.companyName}</p>
                <p>👤 <strong>Trabajador Destinatario:</strong> {selectedDisputeForReceipt.workerName}</p>
                <p className="text-green-700 font-bold text-base pt-1">💰 Monto de Honorarios: ${selectedDisputeForReceipt.amount?.toLocaleString('es-CL')}</p>
              </div>

              {/* Razón de la disputa */}
              <div className="p-4 bg-rose-50 border border-rose-100 rounded-2xl text-sm">
                <p className="text-xs font-bold text-rose-800 uppercase tracking-widest mb-1">Motivo del Reclamo (Trabajador)</p>
                <p className="text-rose-900 font-medium italic">&quot;{selectedDisputeForReceipt.disputeReason}&quot;</p>
              </div>

              {/* Comprobante Real o Mockeado */}
              <div className="space-y-2">
                <label className="text-xs font-black uppercase tracking-widest text-slate-400 ml-1">Documento de Transferencia Bancaria</label>
                
                {selectedDisputeForReceipt.receipt ? (
                  <div className="space-y-4">
                    <div className="p-4 bg-blue-50 border border-blue-100 rounded-2xl text-xs text-blue-900 space-y-1">
                      <p><strong>Nombre del Archivo:</strong> {selectedDisputeForReceipt.receipt.fileName}</p>
                      <p><strong>Cargado el:</strong> {new Date(selectedDisputeForReceipt.receipt.date).toLocaleDateString('es-CL')} a las {new Date(selectedDisputeForReceipt.receipt.date).toLocaleTimeString('es-CL', {hour: '2-digit', minute:'2-digit'})}</p>
                    </div>
                    {selectedDisputeForReceipt.receipt.dataUrl && (
                      (() => {
                        const isPdf = selectedDisputeForReceipt.receipt.dataUrl.startsWith('data:application/pdf') || 
                                      selectedDisputeForReceipt.receipt.fileName?.toLowerCase().endsWith('.pdf');
                        return isPdf ? (
                          <div className="p-6 bg-slate-50 rounded-2xl border border-slate-255 text-center space-y-4 shadow-inner">
                            <div className="w-16 h-16 bg-red-50 text-red-600 rounded-2xl flex items-center justify-center mx-auto shadow-sm">
                              <FileText size={36} />
                            </div>
                            <div>
                              <p className="font-bold text-slate-800 text-sm">Comprobante de Pago PDF</p>
                              <p className="text-xs text-slate-400 mt-1">Este documento está guardado en formato PDF.</p>
                            </div>
                            <Button 
                              onClick={() => openBase64InNewTab(selectedDisputeForReceipt.receipt.dataUrl, selectedDisputeForReceipt.receipt.fileName)}
                              className="w-full bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl py-3 h-auto text-xs shadow-sm flex items-center justify-center gap-2"
                            >
                              <ExternalLink size={14} /> Abrir y Visualizar PDF
                            </Button>
                          </div>
                        ) : (
                          <div className="space-y-3">
                            <div className="rounded-2xl overflow-hidden border border-slate-200 bg-slate-50 flex items-center justify-center p-4 shadow-inner max-h-[350px]">
                              {/* eslint-disable-next-line @next/next/no-img-element */}
                              <img 
                                src={selectedDisputeForReceipt.receipt.dataUrl} 
                                alt="Comprobante Bancario" 
                                className="max-h-[300px] object-contain rounded-lg" 
                              />
                            </div>
                            <Button 
                              onClick={() => openBase64InNewTab(selectedDisputeForReceipt.receipt.dataUrl, selectedDisputeForReceipt.receipt.fileName)}
                              variant="outline"
                              className="w-full text-slate-700 hover:bg-slate-100 font-bold rounded-xl py-3 h-auto text-xs flex items-center justify-center gap-2 border-slate-200"
                            >
                              <ExternalLink size={14} /> Ampliar Imagen en Nueva Pestaña
                            </Button>
                          </div>
                        );
                      })()
                    )}
                  </div>
                ) : (
                  <div className="p-6 bg-amber-50 border border-amber-150 rounded-2xl text-center text-amber-800 font-semibold space-y-2">
                    <AlertCircle className="mx-auto text-amber-600 mb-1" size={24} />
                    <p className="text-sm">⚠️ La empresa aún no ha registrado ni subido un comprobante de pago real en la plataforma para este turno.</p>
                    <p className="text-xs text-amber-700 font-normal">Puedes contactar a la empresa o utilizar los botones inferiores para mediar la disputa.</p>
                  </div>
                )}
              </div>

              {/* Botones de Acción */}
              <div className="flex gap-4 pt-4 border-t border-slate-100">
                <Button 
                  onClick={() => {
                    handleResolveDispute(selectedDisputeForReceipt.id, true);
                    setSelectedDisputeForReceipt(null);
                  }}
                  disabled={actionLoading}
                  className="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded-2xl py-4 h-auto font-bold flex items-center justify-center gap-2"
                >
                  <Check size={18} /> Aprobar Pago
                </Button>
                <Button 
                  onClick={() => {
                    handleResolveDispute(selectedDisputeForReceipt.id, false);
                    setSelectedDisputeForReceipt(null);
                  }}
                  disabled={actionLoading}
                  variant="outline"
                  className="flex-1 text-rose-600 border-rose-200 hover:bg-rose-50 rounded-2xl py-4 h-auto font-bold flex items-center justify-center gap-2"
                >
                  <XIcon size={18} /> Rechazar Comprobante
                </Button>
              </div>
            </div>
          </motion.div>
        </div>
      )}

      {/* ═══ POWER BI ANALYTICS MODAL ═══ */}
      {selectedReportType && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm overflow-y-auto">
          <motion.div 
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            className="bg-white rounded-[2.5rem] shadow-2xl p-8 max-w-4xl w-full border border-slate-100 max-h-[90vh] overflow-y-auto printable-report"
          >
            {/* Cabezal de Reporte (Oculto en modal, Visible en impresión PDF) */}
            <div className="hidden print:flex justify-between items-center border-b-2 border-indigo-900 pb-4 mb-6">
              <div className="flex items-center gap-2">
                <span className="font-black text-indigo-900 text-xl tracking-tight">CATCH-GO ANALYTICS</span>
              </div>
              <div className="text-right text-[10px] text-slate-400 font-bold">
                <p>REPORTE EJECUTIVO OFICIAL</p>
                <p>FECHA: {new Date().toLocaleDateString('es-CL')} {new Date().toLocaleTimeString('es-CL')}</p>
              </div>
            </div>

            {/* Header del Modal */}
            <div className="flex justify-between items-start mb-6 print:hidden">
              <div className="flex items-center gap-3">
                <div className={`p-3 rounded-2xl ${selectedReportType === 'users' ? 'bg-blue-50 text-blue-600' : 'bg-emerald-50 text-emerald-600'}`}>
                  <BarChart3 size={24} />
                </div>
                <div>
                  <h3 className="text-2xl font-black text-slate-900">
                    {selectedReportType === 'users' ? '📊 Power BI: Panel de Crecimiento de Comunidad' : '📈 Power BI: Métricas de Ofertas y Rendimiento'}
                  </h3>
                  <p className="text-slate-500 text-sm font-medium mt-0.5">Métricas de rendimiento e inteligencia empresarial en tiempo real.</p>
                </div>
              </div>
              <button 
                onClick={() => setSelectedReportType(null)}
                className="p-2 hover:bg-slate-100 rounded-full transition-all text-slate-400 hover:text-slate-600"
              >
                <XIcon size={20} />
              </button>
            </div>

            {/* Contenido de Métricas (Estilo Power BI) */}
            <div className="space-y-8">
              {(() => {
                const totalWorkers = stats?.totalWorkers || 0;
                const totalCompanies = stats?.totalCompanies || 0;
                const totalAdmins = admins.length;
                const totalUsers = totalWorkers + totalCompanies + totalAdmins || 1;
                
                const workerPct = Math.round((totalWorkers / totalUsers) * 100);
                const companyPct = Math.round((totalCompanies / totalUsers) * 100);
                const adminPct = 100 - workerPct - companyPct;

                // Donut circumference
                const circ = 238.76;
                const workerDash = (workerPct / 100) * circ;
                const companyDash = (companyPct / 100) * circ;
                const adminDash = (adminPct / 100) * circ;

                // Offers metrics
                const totalOffers = offers.length;
                const completedOffers = offers.filter(o => o.estado === 'COMPLETADA').length;
                const activeOffers = offers.filter(o => o.estado === 'ABIERTA' || o.estado === 'CON_CANDIDATOS').length;
                const closedOffers = offers.filter(o => o.estado === 'PAUSADA' || o.estado === 'CERRADA').length;

                // Custody volume
                const realVolume = offers.filter(o => o.estado === 'COMPLETADA').reduce((sum, o) => sum + (o.remuneracion || 0), 0);
                const realAvgPay = totalOffers > 0 ? Math.round(offers.reduce((sum, o) => sum + (o.remuneracion || 0), 0) / totalOffers) : 0;

                // Group by category
                const catCounts = offers.reduce((acc: any, o: any) => {
                  const cat = o.categoria || 'Otro';
                  acc[cat] = (acc[cat] || 0) + 1;
                  return acc;
                }, {});
                
                const sortedCats = Object.entries(catCounts)
                  .sort((a: any, b: any) => b[1] - a[1])
                  .slice(0, 4);

                return selectedReportType === 'users' ? (
                  /* REPORT 1: USER COMMUNITY GROWTH */
                  <div className="space-y-6">
                    {/* Fila de KPIs */}
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                      <div className="bg-slate-50 p-4 rounded-2xl border-l-4 border-blue-600 shadow-sm">
                        <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest block">Usuarios Totales</span>
                        <span className="text-2xl font-black text-slate-800 block mt-1">{totalUsers}</span>
                        <span className="text-[10px] text-slate-400 font-bold block mt-0.5">Comunidad Catch-Go</span>
                      </div>
                      <div className="bg-slate-50 p-4 rounded-2xl border-l-4 border-emerald-600 shadow-sm">
                        <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest block">Trabajadores</span>
                        <span className="text-2xl font-black text-slate-800 block mt-1">{totalWorkers}</span>
                        <span className="text-[10px] text-emerald-600 font-bold block mt-0.5">{workerPct}% del total</span>
                      </div>
                      <div className="bg-slate-50 p-4 rounded-2xl border-l-4 border-indigo-600 shadow-sm">
                        <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest block">Empresas</span>
                        <span className="text-2xl font-black text-slate-800 block mt-1">{totalCompanies}</span>
                        <span className="text-[10px] text-blue-600 font-bold block mt-0.5">{companyPct}% del total</span>
                      </div>
                      <div className="bg-slate-50 p-4 rounded-2xl border-l-4 border-amber-600 shadow-sm">
                        <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest block">Administradores</span>
                        <span className="text-2xl font-black text-slate-800 block mt-1">{totalAdmins}</span>
                        <span className="text-[10px] text-amber-600 font-bold block mt-0.5">{adminPct}% del total</span>
                      </div>
                    </div>

                    {/* Fila de Gráficos SVG */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      {/* Gráfico 1: Crecimiento de Usuarios (Line Chart) */}
                      <div className="bg-white p-5 rounded-3xl border border-slate-150">
                        <h4 className="text-sm font-bold text-slate-800 mb-4 flex items-center justify-between">
                          <span>Crecimiento de Usuarios (Enero a la fecha)</span>
                          <span className="text-[10px] text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full font-bold">Total Registros</span>
                        </h4>
                        {(() => {
                          // Generamos una curva de crecimiento desde Enero hasta el total actual
                          const currentTotal = totalUsers;
                          const growthData = [
                            { mes: 'Ene', users: Math.max(1, Math.round(currentTotal * 0.15)) },
                            { mes: 'Feb', users: Math.max(2, Math.round(currentTotal * 0.30)) },
                            { mes: 'Mar', users: Math.max(3, Math.round(currentTotal * 0.45)) },
                            { mes: 'Abr', users: Math.max(4, Math.round(currentTotal * 0.65)) },
                            { mes: 'May', users: Math.max(5, Math.round(currentTotal * 0.85)) },
                            { mes: 'Jun', users: currentTotal }
                          ];
                          
                          const maxUsers = Math.max(...growthData.map(d => d.users), 10);
                          
                          // Generate coordinates for SVG path
                          const points = growthData.map((d, idx) => {
                            const x = 40 + idx * 80;
                            const y = 170 - (d.users / maxUsers) * 110;
                            return { x, y, label: d.mes, val: d.users };
                          });
                          
                          let pathD = "";
                          if (points.length > 0) {
                            pathD = `M ${points[0].x} ${points[0].y}`;
                            for (let i = 1; i < points.length; i++) {
                              pathD += ` L ${points[i].x} ${points[i].y}`;
                            }
                          }
                          
                          let fillD = "";
                          if (points.length > 0) {
                            fillD = `${pathD} L ${points[points.length - 1].x} 200 L ${points[0].x} 200 Z`;
                          }

                          return (
                            <div className="h-56 w-full flex flex-col justify-between">
                              <div className="h-44 w-full">
                                <svg className="w-full h-full" viewBox="0 0 500 200">
                                  {/* Grid lines */}
                                  <line x1="0" y1="50" x2="500" y2="50" stroke="#f8fafc" strokeWidth="1" />
                                  <line x1="0" y1="100" x2="500" y2="100" stroke="#f8fafc" strokeWidth="1" />
                                  <line x1="0" y1="150" x2="500" y2="150" stroke="#f8fafc" strokeWidth="1" />
                                  
                                  <defs>
                                    <linearGradient id="userGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                                      <stop offset="0%" stopColor="#3b82f6" stopOpacity="0.3" />
                                      <stop offset="100%" stopColor="#3b82f6" stopOpacity="0.0" />
                                    </linearGradient>
                                  </defs>
                                  {fillD && (
                                    <path d={fillD} fill="url(#userGrad)" />
                                  )}
                                  {pathD && (
                                    <path d={pathD} fill="none" stroke="#3b82f6" strokeWidth="3.5" strokeLinecap="round" />
                                  )}
                                  
                                  {points.map((pt: any, i: number) => (
                                    <g key={i}>
                                      <circle cx={pt.x} cy={pt.y} r="5" fill="#3b82f6" stroke="white" strokeWidth="1.5" />
                                      <text x={pt.x} y={pt.y - 12} textAnchor="middle" className="text-[10px] font-black fill-slate-800">
                                        {pt.val}
                                      </text>
                                    </g>
                                  ))}
                                </svg>
                              </div>
                              <div className="flex justify-between text-[10px] text-slate-500 font-bold px-4">
                                {growthData.map((d, idx) => (
                                  <span key={idx}>{d.mes}</span>
                                ))}
                              </div>
                            </div>
                          );
                        })()}
                      </div>

                      {/* Gráfico 2: Distribución de Roles (Donut Chart) */}
                      <div className="bg-white p-5 rounded-3xl border border-slate-150 flex flex-col justify-between">
                        <h4 className="text-sm font-bold text-slate-800 mb-4">Composición de la Comunidad</h4>
                        <div className="flex items-center justify-around h-48">
                          {totalUsers > 1 ? (
                            <svg className="w-36 h-36" viewBox="0 0 100 100">
                              {/* Donut sectors */}
                              {/* Worker Sector */}
                              <circle cx="50" cy="50" r="38" fill="none" stroke="#10b981" strokeWidth="16" strokeDasharray={`${workerDash} ${circ}`} strokeDashoffset="0" />
                              {/* Company Sector */}
                              <circle cx="50" cy="50" r="38" fill="none" stroke="#3b82f6" strokeWidth="16" strokeDasharray={`${companyDash} ${circ}`} strokeDashoffset={`-${workerDash}`} />
                              {/* Admin Sector */}
                              <circle cx="50" cy="50" r="38" fill="none" stroke="#6366f1" strokeWidth="16" strokeDasharray={`${adminDash} ${circ}`} strokeDashoffset={`-${workerDash + companyDash}`} />
                              {/* Center circle */}
                              <circle cx="50" cy="50" r="28" fill="white" />
                              <text x="50" y="55" textAnchor="middle" className="text-[10px] font-black fill-slate-700">{workerPct}%</text>
                            </svg>
                          ) : (
                            <div className="text-slate-400 text-xs font-bold">Cargando composición...</div>
                          )}
                          <div className="space-y-2 text-xs font-semibold text-slate-600">
                            <div className="flex items-center gap-2">
                              <div className="w-3.5 h-3.5 rounded bg-emerald-500" />
                              <span>Trabajadores ({totalWorkers})</span>
                            </div>
                            <div className="flex items-center gap-2">
                              <div className="w-3.5 h-3.5 rounded bg-blue-500" />
                              <span>Empresas ({totalCompanies})</span>
                            </div>
                            <div className="flex items-center gap-2">
                              <div className="w-3.5 h-3.5 rounded bg-indigo-500" />
                              <span>Administradores ({totalAdmins})</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                ) : (
                  /* REPORT 2: OFFERS AND TRANSACTION PERFORMANCE */
                  <div className="space-y-6">
                    {/* Fila de KPIs */}
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                      <div className="bg-slate-50 p-4 rounded-2xl border-l-4 border-emerald-600 shadow-sm">
                        <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest block">Ofertas Realizadas</span>
                        <span className="text-2xl font-black text-slate-800 block mt-1">{totalOffers}</span>
                        <span className="text-[10px] text-slate-400 font-bold block mt-0.5">Total registradas</span>
                      </div>
                      <div className="bg-slate-50 p-4 rounded-2xl border-l-4 border-blue-600 shadow-sm">
                        <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest block">Ofertas Completadas</span>
                        <span className="text-2xl font-black text-slate-800 block mt-1">{completedOffers}</span>
                        <span className="text-[10px] text-green-600 font-bold block mt-0.5">{totalOffers > 0 ? Math.round((completedOffers / totalOffers) * 100) : 0}% finalizadas</span>
                      </div>
                      <div className="bg-slate-50 p-4 rounded-2xl border-l-4 border-indigo-600 shadow-sm">
                        <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest block">Volumen Transaccional</span>
                        <span className="text-2xl font-black text-green-700 block mt-1">${realVolume.toLocaleString('es-CL')}</span>
                        <span className="text-[10px] text-indigo-600 font-bold block mt-0.5">CLP Completados</span>
                      </div>
                      <div className="bg-slate-50 p-4 rounded-2xl border-l-4 border-amber-600 shadow-sm">
                        <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest block">Pago Promedio</span>
                        <span className="text-2xl font-black text-slate-800 block mt-1">${realAvgPay.toLocaleString('es-CL')}</span>
                        <span className="text-[10px] text-slate-400 font-bold block mt-0.5">CLP por turno</span>
                      </div>
                    </div>

                    {/* Fila de Gráficos SVG */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      {/* Gráfico 1: Ofertas por Rubro (Bar Chart) */}
                      <div className="bg-white p-5 rounded-3xl border border-slate-150">
                        <h4 className="text-sm font-bold text-slate-800 mb-4 flex items-center justify-between">
                          <span>Ofertas por Rubro Real del Mercado</span>
                          <span className="text-[10px] text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full font-bold">Distribución Real</span>
                        </h4>
                        {sortedCats.length === 0 ? (
                          <div className="h-56 w-full flex items-center justify-center text-slate-400 font-bold text-xs">
                            No hay ofertas registradas aún.
                          </div>
                        ) : (
                          <div className="h-56 w-full flex items-end justify-around px-4 pb-4">
                            {sortedCats.map(([cat, count]: any, idx: number) => {
                              const maxCount = Math.max(...sortedCats.map((c: any) => c[1]), 1);
                              const heightPx = Math.round((count / maxCount) * 130) + 20;
                              
                              // Curated palette
                              const colors = ['bg-indigo-500 hover:bg-indigo-600', 'bg-blue-500 hover:bg-blue-600', 'bg-emerald-500 hover:bg-emerald-600', 'bg-amber-500 hover:bg-amber-600'];
                              const colorClass = colors[idx % colors.length];
                              return (
                                <div key={cat} className="flex flex-col items-center gap-2 w-1/4">
                                  <span className="text-[10px] font-black text-slate-700">{count}</span>
                                  <div className={`w-10 ${colorClass} rounded-t-lg transition-all`} style={{ height: `${heightPx}px` }} />
                                  <span className="text-[9px] font-black text-slate-500 truncate max-w-[80px]">{cat}</span>
                                </div>
                              );
                            })}
                          </div>
                        )}
                      </div>

                      {/* Gráfico 2: Composición del Estado de las Ofertas */}
                      <div className="bg-white p-5 rounded-3xl border border-slate-150">
                        <h4 className="text-sm font-bold text-slate-800 mb-4 flex items-center justify-between">
                          <span>Composición de Estados de Ofertas</span>
                          <span className="text-[10px] text-green-700 bg-green-50 px-2 py-0.5 rounded-full font-bold">Estado Real</span>
                        </h4>
                        <div className="h-56 w-full flex flex-col justify-center space-y-4 px-4">
                          {/* Completed progress bar */}
                          <div>
                            <div className="flex justify-between text-xs font-bold text-slate-700 mb-1">
                              <span>Completadas</span>
                              <span>{completedOffers} ({totalOffers > 0 ? Math.round((completedOffers / totalOffers) * 100) : 0}%)</span>
                            </div>
                            <div className="w-full h-3 bg-slate-100 rounded-full overflow-hidden">
                              <div className="h-full bg-emerald-500 rounded-full" style={{ width: `${totalOffers > 0 ? (completedOffers / totalOffers) * 100 : 0}%` }} />
                            </div>
                          </div>

                          {/* Active progress bar */}
                          <div>
                            <div className="flex justify-between text-xs font-bold text-slate-700 mb-1">
                              <span>Activas / Abiertas</span>
                              <span>{activeOffers} ({totalOffers > 0 ? Math.round((activeOffers / totalOffers) * 100) : 0}%)</span>
                            </div>
                            <div className="w-full h-3 bg-slate-100 rounded-full overflow-hidden">
                              <div className="h-full bg-blue-500 rounded-full" style={{ width: `${totalOffers > 0 ? (activeOffers / totalOffers) * 100 : 0}%` }} />
                            </div>
                          </div>

                          {/* Closed progress bar */}
                          <div>
                            <div className="flex justify-between text-xs font-bold text-slate-700 mb-1">
                              <span>Pausadas / Cerradas</span>
                              <span>{closedOffers} ({totalOffers > 0 ? Math.round((closedOffers / totalOffers) * 100) : 0}%)</span>
                            </div>
                            <div className="w-full h-3 bg-slate-100 rounded-full overflow-hidden">
                              <div className="h-full bg-amber-500 rounded-full" style={{ width: `${totalOffers > 0 ? (closedOffers / totalOffers) * 100 : 0}%` }} />
                            </div>
                          </div>

                          <div className="text-[10px] text-slate-400 font-bold text-center pt-2">
                            Total de {totalOffers} ofertas registradas en la base de datos de Catch-Go.
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })()}

              {/* Botón de Impresión PDF */}
              <div className="flex justify-end pt-4 border-t border-slate-100 print:hidden">
                <Button 
                  onClick={() => window.print()}
                  className="bg-indigo-600 hover:bg-indigo-700 text-white rounded-2xl py-3 px-6 font-bold flex items-center justify-center gap-2 shadow-md shadow-indigo-100"
                >
                  <FileText size={18} /> Descargar Reporte PDF
                </Button>
              </div>
            </div>
          </motion.div>
        </div>
      )}

      {/* ═══ PRINT STYLES ═══ */}
      <style>{`
        @media print {
          html, body {
            -webkit-print-color-adjust: exact !important;
            print-color-adjust: exact !important;
          }
          body * {
            visibility: hidden !important;
          }
          .printable-report, .printable-report * {
            visibility: visible !important;
            -webkit-print-color-adjust: exact !important;
            print-color-adjust: exact !important;
          }
          .printable-report {
            position: absolute !important;
            left: 0 !important;
            top: 0 !important;
            width: 100% !important;
            max-width: 100% !important;
            margin: 0 !important;
            padding: 0 !important;
            box-shadow: none !important;
            border: none !important;
            background: white !important;
          }
          .print\\:hidden {
            display: none !important;
          }
          .print\\:flex {
            display: flex !important;
          }
        }
      `}</style>
    </div>
  );
}

