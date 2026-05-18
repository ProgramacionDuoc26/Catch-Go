"use client";

import React, { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { X, Sun, Moon, Volume2, VolumeX, RefreshCw, User, RotateCcw, Languages, Type, LogOut } from "lucide-react";
import { useSettings, Theme, FontSize } from "@/context/SettingsContext";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import Image from "next/image";
import { createClient } from "@/lib/supabase/client";
import { sanitizeUrl } from "@/lib/api/profile";

interface SettingsDrawerProps {
  isOpen: boolean;
  onClose: () => void;
}

export function SettingsDrawer({ isOpen, onClose }: SettingsDrawerProps) {
  const {
    theme,
    language,
    fontSize,
    soundNotifications,
    autoRefresh,
    setTheme,
    setLanguage,
    setFontSize,
    setSoundNotifications,
    setAutoRefresh,
    t,
    resetSettings,
    isMounted
  } = useSettings();

  const router = useRouter();
  const [userData, setUserData] = useState<any>(null);
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false);

  useEffect(() => {
    if (typeof window !== "undefined") {
      const stored = localStorage.getItem("user_info");
      if (stored) {
        try {
          setUserData(JSON.parse(stored));
        } catch (e) {
          console.error("Error loading user info in settings drawer", e);
        }
      }
    }
  }, [isOpen]);

  if (!isMounted) return null;

  const handleLogout = async () => {
    const supabase = createClient();
    try {
      await supabase.auth.signOut();
      localStorage.clear();
      sessionStorage.clear();
      onClose();
      router.push('/login');
      router.refresh();
      toast.success(language === "en" ? "Logged out successfully" : "Sesión cerrada correctamente");
    } catch (e) {
      console.error('Error logging out in drawer:', e);
      toast.error("Error al cerrar sesión");
    }
  };

  const handleEditProfile = () => {
    onClose();
    if (!userData) {
      router.push("/login");
      return;
    }

    const role = userData.tipo || userData.type;
    if (role === "ADMIN" || role === "SUB_ADMIN") {
      router.push("/admin?tab=admins");
    } else if (role === "EMPRESA") {
      router.push("/empresa/perfil");
    } else {
      router.push("/trabajador/perfil");
    }
  };

  const handleReset = () => {
    resetSettings();
    toast.success(t("resetSuccess"), {
      icon: <RotateCcw className="w-4 h-4 text-emerald-500 animate-spin" />
    });
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 z-[80] bg-black/60 backdrop-blur-sm"
          />

          {/* Drawer Sidebar */}
          <motion.div
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "spring", damping: 25, stiffness: 220 }}
            className="fixed top-0 right-0 h-full w-full sm:max-w-md bg-white dark:bg-slate-900 border-l border-slate-200 dark:border-slate-800 shadow-2xl z-[90] flex flex-col text-slate-800 dark:text-slate-100"
          >
            {/* Header */}
            <div className="p-5 border-b border-slate-200 dark:border-slate-800 flex justify-between items-center bg-slate-50/50 dark:bg-slate-900/50">
              <div className="flex items-center gap-2.5">
                <div className="p-2 bg-primary/10 rounded-lg text-primary">
                  <Languages className="w-5 h-5" />
                </div>
                <h2 className="text-lg font-bold tracking-tight text-slate-900 dark:text-white">
                  {t("settingsTitle")}
                </h2>
              </div>
              <button
                onClick={onClose}
                className="p-2 rounded-full text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-all group"
                aria-label={t("closeDrawer")}
              >
                <X className="w-5 h-5 group-hover:rotate-90 transition-transform duration-200" />
              </button>
            </div>

            {/* Content Body */}
            <div className="flex-1 overflow-y-auto p-6 space-y-6">
              
              {/* Apariencia (Theme & Font Size) */}
              <div className="space-y-4">
                <h3 className="text-xs uppercase font-bold tracking-wider text-slate-400 dark:text-slate-500">
                  {t("appearanceSection")}
                </h3>
                
                {/* Theme Selector */}
                <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-xl space-y-3 border border-slate-100 dark:border-slate-800/60">
                  <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-2">
                    <Sun className="w-4 h-4 text-amber-500" />
                    {t("themeLabel")}
                  </label>
                  
                  <div className="grid grid-cols-2 gap-2 bg-slate-200/60 dark:bg-slate-800 p-1 rounded-lg">
                    <button
                      onClick={() => setTheme("light")}
                      className={`flex items-center justify-center gap-2 py-2 text-xs font-semibold rounded-md transition-all ${
                        theme === "light"
                          ? "bg-white text-primary shadow-sm dark:bg-slate-700 dark:text-white"
                          : "text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200"
                      }`}
                    >
                      <Sun className="w-4 h-4" />
                      {t("themeLight")}
                    </button>
                    <button
                      onClick={() => setTheme("dark")}
                      className={`flex items-center justify-center gap-2 py-2 text-xs font-semibold rounded-md transition-all ${
                        theme === "dark"
                          ? "bg-slate-900 text-white shadow-sm dark:bg-primary dark:text-white"
                          : "text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200"
                      }`}
                    >
                      <Moon className="w-4 h-4" />
                      {t("themeDark")}
                    </button>
                  </div>
                </div>

                {/* Font Size Selector */}
                <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-xl space-y-3 border border-slate-100 dark:border-slate-800/60">
                  <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-2">
                    <Type className="w-4 h-4 text-blue-500" />
                    {t("fontSizeLabel")}
                  </label>
                  
                  <div className="grid grid-cols-4 gap-1.5 bg-slate-200/60 dark:bg-slate-800 p-1.5 rounded-lg">
                    {(["sm", "md", "lg", "xl"] as FontSize[]).map((size) => {
                      const labels = {
                        sm: t("fontSizeSm"),
                        md: t("fontSizeMd"),
                        lg: t("fontSizeLg"),
                        xl: t("fontSizeXl"),
                      };
                      return (
                        <button
                          key={size}
                          onClick={() => setFontSize(size)}
                          className={`py-1.5 text-[10px] font-bold rounded transition-all flex flex-col items-center justify-center ${
                            fontSize === size
                              ? "bg-primary text-white shadow-sm dark:bg-primary"
                              : "text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200 hover:bg-white/40 dark:hover:bg-slate-700/40"
                          }`}
                        >
                          <span className={`leading-none mb-1 font-bold ${
                            size === "sm" ? "text-xs" : size === "md" ? "text-sm" : size === "lg" ? "text-base" : "text-lg"
                          }`}>A</span>
                          <span className="text-[8px] uppercase tracking-wide opacity-80">{labels[size]}</span>
                        </button>
                      );
                    })}
                  </div>
                </div>
              </div>

              {/* Preferencias (Language & Notifications) */}
              <div className="space-y-4">
                <h3 className="text-xs uppercase font-bold tracking-wider text-slate-400 dark:text-slate-500">
                  {t("preferencesSection")}
                </h3>

                {/* Language Selector */}
                <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-xl space-y-3 border border-slate-100 dark:border-slate-800/60">
                  <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-2">
                    <Languages className="w-4 h-4 text-emerald-500" />
                    Idioma / Language
                  </label>
                  
                  <div className="flex bg-slate-200/60 dark:bg-slate-800 p-1 rounded-lg">
                    <button
                      onClick={() => setLanguage("es")}
                      className={`flex-1 py-2 text-xs font-bold rounded transition-all ${
                        language === "es"
                          ? "bg-white text-slate-900 shadow dark:bg-slate-700 dark:text-white"
                          : "text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200"
                      }`}
                    >
                      🇪🇸 Español
                    </button>
                    <button
                      onClick={() => setLanguage("en")}
                      className={`flex-1 py-2 text-xs font-bold rounded transition-all ${
                        language === "en"
                          ? "bg-white text-slate-900 shadow dark:bg-slate-700 dark:text-white"
                          : "text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200"
                      }`}
                    >
                      🇺🇸 English
                    </button>
                  </div>
                </div>

                {/* Sounds and Sync Toggles */}
                <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-xl space-y-4 border border-slate-100 dark:border-slate-800/60">
                  
                  {/* Sound Toggle */}
                  <div className="flex items-start justify-between gap-4">
                    <div className="space-y-0.5">
                      <div className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                        {soundNotifications ? <Volume2 className="w-4 h-4 text-indigo-500" /> : <VolumeX className="w-4 h-4 text-slate-400" />}
                        {t("soundLabel")}
                      </div>
                      <p className="text-[10px] text-slate-400 dark:text-slate-500 leading-normal">
                        {t("soundDesc")}
                      </p>
                    </div>
                    <button
                      onClick={() => setSoundNotifications(!soundNotifications)}
                      className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none ${
                        soundNotifications ? "bg-primary" : "bg-slate-300 dark:bg-slate-700"
                      }`}
                    >
                      <span
                        className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
                          soundNotifications ? "translate-x-5" : "translate-x-0"
                        }`}
                      />
                    </button>
                  </div>

                  <hr className="border-slate-200 dark:border-slate-800/80" />

                  {/* Auto Refresh Toggle */}
                  <div className="flex items-start justify-between gap-4">
                    <div className="space-y-0.5">
                      <div className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                        <RefreshCw className={`w-4 h-4 text-amber-500 ${autoRefresh ? "animate-spin" : ""}`} style={{ animationDuration: "6s" }} />
                        {t("autoRefreshLabel")}
                      </div>
                      <p className="text-[10px] text-slate-400 dark:text-slate-500 leading-normal">
                        {t("autoRefreshDesc")}
                      </p>
                    </div>
                    <button
                      onClick={() => setAutoRefresh(!autoRefresh)}
                      className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none ${
                        autoRefresh ? "bg-primary" : "bg-slate-300 dark:bg-slate-700"
                      }`}
                    >
                      <span
                        className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
                          autoRefresh ? "translate-x-5" : "translate-x-0"
                        }`}
                      />
                    </button>
                  </div>

                </div>
              </div>

              {/* Cuenta (Edit Profile Shortcut) */}
              <div className="space-y-4">
                <h3 className="text-xs uppercase font-bold tracking-wider text-slate-400 dark:text-slate-500">
                  {t("accountSection")}
                </h3>

                <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-xl border border-slate-100 dark:border-slate-800/60">
                  {userData ? (
                    <div className="space-y-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full border border-primary/20 bg-primary/5 flex items-center justify-center overflow-hidden">
                          {userData.foto || userData.photoUrl ? (
                            <Image
                              src={sanitizeUrl(userData.foto || userData.photoUrl) || ""}
                              alt="Avatar"
                              width={40}
                              height={40}
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <User className="w-5 h-5 text-primary" />
                          )}
                        </div>
                        <div className="flex-grow min-w-0">
                          <h4 className="text-xs font-bold truncate text-slate-800 dark:text-white">
                            {userData.nombre || userData.name || "Usuario"}
                          </h4>
                          <p className="text-[9px] uppercase tracking-wider font-extrabold text-primary">
                            {userData.tipo || userData.type || "TRABAJADOR"}
                          </p>
                        </div>
                      </div>
                      
                      <button
                        onClick={handleEditProfile}
                        className="w-full py-2.5 bg-primary text-white rounded-lg hover:bg-primary-dark font-bold text-xs shadow-sm transition-all flex items-center justify-center gap-2 mb-2"
                      >
                        <User className="w-4 h-4" />
                        {(userData.tipo === 'ADMIN' || userData.tipo === 'SUB_ADMIN' || userData.type === 'ADMIN' || userData.type === 'SUB_ADMIN') ? "Cuentas Admin" : t("editProfileBtn")}
                      </button>
                      <button
                        onClick={() => setShowLogoutConfirm(true)}
                        className="w-full py-2.5 bg-red-50 hover:bg-red-100 dark:bg-red-950/20 dark:hover:bg-red-950/40 text-red-600 dark:text-red-400 rounded-lg font-bold text-xs transition-all flex items-center justify-center gap-2"
                      >
                        <LogOut className="w-4 h-4" />
                        {t("signOut") || "Cerrar Sesión"}
                      </button>
                    </div>
                  ) : (
                    <div className="text-center py-4 space-y-3">
                      <div className="w-12 h-12 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center mx-auto">
                        <User className="w-6 h-6 text-slate-400" />
                      </div>
                      <p className="text-xs text-slate-500 dark:text-slate-400">
                        {t("notLoggedIn")}
                      </p>
                      <button
                        onClick={() => {
                          onClose();
                          router.push("/login");
                        }}
                        className="px-4 py-1.5 bg-slate-200 hover:bg-slate-300 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 rounded-lg text-xs font-bold transition-all"
                      >
                        {t("signIn")}
                      </button>
                    </div>
                  )}
                </div>
              </div>

            </div>

            {/* Footer */}
            <div className="p-4 border-t border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/50 flex gap-3">
              <button
                onClick={handleReset}
                className="flex-grow py-2.5 border border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 font-bold text-xs rounded-lg transition-all flex items-center justify-center gap-1.5"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                {t("resetBtn")}
              </button>
              <button
                onClick={onClose}
                className="px-6 py-2.5 bg-slate-900 dark:bg-slate-800 hover:bg-slate-800 dark:hover:bg-slate-700 text-white font-bold text-xs rounded-lg transition-all"
              >
                {t("closeDrawer")}
              </button>
            </div>

            {/* Modal de Confirmación de Cierre de Sesión dentro del Drawer */}
            {showLogoutConfirm && (
              <div className="absolute inset-0 z-[100] bg-black/60 backdrop-blur-sm flex items-center justify-center p-6">
                <div className="bg-white dark:bg-slate-800 rounded-xl shadow-2xl p-6 space-y-4 max-w-xs w-full text-center animate-in zoom-in-95 duration-200 border border-slate-100 dark:border-slate-700">
                  <div className="flex justify-center text-red-600 mb-2">
                    <div className="p-3 bg-red-50 dark:bg-red-950/20 rounded-full">
                      <LogOut className="w-8 h-8" />
                    </div>
                  </div>
                  <h4 className="text-base font-bold text-slate-900 dark:text-white leading-snug">
                    ¿Seguro que deseas cerrar sesión?
                  </h4>
                  <p className="text-xs text-slate-500 dark:text-slate-400">
                    Tendrás que ingresar tus credenciales nuevamente para acceder.
                  </p>
                  <div className="flex gap-3 pt-2">
                    <button
                      onClick={() => setShowLogoutConfirm(false)}
                      className="flex-1 py-2 border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800/60 rounded-lg text-xs font-semibold text-slate-600 dark:text-slate-400 transition-colors"
                    >
                      Cancelar
                    </button>
                    <button
                      onClick={handleLogout}
                      className="flex-1 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-xs font-semibold shadow-md shadow-red-200 dark:shadow-none transition-colors"
                    >
                      Cerrar Sesión
                    </button>
                  </div>
                </div>
              </div>
            )}
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}
