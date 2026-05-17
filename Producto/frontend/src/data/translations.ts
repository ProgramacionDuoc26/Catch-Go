export type Language = "es" | "en";

export const translations = {
  es: {
    // Settings Drawer
    settingsTitle: "Ajustes del Sistema",
    appearanceSection: "Apariencia",
    themeLabel: "Tono de la aplicación",
    themeLight: "Modo Claro",
    themeDark: "Modo Oscuro",
    fontSizeLabel: "Tamaño de la letra",
    fontSizeSm: "Chico",
    fontSizeMd: "Normal",
    fontSizeLg: "Grande",
    fontSizeXl: "Extra Grande",
    
    preferencesSection: "Preferencias",
    soundLabel: "Efectos de sonido",
    soundDesc: "Reproducir sonidos al recibir notificaciones",
    autoRefreshLabel: "Sincronización en vivo",
    autoRefreshDesc: "Refrescar datos en segundo plano",
    
    accountSection: "Cuenta y Perfil",
    editProfileBtn: "Editar mi Perfil",
    notLoggedIn: "Inicia sesión para editar tu perfil",
    
    resetBtn: "Restablecer Ajustes",
    resetSuccess: "Ajustes restablecidos correctamente",
    closeDrawer: "Cerrar Ajustes",
    
    // Navbar & Common
    jobsAvailable: "Empleos Disponibles",
    myApplications: "Mis Postulaciones",
    myOffers: "Mis Ofertas",
    applicants: "Postulantes",
    whosWho: "¿Quiénes Somos?",
    contact: "Contacto",
    terms: "Términos",
    signOut: "Cerrar Sesión",
    signIn: "Ingresar",
    signUp: "Regístrate",
    
    // Welcome & Messages
    logoutConfirmTitle: "¿Cerrar sesión?",
    logoutConfirmText: "¿Estás seguro de que deseas salir de tu cuenta? Tendrás que volver a ingresar tus credenciales para acceder nuevamente.",
    cancel: "Cancelar",
    
    // Additional features
    recommendedJobs: "Trabajos Recomendados",
    matchPercentage: "Porcentaje de Match",
    location: "Ubicación",
    salary: "Sueldo",
    hours: "Horario",
  },
  en: {
    // Settings Drawer
    settingsTitle: "System Settings",
    appearanceSection: "Appearance",
    themeLabel: "App color theme",
    themeLight: "Light Mode",
    themeDark: "Dark Mode",
    fontSizeLabel: "Font size size",
    fontSizeSm: "Small",
    fontSizeMd: "Normal",
    fontSizeLg: "Large",
    fontSizeXl: "Extra Large",
    
    preferencesSection: "Preferences",
    soundLabel: "Sound effects",
    soundDesc: "Play sound when receiving notifications",
    autoRefreshLabel: "Live Synchronization",
    autoRefreshDesc: "Refresh dashboard data in the background",
    
    accountSection: "Account & Profile",
    editProfileBtn: "Edit my Profile",
    notLoggedIn: "Log in to edit your profile",
    
    resetBtn: "Reset Settings",
    resetSuccess: "Settings successfully reset",
    closeDrawer: "Close Settings",
    
    // Navbar & Common
    jobsAvailable: "Available Jobs",
    myApplications: "My Applications",
    myOffers: "My Offers",
    applicants: "Applicants",
    whosWho: "About Us",
    contact: "Contact",
    terms: "Terms & Conditions",
    signOut: "Sign Out",
    signIn: "Sign In",
    signUp: "Register",
    
    // Welcome & Messages
    logoutConfirmTitle: "Sign Out?",
    logoutConfirmText: "Are you sure you want to sign out? You will need to re-enter your credentials to log in again.",
    cancel: "Cancel",
    
    // Additional features
    recommendedJobs: "Recommended Jobs",
    matchPercentage: "Match Percentage",
    location: "Location",
    salary: "Salary",
    hours: "Hours",
  }
};

export type TranslationKeys = keyof typeof translations.es;
