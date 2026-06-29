import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { NotificationBell } from '@/components/layout/NotificationBell';

// Simular enrutador de Next.js
const mockPush = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}));

// Mockear el contexto de notificaciones
const mockNotifications = [
  {
    id: 'notif-1',
    title: 'Nuevo Turno Asignado',
    message: 'Se te ha asignado el turno en Constructora Alfa.',
    type: 'success',
    read: false,
    timestamp: '2026-06-14T20:00:00.000Z',
    link: '/trabajador/postulaciones',
  },
  {
    id: 'notif-2',
    title: 'Pago Recibido',
    message: 'Tu pago de honorarios ha sido verificado.',
    type: 'info',
    read: true,
    timestamp: '2026-06-14T19:00:00.000Z',
  },
];

const mockMarkAsRead = vi.fn();
const mockClearAll = vi.fn();

vi.mock('@/context/NotificationContext', () => ({
  useNotifications: () => ({
    notifications: mockNotifications,
    unreadCount: 1,
    markAsRead: mockMarkAsRead,
    clearAll: mockClearAll,
  }),
}));

// Desactivar animaciones de framer-motion para simplificar las pruebas unitarias
vi.mock('framer-motion', () => ({
  motion: {
    div: ({ children, ...props }: any) => <div {...props}>{children}</div>,
  },
  AnimatePresence: ({ children }: any) => <>{children}</>,
}));

describe('NotificationBell Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // Escenario 1: Mostrar el número correcto de alertas pendientes en la campana
  it('dibuja la campana de notificaciones y muestra el badge con el número de alertas no leídas', () => {
    render(<NotificationBell />);

    // Verificar presencia del botón de la campana
    const bellBtn = screen.getByRole('button');
    expect(bellBtn).toBeInTheDocument();

    // Debe mostrar un badge con el número "1" (notif-1 no leída)
    const badge = screen.getByText('1');
    expect(badge).toBeInTheDocument();
    expect(badge.className).toContain('bg-red-600');
  });

  // Escenario 2: Despliegue y colapso del menú de notificaciones
  it('abre el menú desplegable al hacer clic en la campana y muestra el listado de notificaciones', () => {
    render(<NotificationBell />);

    // Por defecto el menú está oculto
    expect(screen.queryByText('Notificaciones')).not.toBeInTheDocument();

    // Simular clic en la campana
    const bellBtn = screen.getByRole('button');
    fireEvent.click(bellBtn);

    // Ahora debe ser visible el encabezado del panel y la cantidad de notificaciones
    expect(screen.getByText('Notificaciones')).toBeInTheDocument();
    expect(screen.getByText('1 nuevas')).toBeInTheDocument();

    // Validar que se muestren las notificaciones del mock
    expect(screen.getByText(/Nuevo Turno Asignado/i)).toBeInTheDocument();
    expect(screen.getByText(/Se te ha asignado el turno en Constructora Alfa./i)).toBeInTheDocument();
    expect(screen.getByText(/Pago Recibido/i)).toBeInTheDocument();
  });

  // Escenario 3: Limpiar todas las notificaciones
  it('llama al callback clearAll cuando se presiona el botón "Limpiar"', () => {
    render(<NotificationBell />);

    // Abrir menú
    const bellBtn = screen.getByRole('button');
    fireEvent.click(bellBtn);

    // Buscar y presionar botón de limpieza
    const clearBtn = screen.getByText('Limpiar');
    expect(clearBtn).toBeInTheDocument();
    fireEvent.click(clearBtn);

    // Debe invocarse la función clearAll del contexto
    expect(mockClearAll).toHaveBeenCalledTimes(1);
  });

  // Escenario 4: Interactuar con una notificación (Marcar como leída y navegar)
  it('marca una notificación como leída y redirige al enlace cuando se hace clic en ella', () => {
    render(<NotificationBell />);

    // Abrir menú
    const bellBtn = screen.getByRole('button');
    fireEvent.click(bellBtn);

    // Hacer clic en la primera notificación (que tiene link)
    const firstNotif = screen.getByText(/Nuevo Turno Asignado/i);
    fireEvent.click(firstNotif);

    // Debe llamar a markAsRead con el ID de la notificación y redirigir
    expect(mockMarkAsRead).toHaveBeenCalledWith('notif-1');
    expect(mockPush).toHaveBeenCalledWith('/trabajador/postulaciones');
  });

  // Escenario 5: Marcar como leída sin navegar usando el botón interno
  it('llama a markAsRead al hacer clic en "Marcar como leída" sin navegar', () => {
    render(<NotificationBell />);

    // Abrir menú
    const bellBtn = screen.getByRole('button');
    fireEvent.click(bellBtn);

    // Buscar el botón flotante "Marcar como leída" que aparece al pasar el cursor (hover)
    const markAsReadBtn = screen.getByText('Marcar como leída');
    expect(markAsReadBtn).toBeInTheDocument();
    fireEvent.click(markAsReadBtn);

    // Debe marcar como leída y detener la propagación del evento
    expect(mockMarkAsRead).toHaveBeenCalledWith('notif-1');
    expect(mockPush).not.toHaveBeenCalled();
  });
});
