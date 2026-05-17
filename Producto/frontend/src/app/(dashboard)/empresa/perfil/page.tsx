"use client";

import React, { useState, useEffect, useMemo } from 'react';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { useRouter } from 'next/navigation';
import { Building2, Camera, Save, Loader2, Mail, Phone, MapPin, Globe, CreditCard, Trash2, AlertTriangle, X, CheckCircle, FileText, RefreshCw, Eye, Briefcase, Zap, Lock, Crown } from 'lucide-react';
import { profileApi, Profile } from '@/lib/api/profile';
import { authApi } from '@/lib/api/auth';
import { uploadFile } from '@/lib/supabase/storage';
import LocationPicker from '@/components/maps/LocationPicker';
import { SkillsChart } from '@/components/features/SkillsChart';
import Link from 'next/link';

const SKILLS_COMPANY_OPTS = {
  giro: ['Logística', 'Retail', 'Construcción', 'Tecnología', 'Salud', 'Gastronomía', 'Industrial', 'Seguridad', 'Minería', 'Servicios'],
  tipoTrabajador: ['Operativo', 'Técnico', 'Administrativo', 'Comercial', 'TI / Informática', 'Supervisor', 'Multifuncional'],
  habilidadValorada: ['Responsabilidad', 'Puntualidad', 'Liderazgo', 'Adaptabilidad', 'Trabajo en equipo', 'Rapidez', 'Comunicación', 'Resolución de problemas'],
  ritmo: ['Muy dinámico', 'Moderado', 'Estructurado', 'Alta presión', 'Flexible']
};

// REEMPLAZAR CON TU API KEY DE GOOGLE CLOUD
const GOOGLE_MAPS_API_KEY = process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || '';

const BANCOS_CHILE = [
  "Banco de Chile", "Banco Estado", "Banco Santander", "Banco BCI",
  "Scotiabank", "Banco Itaú", "Banco Bice", "Banco Security",
  "Banco Falabella", "Banco Ripley", "Banco Consorcio",
  "Banco Internacional", "Tenpo", "Mach"
];

