import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { Navbar } from './Navbar';

// Simular dependencias de enrutamiento y estado de Next.js
const mockPush = vi.fn();
const mockRefresh = vi.fn();
let currentPathname = '/';

vi.mock('next/navigation', () => ({
  usePathname: () => currentPathname,
  useRouter: () => ({
    push: mockPush,
    refresh: mockRefresh,
  }),
}));

// Simular el cliente de Supabase
vi.mock('@/lib/supabase/client', () => ({
  createClient: () => ({
    auth: {
      signOut: vi.fn().mockResolvedValue({ error: null }),
    },
  }),
}));

// Simular el contexto de traducción y ajustes
vi.mock('@/context/SettingsContext', () => ({
  useSettings: () => ({
    t: (key: string) => {
      const dict: Record<string, string> = {
        whosWho: "Quiénes Somos",
        contact: "Contacto",
        terms: "Términos y Condiciones",
        jobsAvailable: "Buscar Trabajo",
        myOffers: "Mis Ofertas",
        myApplications: "Mis Postulaciones",
        applicants: "Postulantes",
        settingsTitle: "Ajustes",
        signOut: "Cerrar Sesión",
        signIn: "Iniciar Sesión",
        signUp: "Registrarse",
        logoutConfirmTitle: "¿Cerrar Sesión?",
        logoutConfirmText: "¿Seguro que deseas salir?",
        cancel: "Cancelar",
      };
      return dict[key] || key;
    },
  }),
}));

// Simular el componente de la Campana de Notificaciones para aislar la prueba del Navbar
vi.mock('./NotificationBell', () => ({
  NotificationBell: () => <div data-testid="notification-bell">Campana Mock</div>,
}));

// Simular el Drawer de Ajustes
vi.mock('./SettingsDrawer', () => ({
  SettingsDrawer: ({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) => 
    isOpen ? (
      <div data-testid="settings-drawer">
        Drawer Ajustes Abierto
        <button onClick={onClose}>Cerrar Ajustes</button>
      </div>
    ) : null,
}));

// Simular helper de sanitización de URL de perfil
vi.mock('@/lib/api/profile', () => ({
  sanitizeUrl: (url: string) => url,
}));

describe('Navbar Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    currentPathname = '/';
  });

  // Escenario 1: Renderizado básico para usuarios sin sesión
  it('renderiza opciones públicas de navegación cuando el usuario no ha iniciado sesión', () => {
    render(<Navbar />);

    // Verificar enlaces principales de la Landing Page
    expect(screen.getByText('Quiénes Somos')).toBeInTheDocument();
    expect(screen.getByText('Contacto')).toBeInTheDocument();
    expect(screen.getByText('Términos y Condiciones')).toBeInTheDocument();

    // Verificar presencia de botones de iniciar sesión y registro
    expect(screen.getByText('Iniciar Sesión')).toBeInTheDocument();
    expect(screen.getByText('Registrarse')).toBeInTheDocument();

    // No debe renderizar la campana ni opciones de dashboard
    expect(screen.queryByTestId('notification-bell')).not.toBeInTheDocument();
  });

  // Escenario 2: Renderizado para el rol TRABAJADOR con sesión activa
  it('muestra enlaces específicos del trabajador y campana de notificaciones al iniciar sesión', () => {
    // Configurar información de sesión simulada en localStorage
    const workerInfo = {
      id: 'worker-1',
      nombre: 'Juan Pérez',
      tipo: 'TRABAJADOR',
      foto: null,
    };
    localStorage.setItem('user_info', JSON.stringify(workerInfo));
    currentPathname = '/trabajador/ofertas';

    render(<Navbar />);

    // Verificar opciones del trabajador en el panel de navegación
    expect(screen.getByText('Buscar Trabajo')).toBeInTheDocument();
    expect(screen.getByText('Mis Postulaciones')).toBeInTheDocument();
    expect(screen.getByText('Trabajador')).toBeInTheDocument();
    expect(screen.getByText('Juan Pérez')).toBeInTheDocument();

    // Debe mostrar la campana de notificaciones
    expect(screen.getByTestId('notification-bell')).toBeInTheDocument();
  });

  // Escenario 3: Renderizado para el rol EMPRESA con sesión activa
  it('muestra enlaces específicos de la empresa al iniciar sesión', () => {
    // Configurar información de empresa en localStorage
    const companyInfo = {
      id: 'company-1',
      nombre: 'Constructora Alfa',
      tipo: 'EMPRESA',
    };
    localStorage.setItem('user_info', JSON.stringify(companyInfo));
    currentPathname = '/empresa/ofertas';

    render(<Navbar />);

    // Verificar enlaces de empresa
    expect(screen.getByText('Mis Ofertas')).toBeInTheDocument();
    expect(screen.getByText('Postulantes')).toBeInTheDocument();
    expect(screen.getByText('Empresa')).toBeInTheDocument();
    expect(screen.getByText('Constructora Alfa')).toBeInTheDocument();
  });

  // Escenario 4: Renderizado para el rol ADMIN con sesión activa
  it('muestra etiqueta de administrador cuando el rol es ADMIN', () => {
    const adminInfo = {
      id: 'admin-1',
      nombre: 'Administrador General',
      tipo: 'ADMIN',
    };
    localStorage.setItem('user_info', JSON.stringify(adminInfo));
    currentPathname = '/admin';

    render(<Navbar />);

    // Debe mostrar etiqueta de Admin y su nombre
    expect(screen.getByText('Admin')).toBeInTheDocument();
    expect(screen.getByText('Administrador General')).toBeInTheDocument();

    // No debe mostrar los links de Trabajador o Empresa
    expect(screen.queryByText('Buscar Trabajo')).not.toBeInTheDocument();
    expect(screen.queryByText('Mis Ofertas')).not.toBeInTheDocument();
  });

  // Escenario 5: Interacción y confirmación de Cierre de Sesión
  it('despliega el modal de confirmación de salida al presionar el botón de cierre de sesión', async () => {
    const workerInfo = {
      id: 'worker-1',
      nombre: 'Juan Pérez',
      tipo: 'TRABAJADOR',
    };
    localStorage.setItem('user_info', JSON.stringify(workerInfo));
    currentPathname = '/trabajador/ofertas';

    render(<Navbar />);

    // Buscar y presionar botón de logout
    const logoutBtn = screen.getByTitle('Cerrar Sesión');
    fireEvent.click(logoutBtn);

    // Debe abrirse el modal de confirmación
    expect(screen.getByText('¿Cerrar Sesión?')).toBeInTheDocument();
    expect(screen.getByText('¿Seguro que deseas salir?')).toBeInTheDocument();

    // Presionar el botón de confirmación en el modal
    const modalButtons = screen.getAllByRole('button', { name: 'Cerrar Sesión' });
    const confirmBtn = modalButtons[modalButtons.length - 1];
    fireEvent.click(confirmBtn);

    // Esperar a que se limpien las claves de localStorage y redireccione
    await waitFor(() => {
      expect(localStorage.getItem('auth_token')).toBeNull();
      expect(localStorage.getItem('user_info')).toBeNull();
      expect(mockPush).toHaveBeenCalledWith('/login');
    });
  });
});
