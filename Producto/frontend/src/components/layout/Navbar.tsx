"use client";

import React, { useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { User, LogIn, LayoutDashboard, LogOut, Settings } from "lucide-react";
import { usePathname, useRouter } from 'next/navigation';
import { createClient } from '@/lib/supabase/client';
import { Button } from "@/components/ui/Button";
import { useSettings } from "@/context/SettingsContext";
import { SettingsDrawer } from "./SettingsDrawer";

import { NotificationBell } from "./NotificationBell";

export function Navbar() {
  const pathname = usePathname();
  const router = useRouter();
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false);
  const [showSettings, setShowSettings] = useState(false);
  const [hasSession, setHasSession] = useState(false);
  const [userData, setUserData] = useState<any>(null);
  const supabase = createClient();
  const { t } = useSettings();

  React.useEffect(() => {
    const session = localStorage.getItem('user_info');
    if (session) {
      try {
        setUserData(JSON.parse(session));
        setHasSession(true);
      } catch (e) {
        setHasSession(false);
      }
    } else {
      setHasSession(false);
      setUserData(null);
    }
  }, [pathname]);

  const isDashboard = hasSession && (pathname.includes('/trabajador') || pathname.includes('/empresa') || pathname.includes('/admin'));
  
  // Detectar rol tanto por URL como por datos de usuario
  const userRole = userData?.tipo || userData?.type;
  const isTrabajador = pathname.includes('/trabajador') || userRole === 'TRABAJADOR';
  const isEmpresa = pathname.includes('/empresa') || userRole === 'EMPRESA';
  const isAdmin = pathname.includes('/admin') || userRole === 'ADMIN';

  const handleLogout = async () => {
    try {
      await supabase.auth.signOut();
      localStorage.removeItem('auth_token');
      localStorage.removeItem('user_info');
      router.push('/login');
      router.refresh();
    } catch (error) {
      console.error('Error logging out:', error);
    } finally {
      setShowLogoutConfirm(false);
    }
  };

  return (
    <>
      <header className="bg-surface border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <Link href="/" className="flex items-center gap-2">
              <div className="relative h-8 md:h-10 w-32">
                <Image 
                  src="/logo.png" 
                  alt="Catch&Go logo" 
                  fill
                  style={{ objectFit: 'contain' }}
                  priority
                  sizes="128px"
                />
              </div>
            </Link>
          </div>
          
          <nav className="hidden md:flex space-x-8">
            {!isDashboard ? (
              <>
                <Link href="/#quienes-somos" className="text-primary-dark font-medium hover:text-primary transition-colors">
                  {t("whosWho")}
                </Link>
                <Link href="/#contacto" className="text-primary-dark font-medium hover:text-primary transition-colors">
                  {t("contact")}
                </Link>
                <Link href="/terminos" className="text-primary-dark font-medium hover:text-primary transition-colors">
                  {t("terms")}
                </Link>
              </>
            ) : (
              <>
                {!isAdmin && (
                  <>
                    <Link 
                      href={isTrabajador ? "/trabajador/ofertas" : "/empresa/ofertas"} 
                      className="text-primary-dark font-medium hover:text-primary transition-colors flex items-center gap-1"
                    >
                      <LayoutDashboard size={18} />
                      {isTrabajador ? t("jobsAvailable") : t("myOffers")}
                    </Link>
                    <Link href={isTrabajador ? "/trabajador/postulaciones" : "/empresa/candidatos"} className="text-primary-dark font-medium hover:text-primary transition-colors">
                      {isTrabajador ? t("myApplications") : t("applicants")}
                    </Link>
                    <Link href={isTrabajador ? "/trabajador/suscripcion" : "/empresa/suscripcion"} className="text-amber-600 font-medium hover:text-amber-700 transition-colors flex items-center gap-1">
                      ⭐ Premium
                    </Link>
                  </>
                )}
              </>
            )}
          </nav>

          <div className="flex items-center space-x-4">
            {isDashboard ? (
              <>
                <button
                  onClick={() => setShowLogoutConfirm(true)}
                  className="flex items-center gap-2 text-text-muted hover:text-red-600 transition-colors font-medium text-sm mr-2"
                >
                  <LogOut className="w-4 h-4" />
                  <span className="hidden lg:inline">{t("signOut")}</span>
                </button>
                
                <NotificationBell />

                <button
                  onClick={() => setShowSettings(true)}
                  className="p-2 text-text-muted hover:text-primary transition-colors hover:bg-slate-100 dark:hover:bg-slate-800 rounded-full"
                  title={t("settingsTitle")}
                >
                  <Settings className="w-5 h-5" />
                </button>

                <div
                  className="flex items-center gap-3 bg-white text-text-main pl-1.5 pr-4 py-1.5 rounded-full font-medium border border-gray-200 shadow-sm cursor-default"
                >
                  <div className="relative w-8 h-8 rounded-full overflow-hidden border border-primary/20 bg-gray-100 flex items-center justify-center">
                    {userData?.foto || userData?.photoUrl ? (
                      <Image 
                        src={userData.foto || userData.photoUrl} 
                        alt="Profile" 
                        fill
                        className="object-cover"
                        sizes="32px"
                      />
                    ) : (
                      <User className="w-5 h-5 text-primary" />
                    )}
                  </div>
                  <div className="flex flex-col items-start">
                    <span className="text-[9px] uppercase font-bold text-primary tracking-wider leading-none mb-0.5">
                      {isAdmin ? "Admin" : (isTrabajador ? "Trabajador" : "Empresa")}
                    </span>
                    <span className="text-sm text-slate-700 font-bold hidden sm:inline leading-none">
                      {userData?.nombre || userData?.name || "Usuario"}
                    </span>
                  </div>
                </div>
              </>
            ) : (
              <>
                <button
                  onClick={() => setShowSettings(true)}
                  className="p-2 text-text-muted hover:text-primary transition-colors hover:bg-slate-100 dark:hover:bg-slate-800 rounded-full"
                  title={t("settingsTitle")}
                >
                  <Settings className="w-5 h-5" />
                </button>
                <Link
                  href="/login"
                  className="hidden md:flex items-center gap-2 text-text-main hover:text-primary font-medium"
                >
                  <LogIn className="w-5 h-5" />
                  {t("signIn")}
                </Link>
                <Link
                  href="/register"
                  className="bg-primary text-white px-4 py-2 rounded-md hover:bg-primary-dark transition-colors font-semibold flex items-center gap-2 min-h-[44px]"
                >
                  <User className="w-5 h-5" />
                  {t("signUp")}
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
    
    {/* Modal de Confirmación de Cierre de Sesión */}
    {showLogoutConfirm && (
      <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
        <div className="bg-white rounded-xl shadow-2xl max-w-md w-full p-6 space-y-4 transform animate-in zoom-in-95 duration-200">
          <div className="flex items-center gap-3 text-red-600">
            <div className="p-2 bg-red-50 rounded-full">
              <LogOut className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-bold">{t("logoutConfirmTitle")}</h3>
          </div>
          <p className="text-gray-600">
            {t("logoutConfirmText")}
          </p>
          <div className="flex gap-3 pt-2">
            <Button 
              variant="outline" 
              className="flex-1" 
              onClick={() => setShowLogoutConfirm(false)}
            >
              {t("cancel")}
            </Button>
            <Button 
              variant="primary" 
              className="flex-1 bg-red-600 hover:bg-red-700 border-red-600" 
              onClick={handleLogout}
            >
              {t("signOut")}
            </Button>
          </div>
        </div>
      </div>
    )}

    {/* Settings Sliding Drawer */}
    <SettingsDrawer isOpen={showSettings} onClose={() => setShowSettings(false)} />
  </>
  );
}
