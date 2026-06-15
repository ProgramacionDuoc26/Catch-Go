import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import LoginPage from './page';

// Simular el router de Next.js
const mockPush = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}));

// Simular el cliente de Supabase (importación dinámica)
const mockSignOut = vi.fn().mockResolvedValue({ error: null });
vi.mock('@/lib/supabase/client', () => ({
  createClient: () => ({
    auth: {
      signOut: mockSignOut,
    },
  }),
}));

// Simular el servicio de API de autenticación (importación dinámica)
const mockLogin = vi.fn();
vi.mock('@/lib/api/auth', () => ({
  authApi: {
    login: mockLogin,
  },
}));

// Desactivar animaciones de framer-motion en la suite de pruebas
vi.mock('framer-motion', () => ({
  motion: {
    div: ({ children, ...props }: any) => <div {...props}>{children}</div>,
    button: ({ children, ...props }: any) => <button {...props}>{children}</button>,
  },
  AnimatePresence: ({ children }: any) => <>{children}</>,
}));

describe('LoginPage Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  // Escenario 1: Renderizado correcto de campos
  it('renderiza el formulario con campos de correo, contraseña y botón de ingreso', () => {
    render(<LoginPage />);

    // Verificar que los inputs y etiquetas existan
    expect(screen.getByLabelText(/Correo Electrónico/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Contraseña/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Ingresar ahora/i })).toBeInTheDocument();
  });

  // Escenario 2: Validaciones de campos vacíos antes de enviar
  it('muestra mensajes de validación si el usuario intenta enviar campos vacíos', () => {
    render(<LoginPage />);

    // Presionar botón de ingreso
    const submitBtn = screen.getByRole('button', { name: /Ingresar ahora/i });
    fireEvent.click(submitBtn);

    // Debe pintar mensajes de campos obligatorios
    expect(screen.getByText('El correo es obligatorio')).toBeInTheDocument();
    expect(screen.getByText('La contraseña es obligatoria')).toBeInTheDocument();

    // No debe disparar la llamada al servicio de login
    expect(mockLogin).not.toHaveBeenCalled();
  });

  // Escenario 3: Inicio de sesión exitoso (Rol Trabajador)
  it('inicia sesión con éxito para Trabajador, guarda token y redirige a su perfil', async () => {
    mockLogin.mockResolvedValue({
      data: {
        token: 'fake-jwt-token',
        usuario: {
          id: 'user-123',
          nombre: 'Carlos Silva',
          tipo: 'TRABAJADOR',
        },
      },
      error: null,
    });

    render(<LoginPage />);

    // Escribir credenciales válidas
    fireEvent.change(screen.getByLabelText(/Correo Electrónico/i), { target: { value: 'carlos@test.com' } });
    fireEvent.change(screen.getByLabelText(/Contraseña/i), { target: { value: 'password123' } });

    // Enviar formulario
    const submitBtn = screen.getByRole('button', { name: /Ingresar ahora/i });
    fireEvent.click(submitBtn);

    // Esperar a que se realicen las acciones de almacenamiento y enrutado
    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        email: 'carlos@test.com',
        password: 'password123',
      });
      // Validar persistencia local
      expect(localStorage.getItem('auth_token')).toBe('fake-jwt-token');
      expect(JSON.parse(localStorage.getItem('user_info') || '{}')).toEqual({
        id: 'user-123',
        nombre: 'Carlos Silva',
        tipo: 'TRABAJADOR',
      });
      // Validar redireccionamiento del trabajador
      expect(mockPush).toHaveBeenCalledWith('/trabajador/perfil');
    });
  });

  // Escenario 4: Inicio de sesión exitoso (Rol Empresa)
  it('inicia sesión con éxito para Empresa y redirige a su respectivo perfil', async () => {
    mockLogin.mockResolvedValue({
      data: {
        token: 'fake-jwt-token-2',
        usuario: {
          id: 'user-456',
          nombre: 'Constructora Beta',
          tipo: 'EMPRESA',
        },
      },
      error: null,
    });

    render(<LoginPage />);

    // Rellenar credenciales
    fireEvent.change(screen.getByLabelText(/Correo Electrónico/i), { target: { value: 'beta@test.com' } });
    fireEvent.change(screen.getByLabelText(/Contraseña/i), { target: { value: 'password456' } });

    // Enviar
    fireEvent.click(screen.getByRole('button', { name: /Ingresar ahora/i }));

    // Esperar redireccionamiento de la empresa
    await waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith('/empresa/perfil');
    });
  });

  // Escenario 5: Control de credenciales inválidas (Error de API)
  it('muestra un mensaje de error global cuando el correo o la contraseña son incorrectos', async () => {
    mockLogin.mockResolvedValue({
      data: null,
      error: 'Correo o contraseña incorrectos',
    });

    render(<LoginPage />);

    // Escribir credenciales
    fireEvent.change(screen.getByLabelText(/Correo Electrónico/i), { target: { value: 'incorrecto@test.com' } });
    fireEvent.change(screen.getByLabelText(/Contraseña/i), { target: { value: 'wrongpass' } });

    // Enviar
    fireEvent.click(screen.getByRole('button', { name: /Ingresar ahora/i }));

    // Debe mostrar la alerta de credenciales erróneas
    await waitFor(() => {
      expect(screen.getByText('Correo o contraseña incorrectos')).toBeInTheDocument();
      // No debe guardar tokens
      expect(localStorage.getItem('auth_token')).toBeNull();
    });
  });
});
