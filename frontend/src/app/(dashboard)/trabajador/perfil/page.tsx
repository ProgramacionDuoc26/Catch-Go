"use client";

import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { User, Camera, Calendar, CreditCard, Save, Loader2, Briefcase, FileText, CheckCircle, Lock, Eye, RefreshCw, Clock, ArrowRight } from 'lucide-react';
import { profileApi, Profile } from '@/lib/api/profile';
import { jobsApi } from '@/lib/api/jobs';
import { createClient } from '@/lib/supabase/client';
import { uploadFile } from '@/lib/supabase/storage';
import { authApi } from '@/lib/api/auth';
import { Trash2, AlertTriangle, X } from 'lucide-react';
import LocationPicker from '@/components/maps/LocationPicker';

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
  const [formData, setFormData] = useState<Profile>({
    userId: '', name: '', email: '', phone: '+56 ', birthDate: '',
    bankName: '', accountType: '', accountNumber: '', type: 'TRABAJADOR'
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

  useEffect(() => {
    const fetchProfile = async () => {
      setLoading(true);
      let realUserId = '';
      try {
        const supabase = createClient();
        const { data: { user: supabaseUser } } = await supabase.auth.getUser();
        let initialData = { name: '', email: '', phone: '+56 ' };

        if (supabaseUser) {
          realUserId = supabaseUser.id;
          initialData = { name: supabaseUser.user_metadata?.full_name || '', email: supabaseUser.email || '', phone: '+56 ' };
        } else {
          const storedUser = localStorage.getItem('user_info');
          if (storedUser) {
            const userData = JSON.parse(storedUser);
            realUserId = userData.id?.toString() || '';
            console.log('DEBUG: Loading profile for userId:', realUserId);
            
            if (!realUserId) {
              console.warn('DEBUG: No userId found in user_info');
            }
            initialData = { name: userData.nombre || '', email: userData.email || '', phone: userData.telefono || '+56 ' };
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
            ...res.data, userId: realUserId,
            birthDate: res.data.birthDate || '', name: res.data.name || initialData.name || '',
            email: res.data.email || initialData.email || '', phone: res.data.phone || initialData.phone || '',
            bankName: res.data.bankName || '', accountType: res.data.accountType || '', accountNumber: res.data.accountNumber || ''
          };
          setFormData(p);
          setSavedProfile(p);
          if (res.data.birthDate) setBirthDateLocked(true);
        } else {
          console.log('No se encontró perfil previo, usando datos iniciales.');
          setFormData(prev => ({ ...prev, userId: realUserId, name: initialData.name, email: initialData.email, phone: initialData.phone }));
        }
      } catch (error) { console.error('Error fetching profile:', error); }
      finally { setLoading(false); }

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
    // Validación de fecha de nacimiento
    if (formData.birthDate) {
      const selectedDate = new Date(formData.birthDate);
      const today = new Date();
      if (selectedDate > today) {
        alert('La fecha de nacimiento no puede ser una fecha futura.');
        return;
      }
    }

    setSaving(true);
    try {
      const res = await profileApi.save(formData);
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
        <p className="text-gray-500">Cargando tu perfil...</p>
      </div>
    );
  }

  const hasBankData = savedProfile?.bankName && savedProfile?.accountType && savedProfile?.accountNumber;
  const maskedAccount = savedProfile?.accountNumber ? '****' + savedProfile.accountNumber.slice(-4) : '';

  return (
    <div className="max-w-7xl mx-auto pb-20 px-4">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Mi Perfil</h1>
        <div className="flex gap-2 text-sm text-gray-500 bg-white px-3 py-1 rounded-full border border-gray-100">
          <span className="w-2 h-2 bg-green-500 rounded-full mt-1.5 animate-pulse"></span>
          Perfil Público Activo
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
                    <img src={formData.photoUrl} alt="Profile" className="w-full h-full object-cover" />
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
              <h2 className="text-lg font-bold text-gray-900">{formData.name || 'Sin nombre'}</h2>
              <p className="text-sm text-gray-500">{formData.email}</p>
              <p className="text-xs text-gray-400 mt-1">{formData.phone !== '+56 ' ? formData.phone : ''}</p>
            </CardContent>
          </Card>

          {/* Datos bancarios confirmados */}
          <Card>
            <CardHeader className="border-b bg-gray-50/50 py-3 px-4">
              <h2 className="text-sm font-semibold flex items-center gap-2">
                <CreditCard className="text-primary w-4 h-4" /> Método de Pago
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
                    <p className="text-xs text-green-700 font-medium">✅ Datos bancarios verificados</p>
                  </div>
                </div>
              ) : (
                <p className="text-sm text-gray-400 text-center py-2">Sin datos bancarios aún. Completa la sección de transferencia.</p>
              )}
            </CardContent>
          </Card>

          {/* Historial de postulaciones */}
          <Card>
            <CardHeader className="border-b bg-gray-50/50 py-3 px-4">
              <h2 className="text-sm font-semibold flex items-center gap-2">
                <Clock className="text-primary w-4 h-4" /> Historial de Postulaciones
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
                <p className="text-xs text-gray-400 text-center py-4">No tienes postulaciones recientes.</p>
              )}
              <Link href="/trabajador/postulaciones">
                <button className="w-full text-xs text-primary hover:text-primary-dark flex items-center justify-center gap-1 pt-2">
                  Ver todas <ArrowRight className="w-3 h-3" />
                </button>
              </Link>
            </CardContent>
          </Card>
        </div>

        {/* ═══ CONTENIDO PRINCIPAL ═══ */}
        <div className="lg:col-span-2 space-y-6">

          {/* Información personal */}
          <Card>
            <CardHeader className="border-b bg-gray-50/50">
              <h2 className="text-lg font-semibold flex items-center gap-2">
                <User className="text-primary w-5 h-5" /> Información Personal
              </h2>
            </CardHeader>
            <CardContent className="p-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">Nombre Completo</label>
                  <input id="name" type="text" value={formData.name} onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary" />
                </div>
                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">Sobre mí / Resumen profesional</label>
                  <textarea id="description" rows={3} value={formData.description} onChange={handleInputChange}
                    placeholder="Cuéntanos sobre tu experiencia y habilidades..."
                    className="w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all resize-none" />
                </div>

                {/* MAPA DE GEOLOCALIZACION */}
                <div className="md:col-span-2 pt-4 border-t border-gray-100">
                  <LocationPicker 
                    apiKey={GOOGLE_MAPS_API_KEY}
                    initialLat={formData.latitude}
                    initialLng={formData.longitude}
                    onLocationChange={(lat, lng) => {
                      setFormData(prev => ({ ...prev, latitude: lat, longitude: lng }));
                    }}
                  />
                </div>
                <div>
                  <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">Correo Electrónico</label>
                  <input id="email" type="email" value={formData.email} onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary" />
                </div>
                <div>
                  <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                  <input id="phone" type="text" value={formData.phone} onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary" />
                </div>
                <div>
                  <label htmlFor="birthDate" className="block text-sm font-medium text-gray-700 mb-1 flex items-center gap-1">
                    Fecha de Nacimiento {birthDateLocked && <Lock className="w-3 h-3 text-gray-400" />}
                  </label>
                  <div className="relative">
                    <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <input id="birthDate" type="date" value={formData.birthDate}
                      onChange={handleInputChange} disabled={birthDateLocked}
                      max={new Date().toISOString().split('T')[0]}
                      className={`w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary ${birthDateLocked ? 'bg-gray-100 cursor-not-allowed text-gray-500' : ''}`} />
                  </div>
                  {birthDateLocked && <p className="text-xs text-gray-400 mt-1">La fecha de nacimiento no se puede modificar</p>}
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Mis documentos */}
          <Card>
            <CardHeader className="border-b bg-gray-50/50">
              <h2 className="text-lg font-semibold flex items-center gap-2">
                <FileText className="text-primary w-5 h-5" /> Mis Documentos
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
                      <h3 className="text-sm font-semibold text-gray-900">Currículum Vitae</h3>
                      <p className="text-xs text-gray-500">{formData.cvUrl ? 'Documento cargado' : 'PDF, DOCX hasta 10MB'}</p>
                    </div>
                  </div>
                  {formData.cvUrl && (
                    <div className="mb-3 p-3 bg-white rounded-lg border border-gray-100">
                      {formData.cvUrl.match(/\.(jpg|jpeg|png|gif|webp)/) ? (
                        <img src={formData.cvUrl} alt="CV Preview" className="w-full h-32 object-cover rounded" />
                      ) : (
                        <div className="flex items-center gap-2 text-sm text-gray-600">
                          <FileText className="w-8 h-8 text-red-500" />
                          <div>
                            <p className="font-medium">Documento PDF</p>
                            <a href={formData.cvUrl} target="_blank" rel="noopener noreferrer" className="text-xs text-primary hover:underline flex items-center gap-1">
                              <Eye className="w-3 h-3" /> Ver documento
                            </a>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                  <input type="file" id="cv-upload" className="hidden" accept=".pdf,.doc,.docx,image/*" onChange={(e) => handleFileUpload(e, 'cvUrl')} />
                  <Button variant="ghost" size="sm" disabled={uploading !== null}
                    onClick={() => document.getElementById('cv-upload')?.click()}>
                    <RefreshCw className="w-3 h-3 mr-1" /> {formData.cvUrl ? 'Cambiar archivo' : 'Subir CV'}
                  </Button>
                </div>

                {/* Certificados */}
                <div className={`border-2 rounded-xl p-5 transition-colors ${formData.description ? 'border-primary/30 bg-primary/5' : 'border-dashed border-gray-200'}`}>
                  <div className="flex items-start gap-3 mb-3">
                    <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${formData.description ? 'bg-primary/10' : 'bg-gray-100'}`}>
                      {uploading === 'description' ? <Loader2 className="w-5 h-5 text-primary animate-spin" /> : formData.description ? <CheckCircle className="w-5 h-5 text-primary" /> : <Briefcase className="w-5 h-5 text-gray-400" />}
                    </div>
                    <div className="flex-1">
                      <h3 className="text-sm font-semibold text-gray-900">Cursos y Certificados</h3>
                      <p className="text-xs text-gray-500">{formData.description ? 'Certificado cargado' : 'OS10, SEC u otros'}</p>
                    </div>
                  </div>
                  {formData.description && (
                    <div className="mb-3 p-3 bg-white rounded-lg border border-gray-100">
                      {formData.description.match(/\.(jpg|jpeg|png|gif|webp)/) ? (
                        <img src={formData.description} alt="Cert Preview" className="w-full h-32 object-cover rounded" />
                      ) : (
                        <div className="flex items-center gap-2 text-sm text-gray-600">
                          <FileText className="w-8 h-8 text-blue-500" />
                          <div>
                            <p className="font-medium">Certificado</p>
                            <a href={formData.description} target="_blank" rel="noopener noreferrer" className="text-xs text-primary hover:underline flex items-center gap-1">
                              <Eye className="w-3 h-3" /> Ver documento
                            </a>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                  <input type="file" id="cert-upload" className="hidden" accept=".pdf,.doc,.docx,image/*" onChange={(e) => handleFileUpload(e, 'description')} />
                  <Button variant="ghost" size="sm" disabled={uploading !== null}
                    onClick={() => document.getElementById('cert-upload')?.click()}>
                    <RefreshCw className="w-3 h-3 mr-1" /> {formData.description ? 'Cambiar archivo' : 'Subir Certificado'}
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
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label htmlFor="bankName" className="block text-sm font-medium text-gray-700 mb-1">Banco</label>
                  <select id="bankName" value={formData.bankName} onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary bg-white">
                    <option value="">Seleccionar banco...</option>
                    {BANCOS_CHILE.map(b => <option key={b} value={b}>{b}</option>)}
                  </select>
                </div>
                <div>
                  <label htmlFor="accountType" className="block text-sm font-medium text-gray-700 mb-1">Tipo de Cuenta</label>
                  <select id="accountType" value={formData.accountType} onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-md px-4 py-2 focus:ring-primary focus:border-primary bg-white">
                    <option value="">Seleccionar...</option>
                    <option value="VISTA">Cuenta Vista / RUT</option>
                    <option value="CORRIENTE">Cuenta Corriente</option>
                    <option value="AHORRO">Cuenta de Ahorro</option>
                  </select>
                </div>
                <div>
                  <label htmlFor="accountNumber" className="block text-sm font-medium text-gray-700 mb-1">Número de Cuenta</label>
                  <input id="accountNumber" type="text" value={formData.accountNumber} onChange={handleInputChange}
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
              {saving ? 'Guardando...' : 'Guardar Todos los Cambios'}
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
                    {deleting ? 'Eliminando...' : 'Eliminar Cuenta'}
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
