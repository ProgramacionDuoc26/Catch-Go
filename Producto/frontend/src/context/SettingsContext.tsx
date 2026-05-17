"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { translations, Language, TranslationKeys } from "../data/translations";

export type Theme = "light" | "dark";
export type FontSize = "sm" | "md" | "lg" | "xl";

interface SettingsContextType {
  theme: Theme;
  language: Language;
  fontSize: FontSize;
  soundNotifications: boolean;
  autoRefresh: boolean;
  setTheme: (theme: Theme) => void;
  setLanguage: (lang: Language) => void;
  setFontSize: (size: FontSize) => void;
  setSoundNotifications: (enabled: boolean) => void;
  setAutoRefresh: (enabled: boolean) => void;
  t: (key: TranslationKeys) => string;
  resetSettings: () => void;
  isMounted: boolean;
}

const SettingsContext = createContext<SettingsContextType | undefined>(undefined);

export function SettingsProvider({ children }: { children: React.ReactNode }) {
  const [theme, setThemeState] = useState<Theme>("light");
  const [language, setLanguageState] = useState<Language>("es");
  const [fontSize, setFontSizeState] = useState<FontSize>("sm");
  const [soundNotifications, setSoundNotificationsState] = useState<boolean>(true);
  const [autoRefresh, setAutoRefreshState] = useState<boolean>(true);
  const [isMounted, setIsMounted] = useState(false);

  // Load from localStorage on mount
  useEffect(() => {
    setIsMounted(true);
    
    const savedTheme = localStorage.getItem("catchgo_theme") as Theme;
    const savedLanguage = localStorage.getItem("catchgo_lang") as Language;
    const savedFontSize = localStorage.getItem("catchgo_font_size") as FontSize;
    const savedSound = localStorage.getItem("catchgo_sound");
    const savedAutoRefresh = localStorage.getItem("catchgo_auto_refresh");

    if (savedTheme) setThemeState(savedTheme);
    if (savedLanguage) setLanguageState(savedLanguage);
    if (savedFontSize) setFontSizeState(savedFontSize);
    if (savedSound !== null) setSoundNotificationsState(savedSound === "true");
    if (savedAutoRefresh !== null) setAutoRefreshState(savedAutoRefresh === "true");
  }, []);

  // Apply Theme
  useEffect(() => {
    if (!isMounted) return;
    
    const root = document.documentElement;
    if (theme === "dark") {
      root.classList.add("dark");
    } else {
      root.classList.remove("dark");
    }
    
    localStorage.setItem("catchgo_theme", theme);
  }, [theme, isMounted]);

  // Apply Font Size
  useEffect(() => {
    if (!isMounted) return;

    const root = document.documentElement;
    if (fontSize === "sm") {
      root.style.fontSize = ""; // reset to CSS default (responsive 14px/15px)
    } else if (fontSize === "md") {
      root.style.fontSize = "16px";
    } else if (fontSize === "lg") {
      root.style.fontSize = "18px";
    } else if (fontSize === "xl") {
      root.style.fontSize = "20px";
    }

    localStorage.setItem("catchgo_font_size", fontSize);
  }, [fontSize, isMounted]);

  // Handle other settings persistence
  const setTheme = (val: Theme) => setThemeState(val);
  const setLanguage = (val: Language) => {
    setLanguageState(val);
    localStorage.setItem("catchgo_lang", val);
  };
  const setFontSize = (val: FontSize) => setFontSizeState(val);
  
  const setSoundNotifications = (val: boolean) => {
    setSoundNotificationsState(val);
    localStorage.setItem("catchgo_sound", String(val));
    if (val && typeof window !== "undefined") {
      // Play a subtle notification tick sound as user feedback
      try {
        const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
        const oscillator = audioCtx.createOscillator();
        const gainNode = audioCtx.createGain();
        oscillator.connect(gainNode);
        gainNode.connect(audioCtx.destination);
        oscillator.type = "sine";
        oscillator.frequency.setValueAtTime(800, audioCtx.currentTime); // A nice high pitch ding
        gainNode.gain.setValueAtTime(0.05, audioCtx.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.1);
        oscillator.start();
        oscillator.stop(audioCtx.currentTime + 0.15);
      } catch (e) {
        console.warn("Audio Context sound failed to play", e);
      }
    }
  };

  const setAutoRefresh = (val: boolean) => {
    setAutoRefreshState(val);
    localStorage.setItem("catchgo_auto_refresh", String(val));
  };

  const resetSettings = () => {
    setThemeState("light");
    setLanguageState("es");
    setFontSizeState("sm");
    setSoundNotificationsState(true);
    setAutoRefreshState(true);
    
    localStorage.removeItem("catchgo_theme");
    localStorage.removeItem("catchgo_lang");
    localStorage.removeItem("catchgo_font_size");
    localStorage.removeItem("catchgo_sound");
    localStorage.removeItem("catchgo_auto_refresh");
  };

  // Translation function
  const t = (key: TranslationKeys): string => {
    const activeLang = language === "en" || language === "es" ? language : "es";
    return translations[activeLang][key] || translations["es"][key] || key;
  };

  return (
    <SettingsContext.Provider
      value={{
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
      }}
    >
      {children}
    </SettingsContext.Provider>
  );
}

export function useSettings() {
  const context = useContext(SettingsContext);
  if (context === undefined) {
    throw new Error("useSettings must be used within a SettingsProvider");
  }
  return context;
}