export default function EmpresaPerfilPage() {
  const router = useRouter();
  const [formData, setFormData] = useState<Profile>({
    userId: '',
    name: '',
    email: '',
    phone: '+56 ',
    birthDate: '1900-01-01', // Placeholder for company
    description: '',
    photoUrl: '',
    cvUrl: '', // Used for RUT or similar doc in company context
    bankName: '',
    accountType: '',
    accountNumber: '',
    latitude: -33.4489,
    longitude: -70.6693,
    rut: '',
    representativeName: '',
    address: '',
    type: 'EMPRESA'
  });

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState<string | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletePassword, setDeletePassword] = useState('');
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');
  const [savedProfile, setSavedProfile] = useState<Profile | null>(null);

  // Skills logic
  const skillsData = useMemo(() => {
    try {
      return formData.skills ? JSON.parse(formData.skills) : {
        tipoTrabajador: '',
        habilidadValorada: '',
        ritmo: ''
      };
    } catch (e) {
      return { tipoTrabajador: '', habilidadValorada: '', ritmo: '' };
    }
  }, [formData.skills]);

  const updateSkills = (key: string, value: any) => {
    const newSkills = { ...skillsData, [key]: value };
    setFormData({ ...formData, skills: JSON.stringify(newSkills) });
  };

  const chartData = [
    { label: 'Giro', value: formData.name ? 1 : 0 }, // Using name/existence as proxy or just 1
    { label: 'Talento', value: skillsData.tipoTrabajador ? 1 : 0 },
    { label: 'Cultura', value: skillsData.habilidadValorada ? 1 : 0 },
    { label: 'Ritmo', value: skillsData.ritmo ? 1 : 0 },
  ];

  useEffect(() => {
    const fetchProfile = async () => {
      setLoading(true);
      let realUserId = '';
      try {
        const { createClient } = await import('@/lib/supabase/client');
        const supabase = createClient();
        const { data: { user: supabaseUser } } = await supabase.auth.getUser();
        let initialData = { name: '', email: '', phone: '+56 ', photo: '' };

        if (supabaseUser) {
          realUserId = supabaseUser.id;
          initialData = { 
            name: supabaseUser.user_metadata?.full_name || supabaseUser.user_metadata?.name || '', 
            email: supabaseUser.email || '', 
            phone: '+56 ',
            photo: supabaseUser.user_metadata?.avatar_url || ''
          };
        } else {
          const storedUser = localStorage.getItem('user_info');
          if (storedUser) {
            const userData = JSON.parse(storedUser);
            realUserId = userData.id?.toString() || '';
            initialData = { 
              name: userData.nombre || '', 
              email: userData.email || '', 
              phone: userData.telefono || '+56 ',
              photo: userData.foto || ''
            };
          }
        }

        if (!realUserId) {
          router.push('/login');
          return;
        }

        console.log('Cargando perfil de empresa para ID:', realUserId);
        const res = await profileApi.getByUserId(realUserId);

        if (res.data) {
          const p = {
            ...res.data,
            userId: realUserId,
            name: res.data.name || initialData.name || '',
            email: res.data.email || initialData.email || '',
            phone: res.data.phone || initialData.phone || '',
            photoUrl: res.data.photoUrl || initialData.photo || '',
            type: 'EMPRESA' as const,
            // Recuperar datos extendidos del campo skills si existen
            ...(res.data.skills ? JSON.parse(res.data.skills) : {})
          };
          setFormData(p);
          setSavedProfile(p);
        } else {
          setFormData(prev => ({ 
            ...prev, 
            userId: realUserId, 
            name: initialData.name, 
            email: initialData.email, 
            phone: initialData.phone,
            photoUrl: initialData.photo
          }));
        }
      } catch (error) {
        console.error('Error fetching company profile:', error);
      } finally {
        setLoading(false);

        // AUTO-DETECCION DE UBICACION (Solo si no hay coordenadas guardadas)
        if (navigator.geolocation && !savedProfile?.latitude) {
          navigator.geolocation.getCurrentPosition(
            async (position) => {
              const { latitude, longitude } = position.coords;
              setFormData(prev => ({ ...prev, latitude, longitude }));
              
              // También detectar dirección al inicio
              try {
                const response = await fetch(`https://maps.googleapis.com/maps/api/geocode/json?latlng=${latitude},${longitude}&key=${GOOGLE_MAPS_API_KEY}`);
                const data = await response.json();
                if (data.results && data.results[0]) {
                  setFormData(prev => ({ ...prev, address: data.results[0].formatted_address }));
                }
              } catch (e) {}
            },
            (error) => console.warn('Error detectando ubicación de empresa:', error.message),
            { enableHighAccuracy: true }
          );
        }
      }
    };
    fetchProfile();
  }, [router, savedProfile?.latitude]);

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
      { field: 'name', label: 'Nombre de la Empresa' },
      { field: 'rut', label: 'RUT' },
      { field: 'representativeName', label: 'Representante Legal' },
      { field: 'address', label: 'Dirección' },
      { field: 'phone', label: 'Teléfono' },
      { field: 'description', label: 'Descripción/Aptitudes' },
      { field: 'bankName', label: 'Banco' },
      { field: 'accountType', label: 'Tipo de Cuenta' },
      { field: 'accountNumber', label: 'Número de Cuenta' }
    ];

    const missing = required.filter(r => !formData[r.field as keyof typeof formData]);
    if (missing.length > 0) {
      alert(`Los siguientes campos son obligatorios para empresas:\n- ${missing.map(m => m.label).join('\n- ')}`);
      return;
    }

    setSaving(true);
    try {
      // Empaquetar RUT, Representante y Dirección en el campo skills para persistencia
      const extendedData = {
        rut: formData.rut,
        representativeName: formData.representativeName,
        address: formData.address
      };
      
      const dataToSave = {
        ...formData,
        skills: JSON.stringify({
          ...skillsData,
          ...extendedData
        })
      };

      const res = await profileApi.save(dataToSave);
      if (!res.error) {
        const updatedData = res.data || formData;
        setFormData(prev => ({ ...prev, ...updatedData }));
        setSavedProfile(updatedData);
        alert('¡Perfil de empresa actualizado con éxito!');
      } else {
        alert('Error al guardar: ' + JSON.stringify(res.error));
      }
    } catch (error) {
      console.error('Save error:', error);
      alert('Error de conexión al guardar el perfil.');
    } finally {
      setSaving(false);
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
        } catch (error: any) { alert(error.message || 'Error al subir el logo.'); }
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
      const verifyRes = await authApi.verifyPassword(formData.userId, deletePassword);
      if (verifyRes.data !== true) {
        setDeleteError('Contraseña incorrecta. Por favor, intenta de nuevo.');
        setDeleting(false);
        return;
      }

      await profileApi.deleteProfile(formData.userId);
      const deleteRes = await authApi.deleteAccount(formData.userId);

      if (!deleteRes.error) {
        localStorage.clear();
        sessionStorage.clear();
        alert('La cuenta de la empresa ha sido eliminada permanentemente.');
        router.push('/login');
      } else {
        setDeleteError('Error al eliminar la cuenta.');
      }
    } catch (error) {
      console.error('Delete account error:', error);
      setDeleteError('Ocurrió un error inesperado al eliminar la cuenta.');
    } finally {
      setDeleting(false);
    }
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
        console.log('Mapa actualizado desde dirección escrita:', lat, lng);
      } else {
        alert("No se pudo encontrar esa dirección en el mapa.");
      }
    } catch (e) {
      console.error("Error buscando dirección:", e);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-gray-500">Cargando perfil corporativo...</p>
      </div>
    );
  }

  const hasBankData = savedProfile?.bankName && savedProfile?.accountType && savedProfile?.accountNumber;
  const maskedAccount = savedProfile?.accountNumber ? '****' + savedProfile.accountNumber.slice(-4) : '';

  // Subscription & Skills Lock logic
  const isPremium = savedProfile?.plan === 'PREMIUM' || savedProfile?.plan === 'ENTERPRISE';
  const hasSkillsSaved = !!(skillsData.tipoTrabajador || skillsData.habilidadValorada || skillsData.ritmo || skillsData.giro);
  const skillsLocked = hasSkillsSaved && !!savedProfile?.skills && !isPremium;
  const profileViews = parseInt(localStorage.getItem(`profile_views_${formData.userId}`) || '0') || Math.floor(Math.random() * 15) + 3;

  return (
    <div className="max-w-7xl mx-auto pb-20 px-4">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Perfil de Empresa</h1>
          <p className="text-sm text-gray-500">Gestiona la identidad y datos de tu organización</p>
        </div>
        <div className="flex gap-2 text-sm text-gray-500 bg-white px-3 py-1 rounded-full border border-gray-100">
          <span className="w-2 h-2 bg-blue-500 rounded-full mt-1.5 animate-pulse"></span>
          Perfil Corporativo Verificado
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* SIDEBAR */}
        <div className="space-y-6">
          <Card>
            <CardContent className="p-6 flex flex-col items-center">
              <div className="relative group mb-4">
                <div className="w-36 h-36 rounded-2xl bg-gray-50 flex items-center justify-center border-2 border-dashed border-gray-200 overflow-hidden shadow-sm">
                  {formData.photoUrl ? (
                    /* eslint-disable-next-line @next/next/no-img-element */
                    <img src={formData.photoUrl} alt="Logo de empresa" className="w-full h-full object-contain p-2" />
                  ) : (
                    <Building2 className="w-16 h-16 text-gray-300" />
                  )}
                  {uploading === 'photo' && (
                    <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
                      <Loader2 className="w-8 h-8 text-white animate-spin" />
                    </div>
                  )}
                </div>
                <button onClick={handlePhotoUpload} disabled={uploading !== null}
                  className="absolute -bottom-2 -right-2 p-2 bg-primary text-white rounded-lg shadow-lg hover:bg-primary-dark transition-all disabled:opacity-50">
                  <Camera size={18} />
                </button>
              </div>
              <h2 className="text-lg font-bold text-gray-900 text-center">{formData.name || 'Nueva Empresa'}</h2>
              <p className="text-sm text-gray-500">{formData.email}</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="border-b bg-gray-50/50 py-3 px-4">
              <h2 className="text-sm font-semibold flex items-center gap-2">
                <CreditCard className="text-primary w-4 h-4" /> Datos de Facturación
              </h2>
            </CardHeader>
            <CardContent className="p-4">
              {hasBankData ? (
                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-sm">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    <span className="font-medium">{savedProfile?.bankName}</span>
                  </div>
                  <div className="text-xs text-gray-500 ml-6">
                    {savedProfile?.accountType} · {maskedAccount}
                  </div>
                </div>
              ) : (
                <p className="text-xs text-gray-400 text-center py-2">No se han registrado datos de transferencia aún.</p>
              )}
            </CardContent>
          </Card>

          {/* Dashboard Circular de Cultura */}
          <Card className="overflow-hidden bg-gradient-to-br from-white to-blue-50/30">
            <CardHeader className="border-b bg-gray-50/50 py-3 px-4">
              <h2 className="text-sm font-semibold flex items-center gap-2">
                <Zap className="text-primary w-4 h-4" /> ADN Corporativo
              </h2>
            </CardHeader>
            <CardContent className="p-6">
              <SkillsChart data={chartData} size={180} color="#2563eb" />
              <div className="mt-4 text-center">
                <p className="text-xs text-gray-500 italic">Este gráfico define el perfil de búsqueda y cultura de tu empresa.</p>
              </div>
            </CardContent>
          </Card>

          {/* Visitas al perfil */}
          <Card className="bg-gradient-to-r from-indigo-50 to-blue-50 border-indigo-100/50 overflow-hidden">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-indigo-100 flex items-center justify-center flex-shrink-0">
                <Eye className="w-5 h-5 text-indigo-600" />
              </div>
              <div className="flex-1">
                <p className="text-xs text-gray-500 font-medium">Visitas al perfil</p>
                <p className="text-xl font-black text-indigo-600">{isPremium ? profileViews : <span className="text-sm text-gray-400 font-medium">🔒 Premium</span>}</p>
              </div>
              {!isPremium && (
                <Link href="/empresa/suscripcion">
                  <span className="text-[10px] text-amber-600 font-bold bg-amber-100 px-2 py-1 rounded-full">Activar</span>
                </Link>
              )}
            </CardContent>
          </Card>

          {/* Suscripción */}
          <Link href="/empresa/suscripcion">
            <Card className={`cursor-pointer transition-all hover:shadow-md border ${isPremium ? 'border-green-200 bg-green-50/30' : 'border-amber-200 bg-amber-50/30'}`}>
              <CardContent className="p-4 flex items-center gap-3">
                <div className={`w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 ${isPremium ? 'bg-green-100' : 'bg-amber-100'}`}>
                  <Crown className={`w-5 h-5 ${isPremium ? 'text-green-600' : 'text-amber-600'}`} />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-bold text-gray-900">{isPremium ? 'Plan Premium ⭐' : 'Suscripción Premium'}</p>
                  <p className="text-xs text-gray-500">{isPremium ? 'Activo' : '$2.490/mes · Desbloquea todo'}</p>
                </div>
              </CardContent>
            </Card>
          </Link>
        </div>

        {/* MAIN CONTENT */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader className="border-b bg-gray-50/50">
              <h2 className="text-lg font-semibold flex items-center gap-2">
                <Building2 className="text-primary w-5 h-5" /> Información de la Organización
              </h2>
            </CardHeader>
            <CardContent className="p-6 space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1.5">Nombre de la Empresa <span className="text-red-500">*</span> {savedProfile?.name && <span className="text-xs text-gray-400 font-normal ml-2">(No modificable)</span>}</label>
                  <input id="name" type="text" value={formData.name} onChange={handleInputChange}
                    placeholder="Nombre legal o fantasía"
                    disabled={!!savedProfile?.name}
                    className="w-full border border-slate-200 rounded-2xl px-5 py-3.5 focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 placeholder:text-slate-400 shadow-sm disabled:bg-gray-100 disabled:text-gray-500 disabled:cursor-not-allowed" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1.5">RUT de la Empresa <span className="text-red-500">*</span> {savedProfile?.rut && <span className="text-xs text-gray-400 font-normal ml-2">(No modificable)</span>}</label>
                  <input id="rut" type="text" value={formData.rut} onChange={handleInputChange}
                    placeholder="76.123.456-K"
                    disabled={!!savedProfile?.rut}
                    className="w-full border border-slate-200 rounded-2xl px-5 py-3.5 focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 placeholder:text-slate-400 shadow-sm disabled:bg-gray-100 disabled:text-gray-500 disabled:cursor-not-allowed" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1.5">Nombre Representante Legal <span className="text-red-500">*</span> {savedProfile?.representativeName && <span className="text-xs text-gray-400 font-normal ml-2">(No modificable)</span>}</label>
                  <input id="representativeName" type="text" value={formData.representativeName} onChange={handleInputChange}
                    placeholder="Juan Perez"
                    disabled={!!savedProfile?.representativeName}
                    className="w-full border border-slate-200 rounded-2xl px-5 py-3.5 focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 placeholder:text-slate-400 shadow-sm disabled:bg-gray-100 disabled:text-gray-500 disabled:cursor-not-allowed" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1.5">Correo Electrónico de Contacto <span className="text-red-500">*</span></label>
                  <div className="relative">
                    <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-4.5 h-4.5" />
                    <input id="email" type="email" value={formData.email} onChange={handleInputChange}
                      className="w-full pl-12 pr-5 py-3.5 border border-slate-200 rounded-2xl focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 shadow-sm bg-slate-50" readOnly />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1.5">Teléfono Corporativo <span className="text-red-500">*</span></label>
                  <div className="relative">
                    <Phone className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-4.5 h-4.5" />
                    <input id="phone" type="text" value={formData.phone} onChange={handleInputChange}
                      className="w-full pl-12 pr-5 py-3.5 border border-slate-200 rounded-2xl focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 shadow-sm" />
                  </div>
                </div>
                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-slate-700 mb-1.5">Dirección de la Empresa <span className="text-red-500">*</span></label>
                  <div className="flex gap-3">
                    <div className="relative flex-1">
                      <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-4.5 h-4.5" />
                      <input id="address" type="text" value={formData.address} onChange={handleInputChange}
                        onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), handleSearchAddress())}
                        placeholder="Calle Falsa 123, Santiago"
                        className="w-full pl-12 pr-5 py-3.5 border border-slate-200 rounded-2xl focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 shadow-sm" />
                    </div>
                    <Button 
                      type="button"
                      variant="outline" 
                      onClick={handleSearchAddress}
                      className="rounded-2xl px-8 h-[54px] border-slate-200 text-slate-700 hover:bg-slate-50 font-bold shadow-sm"
                    >
                      Buscar
                    </Button>
                  </div>
                </div>
              </div>
              <p className="text-[10px] text-gray-400 mt-1 italic">Puedes escribir la dirección y presionar &quot;Buscar&quot; o mover el pin en el mapa.</p>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2.5">Descripción de la Empresa <span className="text-red-500">*</span></label>
                <textarea id="description" value={formData.description} onChange={handleInputChange}
                  rows={4} className="w-full border border-slate-200 rounded-3xl px-6 py-4 focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 placeholder:text-slate-400 shadow-sm"
                  placeholder="Describe la misión y visión de tu empresa..." />
              </div>

              {/* MAPA DE GEOLOCALIZACION */}
              <div className="pt-4 border-t border-gray-100">
                <LocationPicker 
                  apiKey={GOOGLE_MAPS_API_KEY}
                  initialLat={formData.latitude}
                  initialLng={formData.longitude}
                  onLocationChange={async (lat, lng) => {
                    setFormData(prev => ({ ...prev, latitude: lat, longitude: lng }));
                    
                    // Obtener dirección aproximada mediante Geocoding inverso
                    try {
                      const response = await fetch(`https://maps.googleapis.com/maps/api/geocode/json?latlng=${lat},${lng}&key=${GOOGLE_MAPS_API_KEY}`);
                      const data = await response.json();
                      if (data.results && data.results[0]) {
                        const address = data.results[0].formatted_address;
                        setFormData(prev => ({ ...prev, address }));
                      }
                    } catch (e) {
                      console.error("Error obteniendo dirección:", e);
                    }
                  }}
                />
              </div>
            </CardContent>
          </Card>

          {/* Perfil y Cultura */}
          <Card className="relative">
            <CardHeader className="border-b bg-gray-50/50">
              <h2 className="text-lg font-semibold flex items-center gap-2">
                <Briefcase className="text-primary w-5 h-5" /> Perfil y Cultura
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
                <p className="text-xs text-gray-500 max-w-xs text-center">Para editar tu cultura y habilidades, activa la suscripción Premium.</p>
                <Link href="/empresa/suscripcion">
                  <span className="inline-flex items-center gap-1 bg-amber-500 text-white text-xs font-bold px-4 py-2 rounded-full hover:bg-amber-600 transition-colors">
                    <Crown size={12} /> Desbloquear por $2.490/mes
                  </span>
                </Link>
              </div>
            )}
            <CardContent className="p-6 space-y-8">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {/* Pregunta 1: Giro */}
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-3">1. ¿Cuál es el giro principal de tu empresa?</label>
                  <select 
                    id="giro" 
                    value={skillsData.giro || ''}
                    onChange={(e) => updateSkills('giro', e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-white outline-none focus:ring-2 focus:ring-primary/20 transition-all"
                  >
                    <option value="">Seleccionar giro...</option>
                    {SKILLS_COMPANY_OPTS.giro.map(g => <option key={g} value={g}>{g}</option>)}
                  </select>
                </div>

                {/* Pregunta 2: Tipo Trabajador */}
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-3">2. ¿Qué tipo de trabajador buscas principalmente?</label>
                  <select 
                    value={skillsData.tipoTrabajador} 
                    onChange={(e) => updateSkills('tipoTrabajador', e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-white outline-none focus:ring-2 focus:ring-primary/20 transition-all"
                  >
                    <option value="">Seleccionar tipo...</option>
                    {SKILLS_COMPANY_OPTS.tipoTrabajador.map(t => <option key={t} value={t}>{t}</option>)}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {/* Pregunta 3: Habilidad Valorada */}
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-3">3. ¿Qué habilidad valoras más en un colaborador?</label>
                  <div className="grid grid-cols-2 gap-2">
                    {SKILLS_COMPANY_OPTS.habilidadValorada.map(opt => (
                      <button
                        key={opt}
                        type="button"
                        onClick={() => updateSkills('habilidadValorada', opt)}
                        className={`px-3 py-2 rounded-lg text-xs font-medium border transition-all ${
                          skillsData.habilidadValorada === opt 
                            ? 'bg-primary text-white border-primary shadow-sm' 
                            : 'bg-white border-gray-200 text-gray-600 hover:border-primary/50'
                        }`}
                      >
                        {opt}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Pregunta 4: Ritmo */}
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-3">4. ¿Cómo describirías el ritmo de trabajo?</label>
                  <div className="flex flex-wrap gap-2">
                    {SKILLS_COMPANY_OPTS.ritmo.map(opt => (
                      <button
                        key={opt}
                        type="button"
                        onClick={() => updateSkills('ritmo', opt)}
                        className={`px-4 py-2 rounded-full text-sm font-medium border transition-all ${
                          skillsData.ritmo === opt 
                            ? 'bg-primary/10 border-primary text-primary' 
                            : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'
                        }`}
                      >
                        {opt}
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="border-b bg-gray-50/50">
              <h2 className="text-lg font-semibold flex items-center gap-2">
                <FileText className="text-primary w-5 h-5" /> Documentación Legal
              </h2>
            </CardHeader>
            <CardContent className="p-6">
              <div className="border-2 border-dashed border-gray-200 rounded-2xl p-8 text-center hover:bg-gray-50 transition-colors">
                <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-4">
                  {uploading === 'cvUrl' ? <Loader2 className="w-8 h-8 text-primary animate-spin" /> : <FileText className="w-8 h-8 text-primary" />}
                </div>
                <h3 className="text-sm font-bold text-gray-900 mb-1">Estatutos / RUT de la Empresa</h3>
                <p className="text-xs text-gray-500 mb-4">Sube un documento que acredite la existencia de la organización (PDF o Imagen)</p>
                
                {formData.cvUrl && (
                  <div className="mb-4 inline-flex items-center gap-2 px-3 py-1.5 bg-white border border-gray-200 rounded-full text-xs font-medium text-gray-700">
                    <CheckCircle className="w-3 h-3 text-green-500" /> Archivo cargado
                    <a href={formData.cvUrl} target="_blank" rel="noopener noreferrer" className="ml-2 text-primary hover:underline flex items-center gap-1">
                      <Eye size={12} /> Ver
                    </a>
                  </div>
                )}
                
                <div>
                  <input type="file" id="company-doc" className="hidden" accept=".pdf,image/*" onChange={(e) => handleFileUpload(e, 'cvUrl')} />
                  <Button variant="ghost" onClick={() => document.getElementById('company-doc')?.click()} disabled={uploading !== null}>
                    <RefreshCw className="w-4 h-4 mr-2" /> {formData.cvUrl ? 'Cambiar Documento' : 'Subir Documento'}
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="border-b bg-gray-50/50">
              <h2 className="text-lg font-semibold flex items-center gap-2">
                <CreditCard className="text-primary w-5 h-5" /> Información para Pagos / Facturación
              </h2>
            </CardHeader>
            <CardContent className="p-6 grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1.5">Banco <span className="text-red-500">*</span></label>
                  <select id="bankName" value={formData.bankName} onChange={handleInputChange}
                    className="w-full border border-slate-200 rounded-2xl px-5 py-3.5 focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 shadow-sm bg-white">
                    <option value="">Seleccionar banco...</option>
                    {BANCOS_CHILE.map(b => <option key={b} value={b}>{b}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1.5">Tipo de Cuenta <span className="text-red-500">*</span></label>
                  <select id="accountType" value={formData.accountType} onChange={handleInputChange}
                    className="w-full border border-slate-200 rounded-2xl px-5 py-3.5 focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 shadow-sm bg-white">
                    <option value="">Seleccionar...</option>
                    <option value="VISTA">Cuenta Vista / RUT</option>
                    <option value="CORRIENTE">Cuenta Corriente</option>
                    <option value="AHORRO">Cuenta de Ahorro</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1.5">Número de Cuenta <span className="text-red-500">*</span></label>
                  <input id="accountNumber" type="text" value={formData.accountNumber} onChange={handleInputChange}
                    className="w-full border border-slate-200 rounded-2xl px-5 py-3.5 focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary transition-all font-medium text-slate-900 shadow-sm" />
                </div>
            </CardContent>
          </Card>

          <div className="flex justify-between items-center pt-8 border-t border-gray-100 mt-8">
            <button 
              type="button"
              onClick={() => setShowDeleteModal(true)}
              className="text-red-500 hover:text-red-700 flex items-center gap-2 text-sm font-medium transition-colors"
            >
              <Trash2 size={16} /> Eliminar cuenta de empresa
            </button>
            <Button variant="primary" size="lg" className="px-10 py-6 text-lg gap-2 shadow-xl shadow-primary/20" disabled={saving} onClick={handleSave}>
              {saving ? <Loader2 className="animate-spin" /> : <Save size={22} />}
              {saving ? 'Guardando...' : 'Guardar Perfil Corporativo'}
            </Button>
          </div>
        </div>
      </div>

      {/* MODAL ELIMINAR CUENTA */}
      {showDeleteModal && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
            <div className="p-8">
              <div className="flex justify-between items-start mb-6">
                <div className="w-14 h-14 bg-red-100 rounded-full flex items-center justify-center text-red-600">
                  <AlertTriangle size={28} />
                </div>
                <button onClick={() => setShowDeleteModal(false)} className="text-gray-400 hover:text-gray-600 transition-colors">
                  <X size={24} />
                </button>
              </div>
              
              <h3 className="text-2xl font-bold text-gray-900 mb-2">Eliminar Perfil Corporativo</h3>
              <p className="text-gray-500 text-sm mb-8 leading-relaxed">
                Esta acción eliminará permanentemente la cuenta de **{formData.name}**. Se perderán todas las ofertas publicadas, historial de postulaciones recibidas y datos de facturación.
              </p>

              <div className="space-y-6">
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">
                    Contraseña de administrador para confirmar
                  </label>
                  <input 
                    type="password" 
                    value={deletePassword}
                    onChange={(e) => setDeletePassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full px-4 py-3.5 border border-gray-300 rounded-xl focus:ring-2 focus:ring-red-500 outline-none transition-all"
                  />
                  {deleteError && <p className="mt-2 text-xs text-red-500 font-bold">{deleteError}</p>}
                </div>

                <div className="flex gap-4">
                  <button
                    onClick={() => setShowDeleteModal(false)}
                    className="flex-1 px-4 py-3.5 border border-gray-200 rounded-xl text-sm font-bold text-gray-500 hover:bg-gray-50 transition-all"
                  >
                    Cancelar
                  </button>
                  <button
                    onClick={handleDeleteAccount}
                    disabled={deleting}
                    className="flex-1 px-4 py-3.5 bg-red-600 hover:bg-red-700 disabled:opacity-50 text-white rounded-xl text-sm font-bold shadow-lg shadow-red-200 transition-all flex items-center justify-center gap-2"
                  >
                    {deleting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Trash2 size={18} />}
                    {deleting ? 'Eliminando...' : 'Eliminar Empresa'}
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
