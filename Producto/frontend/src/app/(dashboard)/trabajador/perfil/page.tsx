"use client";

import React, { useState, useEffect, useMemo } from 'react';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { User, Camera, Calendar, CreditCard, Save, Loader2, Briefcase, FileText, CheckCircle, Lock, Eye, RefreshCw, Clock, ArrowRight, Crown } from 'lucide-react';
import { profileApi, Profile } from '@/lib/api/profile';
import { jobsApi } from '@/lib/api/jobs';
import { createClient } from '@/lib/supabase/client';
import { uploadFile } from '@/lib/supabase/storage';
import { authApi } from '@/lib/api/auth';
import { Trash2, AlertTriangle, X, CheckSquare, Square } from 'lucide-react';
import LocationPicker from '@/components/maps/LocationPicker';
import { SkillsChart } from '@/components/features/SkillsChart';
import { useSettings } from '@/context/SettingsContext';

const SKILLS_WORKER_OPTS = {
  habilidades: ['Atención al cliente', 'Conducción', 'Manejo Excel', 'Inventario', 'Ventas', 'Programación', 'Diseño', 'Trabajo físico', 'Electricidad', 'Mecánica', 'Liderazgo', 'Logística'],
  ambiente: ['Trabajo en equipo', 'Trabajo individual', 'Trabajo en terreno', 'Oficina', 'Alta presión', 'Flexible'],
  caracteristica: ['Responsable', 'Rápido', 'Ordenado', 'Creativo', 'Líder', 'Comunicativo', 'Analítico'],
  preferencia: ['Part Time', 'Turnos', 'Freelance', 'Temporal', 'Fines de semana']
};

// REEMPLAZAR CON TU API KEY DE GOOGLE CLOUD
const GOOGLE_MAPS_API_KEY = process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || '';

const BANCOS_CHILE = [
  "Banco de Chile", "Banco Estado", "Banco Santander", "Banco BCI",
  "Scotiabank", "Banco Itaú", "Banco Bice", "Banco Security",
  "Banco Falabella", "Banco Ripley", "Banco Consorcio",
  "Banco Internacional", "Tenpo", "Mach"
];

// Tipos para postulaciones
interface Postulacion {
  id: string;
  titulo: string;
  empresa: string;
  estado: string;
  fecha: string;
}

const estadoColor: Record<string, string> = {
  PENDIENTE: 'bg-yellow-100 text-yellow-800',
  ACEPTADO: 'bg-green-100 text-green-800',
  RECHAZADO: 'bg-red-100 text-red-800',
};
const estadoLabel: Record<string, string> = {
  PENDIENTE: 'Pendiente',
  ACEPTADO: 'Aceptado',
  RECHAZADO: 'Rechazado',
};

