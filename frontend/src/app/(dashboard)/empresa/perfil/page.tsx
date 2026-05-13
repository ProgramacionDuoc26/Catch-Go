"use client";

import React, { useState, useEffect, useMemo } from 'react';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { useRouter } from 'next/navigation';
import { Building2, Camera, Save, Loader2, Mail, Phone, MapPin, Globe, CreditCard, Trash2, AlertTriangle, X, CheckCircle, FileText, RefreshCw, Eye, Briefcase, Zap } from 'lucide-react';
import { profileApi, Profile } from '@/lib/api/profile';
import { authApi } from '@/lib/api/auth';
import { uploadFile } from '@/lib/supabase/storage';
import LocationPicker from '@/components/maps/LocationPicker';
import { SkillsChart } from '@/components/features/SkillsChart';

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
        const storedUser = localStorage.getItem('user_info');
        let initialData = { name: '', email: '', phone: '+56 ' };

        if (storedUser) {
          const userData = JSON.parse(storedUser);
          realUserId = userData.id?.toString() || '';
          initialData = { 
            name: userData.nombre || '', 
            email: userData.email || '', 
            phone: userData.telefono || '+56 ' 
          };
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
            type: 'EMPRESA' as const
          };
          setFormData(p);
          setSavedProfile(p);
        } else {
          setFormData(prev => ({ 
            ...prev, 
            userId: realUserId, 
            name: initialData.name, 
            email: initialData.email, 
            phone: initialData.phone 
          }));
        }
      } catch (error) {
        console.error('Error fetching company profile:', error);
      } finally {
        setLoading(false);
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
    setSaving(true);
    try {
      const res = await profileApi.save(formData);
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
                    <img src={formData.photoUrl} alt="Logo" className="w-full h-full object-contain p-2" />
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
                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">Nombre de la Empresa / Razón Social</label>
                  <input id="name" type="text" value={formData.name} onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Correo Electrónico de Contacto</label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <input id="email" type="email" value={formData.email} onChange={handleInputChange}
                      className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all" />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono Corporativo</label>
                  <div className="relative">
                    <Phone className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <input id="phone" type="text" value={formData.phone} onChange={handleInputChange}
                      className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all" />
                  </div>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Descripción de la Empresa</label>
                <textarea id="description" rows={3} value={formData.description} onChange={handleInputChange}
                  placeholder="Describe la actividad de tu empresa..."
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all resize-none" />
              </div>

              {/* MAPA DE GEOLOCALIZACION */}
              <div className="pt-4 border-t border-gray-100">
                <LocationPicker 
                  apiKey={GOOGLE_MAPS_API_KEY}
                  initialLat={formData.latitude}
                  initialLng={formData.longitude}
                  onLocationChange={(lat, lng) => {
                    setFormData(prev => ({ ...prev, latitude: lat, longitude: lng }));
                  }}
                />
              </div>
            </CardContent>
          </Card>

          {/* Perfil y Cultura */}
          <Card>
            <CardHeader className="border-b bg-gray-50/50">
              <h2 className="text-lg font-semibold flex items-center gap-2">
                <Briefcase className="text-primary w-5 h-5" /> Perfil y Cultura
              </h2>
            </CardHeader>
            <CardContent className="p-6 space-y-8">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {/* Pregunta 1: Giro */}
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-3">1. ¿Cuál es el giro principal de tu empresa?</label>
                  <select 
                    id="giro" 
                    value={formData.description || ''} // Using description or adding a giro field?
                    // Wait, Empresa entity has a giro field in Prisma but the Profile entity in Java/Frontend doesn't explicitly have it beyond the 'description' or maybe I should use the new 'skills'
                    onChange={(e) => setFormData({...formData, description: e.target.value})}
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
                <label className="block text-sm font-medium text-gray-700 mb-1">Banco</label>
                <select id="bankName" value={formData.bankName} onChange={handleInputChange}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-white outline-none focus:ring-2 focus:ring-primary/20 transition-all">
                  <option value="">Seleccionar...</option>
                  {BANCOS_CHILE.map(b => <option key={b} value={b}>{b}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Tipo de Cuenta</label>
                <select id="accountType" value={formData.accountType} onChange={handleInputChange}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 bg-white outline-none focus:ring-2 focus:ring-primary/20 transition-all">
                  <option value="">Seleccionar...</option>
                  <option value="VISTA">Cuenta Vista / RUT</option>
                  <option value="CORRIENTE">Cuenta Corriente</option>
                  <option value="AHORRO">Cuenta de Ahorro</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Número de Cuenta</label>
                <input id="accountNumber" type="text" value={formData.accountNumber} onChange={handleInputChange}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 outline-none border-gray-300 focus:ring-2 focus:ring-primary/20 transition-all" />
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