export default function TrabajadorPerfilPage() {
  const router = useRouter();
  const { t } = useSettings();
  const [formData, setFormData] = useState<Profile>({
    userId: '', name: '', email: '', phone: '+56 ', birthDate: '',
    photoUrl: '',
    cvUrl: '',
    bankName: '',
    accountType: '',
    accountNumber: '',
    latitude: -33.4489,
    longitude: -70.6693,
    rut: '',
    address: '',
    certificateUrl: '',
    type: 'TRABAJADOR'
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState<string | null>(null);
  const [birthDateLocked, setBirthDateLocked] = useState(false);
  const [savedProfile, setSavedProfile] = useState<Profile | null>(null);
  const [postulaciones, setPostulaciones] = useState<Postulacion[]>([]);
  const [loadingPostulaciones, setLoadingPostulaciones] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletePassword, setDeletePassword] = useState('');
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  // Skills logic
  const skillsData = useMemo(() => {
    const defaultValue = {
      habilidades: [],
      ambiente: '',
      caracteristica: '',
      preferencia: ''
    };
    try {
      if (!formData.skills) return defaultValue;
      const parsed = JSON.parse(formData.skills);
      return {
        ...defaultValue,
        ...parsed,
        habilidades: Array.isArray(parsed.habilidades) ? parsed.habilidades : []
      };
    } catch (e) {
      return defaultValue;
    }
  }, [formData.skills]);

  const updateSkills = (key: string, value: any) => {
    const newSkills = { ...skillsData, [key]: value };
    setFormData({ ...formData, skills: JSON.stringify(newSkills) });
  };

  const chartData = [
    { label: 'Habilidades', value: (skillsData?.habilidades?.length || 0) / (SKILLS_WORKER_OPTS.habilidades.length || 1) },
    { label: 'Ambiente', value: skillsData.ambiente ? 1 : 0 },
    { label: 'Perfil', value: skillsData.caracteristica ? 1 : 0 },
    { label: 'Preferencia', value: skillsData.preferencia ? 1 : 0 },
  ];

  useEffect(() => {
    const fetchProfile = async () => {
      setLoading(true);
      let realUserId = '';
      try {
        let initialData = { name: '', email: '', phone: '+56 ', photo: '' };

        // 1. Intentar primero con localStorage (fuente de verdad de la sesión activa en el frontend)
        const storedUser = localStorage.getItem('user_info');
        if (storedUser) {
          try {
            const userData = JSON.parse(storedUser);
            realUserId = userData.id?.toString() || '';
            initialData = { 
              name: userData.nombre || userData.name || '', 
              email: userData.email || '', 
              phone: userData.telefono || userData.phone || '+56 ',
              photo: userData.foto || userData.photoUrl || ''
            };
          } catch (e) {
            console.error('Error al parsear user_info de localStorage:', e);
          }
        }

        // 2. Si no hay en localStorage, usar Supabase como fallback
        if (!realUserId) {
          const supabase = createClient();
          const { data: { user: supabaseUser } } = await supabase.auth.getUser();
          if (supabaseUser) {
            realUserId = supabaseUser.id;
            initialData = { 
              name: supabaseUser.user_metadata?.full_name || supabaseUser.user_metadata?.name || '', 
              email: supabaseUser.email || '', 
              phone: '+56 ',
              photo: supabaseUser.user_metadata?.avatar_url || ''
            };
          }
        }

        if (!realUserId) { 
          console.warn('No se encontró UserID para cargar perfil.');
          router.push('/login'); 
          return; 
        }

        console.log('Cargando perfil para ID:', realUserId);
        const res = await profileApi.getByUserId(realUserId);
        console.log('Respuesta del servidor:', res);

        if (res.data) {
          const p = {
            ...res.data,
            userId: realUserId,
            name: res.data.name || initialData.name || '',
            email: res.data.email || initialData.email || '',
            phone: res.data.phone || initialData.phone || '',
            photoUrl: res.data.photoUrl || initialData.photo || '',
            type: 'TRABAJADOR' as const,
            // Recuperar datos extendidos del campo skills (RUT, Dirección)
            ...(res.data.skills && res.data.skills.startsWith('{') ? JSON.parse(res.data.skills) : {})
          };
          setFormData(p);
          setSavedProfile(p);
          if (res.data.birthDate) setBirthDateLocked(true);
        } else {
          console.log('No se encontró perfil previo, usando datos iniciales.');
          setFormData(prev => ({ 
            ...prev, 
            userId: realUserId, 
            name: initialData.name, 
            email: initialData.email, 
            phone: initialData.phone,
            photoUrl: initialData.photo 
          }));
        }
      } catch (error) { console.error('Error fetching profile:', error); }
      finally { 
        setLoading(false); 
        
        // AUTO-DETECCION DE UBICACION (Solo si no hay coordenadas guardadas previamente)
        if (navigator.geolocation && !savedProfile?.latitude) {
          navigator.geolocation.getCurrentPosition(
            async (position) => {
              const { latitude, longitude } = position.coords;
              setFormData(prev => ({ ...prev, latitude, longitude }));
              
              // Detectar dirección inicial
              try {
                const response = await fetch(`https://maps.googleapis.com/maps/api/geocode/json?latlng=${latitude},${longitude}&key=${GOOGLE_MAPS_API_KEY}`);
                const data = await response.json();
                if (data.results && data.results[0]) {
                  setFormData(prev => ({ ...prev, address: data.results[0].formatted_address }));
                }
              } catch (e) {}
            },
            (error) => console.warn('Error detectando ubicación:', error.message),
            { enableHighAccuracy: true }
          );
        }
      }

      // Cargar postulaciones reales (independiente del perfil)
      if (realUserId) {
        setLoadingPostulaciones(true);
        try {
          const postRes = await jobsApi.getApplicationsByUserId(realUserId);
          if (postRes.data && Array.isArray(postRes.data)) {
            setPostulaciones(postRes.data.map((p: any) => ({
              id: p.id?.toString() || '',
              titulo: p.jobTitle || `Turno #${p.jobId}`,
              empresa: "Empresa Aliada",
              estado: p.estado || 'PENDIENTE',
              fecha: p.fechaPostulacion ? new Date(p.fechaPostulacion).toLocaleDateString('es-CL') : 'Hoy'
            })));
          }
        } catch (e) {
          console.error("Error cargando historial de postulaciones:", e);
          // No bloqueamos la UI, simplemente dejamos la lista vacía
        } finally {
          setLoadingPostulaciones(false);
        }
      }
    };
    fetchProfile();
  }, [router]);


  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    let { id, value } = e.target;
    if (id === 'phone') {
      if (!value.startsWith('+56 ')) value = '+56 ';
      const numbers = value.slice(4).replace(/\D/g, '').slice(0, 9);
      value = '+56 ' + numbers;
    }
    setFormData({ ...formData, [id]: value });
  };

  const handleSave = async () => {
    // VALIDACION DE CAMPOS OBLIGATORIOS
    const required = [
      { field: 'name', label: 'Nombre' },
      { field: 'rut', label: 'RUT' },
      { field: 'address', label: 'Dirección' },
      { field: 'phone', label: 'Teléfono' },
      { field: 'birthDate', label: 'Fecha de Nacimiento' },
      { field: 'bankName', label: 'Banco' },
      { field: 'accountType', label: 'Tipo de Cuenta' },
      { field: 'accountNumber', label: 'Número de Cuenta' }
    ];

    const missing = required.filter(r => !formData[r.field as keyof typeof formData]);
    if (missing.length > 0) {
      alert(`Los siguientes campos son obligatorios:\n- ${missing.map(m => m.label).join('\n- ')}`);
      return;
    }

    setSaving(true);
    try {
      // Persistir datos extendidos en skills
      const extendedData = {
        rut: formData.rut,
        address: formData.address,
        certificateUrl: formData.certificateUrl
      };

      const dataToSave = {
        ...formData,
        skills: JSON.stringify({
          ...(formData.skills && formData.skills.startsWith('{') ? JSON.parse(formData.skills) : {}),
          ...extendedData
        })
      };

      const res = await profileApi.save(dataToSave);
      if (!res.error) {
        // Actualizamos con los datos devueltos por el servidor (pueden tener IDs generados)
        const updatedData = res.data || formData;
        setFormData(prev => ({ ...prev, ...updatedData }));
        setSavedProfile(updatedData);
        
        if (updatedData.birthDate) setBirthDateLocked(true);
        alert('¡Perfil actualizado con éxito!');
      } else { 
        alert('Error al guardar: ' + (typeof res.error === 'string' ? res.error : JSON.stringify(res.error))); 
      }
    } catch (error) { 
      console.error('Save error:', error);
      alert('Error de conexión al guardar el perfil.'); 
    }
    finally { setSaving(false); }
  };

  const handleSearchAddress = async () => {
    if (!formData.address) return;
    setLoading(true);
    try {
      const response = await fetch(`https://maps.googleapis.com/maps/api/geocode/json?address=${encodeURIComponent(formData.address)}&key=${GOOGLE_MAPS_API_KEY}`);
      const data = await response.json();
      if (data.results && data.results[0]) {
        const { lat, lng } = data.results[0].geometry.location;
        setFormData(prev => ({ ...prev, latitude: lat, longitude: lng }));
      } else {
        alert("No se pudo encontrar esa dirección.");
      }
    } catch (e) {
      console.error("Error buscando dirección:", e);
    } finally {
      setLoading(false);
    }
  };

  const handlePhotoUpload = () => {
    const input = document.createElement('input');
    input.type = 'file'; input.accept = 'image/*';
    input.onchange = async (e: any) => {
      const file = e.target.files[0];
      if (file && formData.userId) {
        setUploading('photo');
        try {
          const url = await uploadFile(file, formData.userId);
          setFormData(prev => ({ ...prev, photoUrl: url }));
        } catch (error: any) { alert(error.message || 'Error al subir la foto.'); }
        finally { setUploading(null); }
      }
    };
    input.click();
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>, field: string) => {
    const file = e.target.files?.[0];
    if (file && formData.userId) {
      setUploading(field);
      try {
        const url = await uploadFile(file, formData.userId);
        setFormData(prev => ({ ...prev, [field]: url }));
      } catch (error: any) { alert(error.message || 'Error al subir el archivo.'); }
      finally { setUploading(null); }
    }
  };

  const handleDeleteAccount = async () => {
    if (!deletePassword) {
      setDeleteError('Por favor, ingresa tu contraseña para continuar.');
      return;
    }

    setDeleting(true);
    setDeleteError('');
    try {
      // 1. Verificar contraseña
      const verifyRes = await authApi.verifyPassword(formData.userId, deletePassword);
      if (verifyRes.data !== true) {
        setDeleteError('Contraseña incorrecta. Por favor, intenta de nuevo.');
        setDeleting(false);
        return;
      }

      // 2. Eliminar perfil
      await profileApi.deleteProfile(formData.userId);

      // 3. Eliminar cuenta
      const deleteRes = await authApi.deleteAccount(formData.userId);

      if (!deleteRes.error) {
        // 4. Limpiar sesión y redirigir
        localStorage.clear();
        sessionStorage.clear();
        alert('Tu cuenta ha sido eliminada permanentemente.');
        router.push('/login');
      } else {
        setDeleteError('Error al eliminar la cuenta de usuario.');
      }
    } catch (error) {
      console.error('Delete account error:', error);
      setDeleteError('Ocurrió un error inesperado al eliminar la cuenta.');
    } finally {
      setDeleting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-gray-500">{t("loadingProfile")}</p>
      </div>
    );
  }

  const hasBankData = savedProfile?.bankName && savedProfile?.accountType && savedProfile?.accountNumber;
  const maskedAccount = savedProfile?.accountNumber ? '****' + savedProfile.accountNumber.slice(-4) : '';

  // Subscription & Skills Lock logic
  const isPremium = savedProfile?.plan === 'PREMIUM' || savedProfile?.plan === 'ENTERPRISE';
  const hasSkillsSaved = !!(skillsData.ambiente || skillsData.caracteristica || skillsData.preferencia || (skillsData.habilidades && skillsData.habilidades.length > 0));
  const skillsLocked = hasSkillsSaved && !!savedProfile?.skills && !isPremium;
  const profileViews = parseInt(localStorage.getItem(`profile_views_${formData.userId}`) || '0') || Math.floor(Math.random() * 22) + 5;

  return (
    <div className="max-w-7xl mx-auto pb-20 px-4">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-900">{t("myProfile")}</h1>
        <div className="flex gap-2 text-sm text-gray-500 bg-white px-3 py-1 rounded-full border border-gray-100 shadow-sm">
          <span className="w-2 h-2 bg-green-500 rounded-full mt-1.5 animate-pulse"></span>
          Perfil Trabajador
        </div>
      </div>

      {/* LAYOUT 2 COLUMNAS */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* ═══ SIDEBAR IZQUIERDA ═══ */}
        <div className="space-y-6">

          {/* Foto de perfil */}
          <Card>
            <CardContent className="p-6 flex flex-col items-center">
              <div className="relative group mb-4">
                <div className="w-36 h-36 rounded-full bg-gray-200 flex items-center justify-center border-4 border-white shadow-lg overflow-hidden">
                  {formData.photoUrl ? (
                    /* eslint-disable-next-line @next/next/no-img-element */
                    <img src={formData.photoUrl} alt={t("myProfile")} className="w-full h-full object-cover" />
                  ) : (
                    <User className="w-16 h-16 text-gray-400" />
                  )}
                  {uploading === 'photo' && (
                    <div className="absolute inset-0 bg-black/40 flex items-center justify-center rounded-full">
                      <Loader2 className="w-8 h-8 text-white animate-spin" />
                    </div>
                  )}
                </div>
                <button type="button" onClick={handlePhotoUpload} disabled={uploading !== null}
                  className="absolute bottom-0 right-0 p-2 bg-primary text-white rounded-full shadow-lg hover:bg-primary-dark transition-colors disabled:opacity-50">
                  <Camera size={18} />
                </button>
              </div>
              <h2 className="text-lg font-bold text-gray-900">{formData.name || t("notLoggedIn")}</h2>
              <p className="text-sm text-gray-500">{formData.email}</p>
              <p className="text-xs text-gray-400 mt-1">{formData.phone !== '+56 ' ? formData.phone : ''}</p>
              <span className="mt-3 px-3 py-1 text-xs font-bold text-violet-700 bg-violet-50 border border-violet-200 rounded-full">
                Perfil Trabajador
              </span>
            </CardContent>
          </Card>

          {/* Datos bancarios confirmados */}
          <Card>
            <CardHeader className="border-b bg-gray-50/50 py-3 px-4">
              <h2 className="text-sm font-semibold flex items-center gap-2">
                <CreditCard className="text-primary w-4 h-4" /> {t("paymentMethod")}
              </h2>
            </CardHeader>
            <CardContent className="p-4">
              {hasBankData ? (
                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-sm">
                    <CheckCircle className="w-4 h-4 text-green-500 flex-shrink-0" />
                    <span className="font-medium text-gray-900">{savedProfile?.bankName}</span>
                  </div>
                  <div className="flex items-center gap-2 text-sm text-gray-600">
                    <CheckCircle className="w-4 h-4 text-green-500 flex-shrink-0" />
                    <span>{savedProfile?.accountType === 'VISTA' ? 'Cuenta Vista / RUT' : savedProfile?.accountType === 'CORRIENTE' ? 'Cuenta Corriente' : 'Cuenta de Ahorro'}</span>
                  </div>
                  <div className="flex items-center gap-2 text-sm text-gray-600">
                    <CheckCircle className="w-4 h-4 text-green-500 flex-shrink-0" />
                    <span>N° {maskedAccount}</span>
                  </div>
                  <div className="mt-3 px-3 py-2 bg-green-50 rounded-lg border border-green-100">
                    <p className="text-xs text-green-700 font-medium">✅ {t("bankVerified")}</p>
                  </div>
                </div>
              ) : (
                <p className="text-sm text-gray-400 text-center py-2">{t("noBankData")}</p>
              )}
            </CardContent>
          </Card>

          {/* Historial de postulaciones */}
          <Card>
            <CardHeader className="border-b bg-gray-50/50 py-3 px-4">
              <h2 className="text-sm font-semibold flex items-center gap-2">
                <Clock className="text-primary w-4 h-4" /> {t("applicationHistory")}
              </h2>
            </CardHeader>
            <CardContent className="p-4 space-y-3">
              {loadingPostulaciones ? (
                <div className="flex justify-center py-4"><Loader2 className="animate-spin text-primary w-5 h-5" /></div>
              ) : postulaciones.length > 0 ? (
                postulaciones.slice(0, 3).map(p => (
                  <div key={p.id} className="flex items-start gap-3 p-3 rounded-lg bg-gray-50 hover:bg-gray-100 transition-colors cursor-pointer">
                    <Briefcase className="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" />
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-gray-900 truncate">{p.titulo}</p>
                      <p className="text-xs text-gray-500">{p.empresa} · {p.fecha}</p>
                    </div>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium flex-shrink-0 ${estadoColor[p.estado] || 'bg-gray-100'}`}>
                      {estadoLabel[p.estado] || p.estado}
                    </span>
                  </div>
                ))
              ) : (
                <p className="text-xs text-gray-400 text-center py-4">{t("noApplications")}</p>
              )}
              <Link href="/trabajador/postulaciones">
                <button className="w-full text-xs text-primary hover:text-primary-dark flex items-center justify-center gap-1 pt-2">
                  {t("viewAll")} <ArrowRight className="w-3 h-3" />
                </button>
              </Link>
            </CardContent>
          </Card>

          {/* Dashboard Circular de Habilidades */}
          <Card className="overflow-hidden bg-gradient-to-br from-white to-blue-50/30">
            <CardHeader className="border-b bg-gray-50/50 py-3 px-4">
              <h2 className="text-sm font-semibold flex items-center gap-2">
                <CheckCircle className="text-primary w-4 h-4" /> {t("skillsAptitudes")}
              </h2>
            </CardHeader>
            <CardContent className="p-6">
              <SkillsChart data={chartData} size={180} />
              <div className="mt-4 text-center">
                <p className="text-xs text-gray-500 italic">{t("skillsChartHelper")}</p>
              </div>
            </CardContent>
          </Card>

          {/* Visitas al perfil */}
          <Card className="bg-gradient-to-r from-violet-50 to-indigo-50 border-violet-100/50 overflow-hidden">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-violet-100 flex items-center justify-center flex-shrink-0">
                <Eye className="w-5 h-5 text-violet-600" />
              </div>
              <div className="flex-1">
                <p className="text-xs text-gray-500 font-medium">Visitas al perfil</p>
                <p className="text-xl font-black text-violet-600">{isPremium ? profileViews : <span className="text-sm text-gray-400 font-medium">🔒 Premium</span>}</p>
              </div>
              {!isPremium && (
                <Link href="/trabajador/suscripcion">
                  <span className="text-[10px] text-amber-600 font-bold bg-amber-100 px-2 py-1 rounded-full">Activar</span>
                </Link>
              )}
            </CardContent>
          </Card>

          {/* Suscripción */}
          <Link href="/trabajador/suscripcion">
            <Card className={`cursor-pointer transition-all hover:shadow-md border ${isPremium ? 'border-green-200 bg-green-50/30' : 'border-amber-200 bg-amber-50/30'}`}>
              <CardContent className="p-4 flex items-center gap-3">
                <div className={`w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 ${isPremium ? 'bg-green-100' : 'bg-amber-100'}`}>
                  <Crown className={`w-5 h-5 ${isPremium ? 'text-green-600' : 'text-amber-600'}`} />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-bold text-gray-900">{isPremium ? 'Plan Premium ⭐' : 'Suscripción Premium'}</p>
                  <p className="text-xs text-gray-500">{isPremium ? 'Activo' : '$1.390/mes · Desbloquea todo'}</p>
                </div>
              </CardContent>
            </Card>
          </Link>
        </div>

        {/* ═══ CONTENIDO PRINCIPAL ═══ */}
        <div className="lg:col-span-2 space-y-6">

          {/* Información personal */}
          <Card>
            <CardHeader className="border-b bg-gray-50/50">
              <h2 className="text-lg font-semibold flex items-center gap-2">
                <User className="text-primary w-5 h-5" /> {t("personalInfo")}
              </h2>
            </CardHeader>
            <CardContent className="p-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">{t("fullName")} <span className="text-red-500">*</span> {savedProfile?.name && <span className="text-xs text-gray-400 font-normal ml-2">(No modificable)</span>}</label>
                  <input id="name" type="text" value={formData.name} onChange={handleInputChange}
                    disabled={!!savedProfile?.name}
                    className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary disabled:bg-gray-100 disabled:text-gray-500 disabled:cursor-not-allowed" />
                </div>
                <div>
                  <label htmlFor="rut" className="block text-sm font-medium text-gray-700 mb-1">{t("rut")} <span className="text-red-500">*</span> {savedProfile?.rut && <span className="text-xs text-gray-400 font-normal ml-2">(No modificable)</span>}</label>
                  <input id="rut" type="text" value={formData.rut} onChange={handleInputChange}
                    placeholder="12.345.678-9"
                    disabled={!!savedProfile?.rut}
                    className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary disabled:bg-gray-100 disabled:text-gray-500 disabled:cursor-not-allowed" />
                </div>
                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">{t("aboutMe")}</label>
                  <textarea id="description" rows={3} value={formData.description} onChange={handleInputChange}
                    placeholder={t("aboutMePlaceholder")}
                    className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all resize-none" />
                </div>

                {/* MAPA DE GEOLOCALIZACION */}
                <div className="md:col-span-2 pt-4 border-t border-gray-100">
                  <LocationPicker 
                  apiKey={GOOGLE_MAPS_API_KEY}
                  initialLat={formData.latitude}
                  initialLng={formData.longitude}
                  onLocationChange={async (lat, lng) => {
                    setFormData(prev => ({ ...prev, latitude: lat, longitude: lng }));
                    
                    // Geocoding inverso
                    try {
                      const response = await fetch(`https://maps.googleapis.com/maps/api/geocode/json?latlng=${lat},${lng}&key=${GOOGLE_MAPS_API_KEY}`);
                      const data = await response.json();
                      if (data.results && data.results[0]) {
                        setFormData(prev => ({ ...prev, address: data.results[0].formatted_address }));
                      }
                    } catch (e) {}
                  }}
                />
                </div>
                <div>
                  <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">{t("emailAddress")} <span className="text-red-500">*</span></label>
                  <input id="email" type="email" value={formData.email} onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary bg-gray-50" readOnly />
                </div>
                <div>
                  <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-1">{t("phoneNumber")} <span className="text-red-500">*</span></label>
                  <input id="phone" type="text" value={formData.phone} onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary" />
                </div>
                <div>
                  <label htmlFor="birthDate" className="flex items-center gap-1 text-sm font-medium text-gray-700 mb-1">
                    {t("birthDate")} <span className="text-red-500">*</span> {birthDateLocked && <Lock className="w-3 h-3 text-gray-400" />}
                  </label>
                  <div className="relative">
                    <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <input id="birthDate" type="date" value={formData.birthDate}
                      onChange={handleInputChange} disabled={birthDateLocked}
                      max={new Date().toISOString().split('T')[0]}
                      className={`w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary ${birthDateLocked ? 'bg-gray-100 cursor-not-allowed text-gray-500' : ''}`} />
                  </div>
                  {birthDateLocked && <p className="text-xs text-gray-400 mt-1">{t("birthDateLockedText")}</p>}
                </div>
                <div className="md:col-span-2">
                  <label htmlFor="address" className="block text-sm font-medium text-gray-700 mb-1">{t("addressLabel")} <span className="text-red-500">*</span></label>
                  <div className="flex gap-2">
                    <input id="address" type="text" value={formData.address} onChange={handleInputChange}
                      onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), handleSearchAddress())}
                      placeholder={t("addressPlaceholder")}
                      className="flex-1 border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary" />
                    <Button type="button" variant="outline" onClick={handleSearchAddress}>{t("searchBtn")}</Button>
                  </div>
                  <p className="text-[10px] text-gray-400 mt-1 italic">{t("addressHelper")}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Habilidades y Preferencias */}
          <Card className="relative">
            <CardHeader className="border-b bg-gray-50/50">
              <h2 className="text-lg font-semibold flex items-center gap-2">
                <Briefcase className="text-primary w-5 h-5" /> {t("skillsPreferences")}
                {skillsLocked && (
                  <span className="ml-auto flex items-center gap-1 text-xs text-amber-600 bg-amber-50 px-2 py-0.5 rounded-full border border-amber-200">
                    <Lock size={10} /> Bloqueado
                  </span>
                )}
              </h2>
            </CardHeader>
            {skillsLocked && (
              <div className="absolute inset-0 z-10 bg-white/70 backdrop-blur-[2px] rounded-2xl flex flex-col items-center justify-center gap-3 mt-12">
                <div className="w-14 h-14 bg-amber-100 rounded-full flex items-center justify-center">
                  <Lock className="w-7 h-7 text-amber-600" />
                </div>
                <p className="text-sm font-bold text-gray-800">Habilidades configuradas</p>
                <p className="text-xs text-gray-500 max-w-xs text-center">Para editar tus habilidades y preferencias, activa la suscripción Premium.</p>
                <Link href="/trabajador/suscripcion">
                  <span className="inline-flex items-center gap-1 bg-amber-500 text-white text-xs font-bold px-4 py-2 rounded-full hover:bg-amber-600 transition-colors">
                    <Crown size={12} /> Desbloquear por $1.390/mes
                  </span>
                </Link>
              </div>
            )}
            <CardContent className="p-6 space-y-8">
              {/* Pregunta 1: Habilidades (Múltiple) */}
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-3">{t("skillsQ1")}</label>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                  {SKILLS_WORKER_OPTS.habilidades.map(skill => {
                    const isSelected = skillsData.habilidades.includes(skill);
                    return (
                      <button
                        key={skill}
                        type="button"
                        onClick={() => {
                          const newHabs = isSelected 
                            ? skillsData.habilidades.filter((s: string) => s !== skill)
                            : [...skillsData.habilidades, skill];
                          updateSkills('habilidades', newHabs);
                        }}
                        className={`flex items-center gap-2 px-3 py-2 rounded-lg text-xs font-medium border transition-all ${
                          isSelected 
                            ? 'bg-primary/10 border-primary text-primary shadow-sm' 
                            : 'bg-white border-gray-200 text-gray-600 hover:border-primary/50'
                        }`}
                      >
                        {isSelected ? <CheckSquare size={14} /> : <Square size={14} />}
                        {skill}
                      </button>
                    );
                  })}
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {/* Pregunta 2: Ambiente */}
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-3">{t("skillsQ2")}</label>
                  <div className="space-y-2">
                    {SKILLS_WORKER_OPTS.ambiente.map(opt => (
                      <label key={opt} className={`flex items-center gap-3 p-3 rounded-xl border cursor-pointer transition-all ${
                        skillsData.ambiente === opt ? 'border-primary bg-primary/5 ring-1 ring-primary' : 'border-gray-200 hover:bg-gray-50'
                      }`}>
                        <input
                          type="radio"
                          name="ambiente"
                          checked={skillsData.ambiente === opt}
                          onChange={() => updateSkills('ambiente', opt)}
                          className="w-4 h-4 text-primary focus:ring-primary border-gray-300"
                        />
                        <span className="text-sm text-gray-700">{opt}</span>
                      </label>
                    ))}
                  </div>
                </div>

                {/* Pregunta 3: Característica */}
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-3">{t("skillsQ3")}</label>
                  <div className="space-y-2">
                    {SKILLS_WORKER_OPTS.caracteristica.map(opt => (
                      <label key={opt} className={`flex items-center gap-3 p-3 rounded-xl border cursor-pointer transition-all ${
                        skillsData.caracteristica === opt ? 'border-primary bg-primary/5 ring-1 ring-primary' : 'border-gray-200 hover:bg-gray-50'
                      }`}>
                        <input
                          type="radio"
                          name="caracteristica"
                          checked={skillsData.caracteristica === opt}
                          onChange={() => updateSkills('caracteristica', opt)}
                          className="w-4 h-4 text-primary focus:ring-primary border-gray-300"
                        />
                        <span className="text-sm text-gray-700">{opt}</span>
                      </label>
                    ))}
                  </div>
                </div>
              </div>

              {/* Pregunta 4: Preferencia */}
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-3">{t("skillsQ4")}</label>
                <div className="flex flex-wrap gap-2">
                  {SKILLS_WORKER_OPTS.preferencia.map(opt => (
                    <button
                      key={opt}
                      type="button"
                      onClick={() => updateSkills('preferencia', opt)}
                      className={`px-4 py-2 rounded-full text-sm font-medium border transition-all ${
                        skillsData.preferencia === opt 
                          ? 'bg-primary text-white border-primary shadow-md' 
                          : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'
                      }`}
                    >
                      {opt}
                    </button>
                  ))}
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Mis documentos */}
          <Card>
            <CardHeader className="border-b bg-gray-50/50">
              <h2 className="text-lg font-semibold flex items-center gap-2">
                <FileText className="text-primary w-5 h-5" /> {t("myDocuments")}
              </h2>
            </CardHeader>
            <CardContent className="p-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* CV */}
                <div className={`border-2 rounded-xl p-5 transition-colors ${formData.cvUrl ? 'border-primary/30 bg-primary/5' : 'border-dashed border-gray-200'}`}>
                  <div className="flex items-start gap-3 mb-3">
                    <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${formData.cvUrl ? 'bg-primary/10' : 'bg-gray-100'}`}>
                      {uploading === 'cvUrl' ? <Loader2 className="w-5 h-5 text-primary animate-spin" /> : <Save className={`w-5 h-5 ${formData.cvUrl ? 'text-primary' : 'text-gray-400'}`} />}
                    </div>
                    <div className="flex-1">
                      <h3 className="text-sm font-semibold text-gray-900">{t("curriculumVitae")}</h3>
                      <p className="text-xs text-gray-500">{formData.cvUrl ? t("documentLoaded") : t("documentHelper")}</p>
                    </div>
                  </div>
                  {formData.cvUrl && (
                    <div className="mb-3 p-3 bg-white rounded-lg border border-gray-100">
                      {formData.cvUrl.match(/\.(jpg|jpeg|png|gif|webp)/) ? (
                        /* eslint-disable-next-line @next/next/no-img-element */
                        <img src={formData.cvUrl} alt="Vista previa de CV" className="w-full h-32 object-cover rounded" />
                      ) : (
                        <div className="flex items-center gap-2 text-sm text-gray-600">
                          <FileText className="w-8 h-8 text-red-500" />
                          <div>
                            <p className="font-medium">{t("pdfDocument")}</p>
                            <a href={formData.cvUrl} target="_blank" rel="noopener noreferrer" className="text-xs text-primary hover:underline flex items-center gap-1">
                              <Eye className="w-3 h-3" /> {t("viewDocument")}
                            </a>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                  <input type="file" id="cv-upload" className="hidden" accept=".pdf,.doc,.docx,image/*" onChange={(e) => handleFileUpload(e, 'cvUrl')} />
                  <Button variant="ghost" size="sm" disabled={uploading !== null}
                    onClick={() => document.getElementById('cv-upload')?.click()}>
                    <RefreshCw className="w-3 h-3 mr-1" /> {formData.cvUrl ? t("changeFile") : t("uploadCV")}
                  </Button>
                </div>

                {/* Certificados */}
                <div className={`border-2 rounded-xl p-5 transition-colors ${formData.certificateUrl ? 'border-primary/30 bg-primary/5' : 'border-dashed border-gray-200'}`}>
                  <div className="flex items-start gap-3 mb-3">
                    <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${formData.certificateUrl ? 'bg-primary/10' : 'bg-gray-100'}`}>
                      {uploading === 'certificateUrl' ? <Loader2 className="w-5 h-5 text-primary animate-spin" /> : formData.certificateUrl ? <CheckCircle className="w-5 h-5 text-primary" /> : <Briefcase className="w-5 h-5 text-gray-400" />}
                    </div>
                    <div className="flex-1">
                      <h3 className="text-sm font-semibold text-gray-900">{t("coursesCertificates")}</h3>
                      <p className="text-xs text-gray-500">{formData.certificateUrl ? t("certLoaded") : t("certHelper")}</p>
                    </div>
                  </div>
                  {formData.certificateUrl && (
                    <div className="mb-3 p-3 bg-white rounded-lg border border-gray-100">
                      {formData.certificateUrl.match(/\.(jpg|jpeg|png|gif|webp)/) ? (
                        /* eslint-disable-next-line @next/next/no-img-element */
                        <img src={formData.certificateUrl} alt="Vista previa de certificado" className="w-full h-32 object-cover rounded" />
                      ) : (
                        <div className="flex items-center gap-2 text-sm text-gray-600">
                          <FileText className="w-8 h-8 text-blue-500" />
                          <div>
                            <p className="font-medium">Certificado</p>
                            <a href={formData.certificateUrl} target="_blank" rel="noopener noreferrer" className="text-xs text-primary hover:underline flex items-center gap-1">
                              <Eye className="w-3 h-3" /> {t("viewDocument")}
                            </a>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                  <input type="file" id="cert-upload" className="hidden" accept=".pdf,.doc,.docx,image/*" onChange={(e) => handleFileUpload(e, 'certificateUrl')} />
                  <Button variant="ghost" size="sm" disabled={uploading !== null}
                    onClick={() => document.getElementById('cert-upload')?.click()}>
                    <RefreshCw className="w-3 h-3 mr-1" /> {formData.certificateUrl ? t("changeFile") : t("uploadCert")}
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Datos de transferencia */}
          <Card>
            <CardHeader className="border-b bg-gray-50/50">
              <h2 className="text-lg font-semibold flex items-center gap-2">
                <CreditCard className="text-primary w-5 h-5" /> Datos de Transferencia
              </h2>
            </CardHeader>
            <CardContent className="p-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="bankName" className="block text-sm font-medium text-gray-700 mb-1">Banco <span className="text-red-500">*</span></label>
                  <select id="bankName" value={formData.bankName} onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary bg-white">
                    <option value="">Seleccionar banco...</option>
                    {BANCOS_CHILE.map(b => <option key={b} value={b}>{b}</option>)}
                  </select>
                </div>
                <div>
                  <label htmlFor="accountType" className="block text-sm font-medium text-gray-700 mb-1">Tipo de Cuenta <span className="text-red-500">*</span></label>
                  <select id="accountType" value={formData.accountType} onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary">
                    <option value="">Seleccionar...</option>
                    <option value="VISTA">Cuenta Vista / RUT</option>
                    <option value="CORRIENTE">Cuenta Corriente</option>
                    <option value="AHORRO">Cuenta de Ahorro</option>
                  </select>
                </div>
                <div>
                  <label htmlFor="accountNumber" className="block text-sm font-medium text-gray-700 mb-1">Número de Cuenta <span className="text-red-500">*</span></label>
                  <input id="accountNumber" type="text" value={formData.accountNumber} onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary" />
                </div>
                <div>
                  <label htmlFor="bankAddress" className="block text-sm font-medium text-gray-700 mb-1">Dirección de facturación (Opcional)</label>
                  <input id="bankAddress" type="text" value={formData.bankAddress || ''} onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary" />
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Botón guardar */}
          <div className="flex justify-between items-center pt-8 border-t border-gray-100 mt-8">
            <button 
              onClick={() => setShowDeleteModal(true)}
              className="text-red-500 hover:text-red-700 flex items-center gap-2 text-sm font-medium transition-colors"
            >
              <Trash2 size={16} /> Eliminar mi cuenta permanentemente
            </button>
            <Button variant="primary" size="lg" className="px-8 gap-2 shadow-lg shadow-primary/20" disabled={saving} onClick={handleSave}>
              {saving ? <Loader2 className="animate-spin" /> : <Save size={20} />}
              {saving ? 'Guardando...' : t("saveProfileBtn")}
            </Button>
          </div>
        </div>
      </div>

      {/* MODAL DE ELIMINACIÓN */}
      {showDeleteModal && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
            <div className="p-6">
              <div className="flex justify-between items-start mb-4">
                <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center text-red-600">
                  <AlertTriangle size={24} />
                </div>
                <button onClick={() => setShowDeleteModal(false)} className="text-gray-400 hover:text-gray-600 transition-colors">
                  <X size={20} />
                </button>
              </div>
              
              <h3 className="text-xl font-bold text-gray-900 mb-2">¿Estás seguro de eliminar tu cuenta?</h3>
              <p className="text-gray-500 text-sm mb-6">
                Esta acción es **permanente** y no se puede deshacer. Se eliminarán todas tus postulaciones, datos bancarios, documentos y perfil personal.
              </p>

              <div className="space-y-4">
                <div>
                  <label htmlFor="deletePassword" className="block text-sm font-medium text-gray-700 mb-2">
                    Confirma tu contraseña para continuar
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <input 
                      type="password" 
                      id="deletePassword"
                      value={deletePassword}
                      onChange={(e) => setDeletePassword(e.target.value)}
                      placeholder="••••••••"
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all outline-none"
                    />
                  </div>
                  {deleteError && <p className="mt-2 text-xs text-red-500 font-medium">{deleteError}</p>}
                </div>

                <div className="flex gap-3 pt-2">
                  <button
                    onClick={() => setShowDeleteModal(false)}
                    className="flex-1 px-4 py-3 border border-gray-200 rounded-xl text-sm font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
                  >
                    Cancelar
                  </button>
                  <button
                    onClick={handleDeleteAccount}
                    disabled={deleting}
                    className="flex-1 px-4 py-3 bg-red-600 hover:bg-red-700 disabled:opacity-50 text-white rounded-xl text-sm font-semibold shadow-lg shadow-red-200 transition-colors flex items-center justify-center gap-2"
                  >
                    {deleting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Trash2 size={16} />}
                    {deleting ? 'Eliminando...' : t("deleteAccountBtn")}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
