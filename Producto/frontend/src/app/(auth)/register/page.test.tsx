import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import RegisterPage from './page';

// Simular el router de Next.js
const mockPush = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}));

// Simular el servicio de API de autenticación (importación dinámica)
const mockRegister = vi.fn();
vi.mock('@/lib/api/auth', () => ({
  authApi: {
    register: mockRegister,
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

describe('RegisterPage Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  // CP-62: Validación de fortaleza de contraseña (mínimo 8 caracteres, 1 mayúscula y 1 número)
  it('CP-62: Muestra error si la contraseña no cumple con la fortaleza requerida', () => {
    const { container } = render(<RegisterPage />);

    // Llenar campos válidos excepto contraseña
    fireEvent.change(container.querySelector('#name')!, { target: { value: 'Juan Perez' } });
    fireEvent.change(container.querySelector('#rut')!, { target: { value: '12345678-9' } });
    fireEvent.change(container.querySelector('#email')!, { target: { value: 'juan@test.com' } });
    fireEvent.change(container.querySelector('#phone')!, { target: { value: '+56 912345678' } });
    
    // Contraseña sin mayúscula o sin número o menor a 8 caracteres
    fireEvent.change(container.querySelector('#password')!, { target: { value: 'weakpass' } });
    fireEvent.change(container.querySelector('#confirmPassword')!, { target: { value: 'weakpass' } });

    // Intentar registrar
    const submitBtn = screen.getByRole('button', { name: /Crear Cuenta de Trabajador/i });
    fireEvent.click(submitBtn);

    // Debe mostrar error de contraseña débil
    expect(screen.getByText('Mínimo 8 caracteres, 1 mayúscula y 1 número')).toBeInTheDocument();
    expect(mockRegister).not.toHaveBeenCalled();
  });

  // CP-63: Mensaje de error local si las contraseñas no coinciden
  it('CP-63: Muestra error si las contraseñas no coinciden', () => {
    const { container } = render(<RegisterPage />);

    fireEvent.change(container.querySelector('#name')!, { target: { value: 'Juan Perez' } });
    fireEvent.change(container.querySelector('#rut')!, { target: { value: '12345678-9' } });
    fireEvent.change(container.querySelector('#email')!, { target: { value: 'juan@test.com' } });
    fireEvent.change(container.querySelector('#phone')!, { target: { value: '+56 912345678' } });
    
    // Contraseñas distintas
    fireEvent.change(container.querySelector('#password')!, { target: { value: 'Secured123' } });
    fireEvent.change(container.querySelector('#confirmPassword')!, { target: { value: 'Different123' } });

    // Intentar registrar
    const submitBtn = screen.getByRole('button', { name: /Crear Cuenta de Trabajador/i });
    fireEvent.click(submitBtn);

    // Debe mostrar error de coincidencia de contraseñas
    expect(screen.getByText('Las contraseñas no coinciden')).toBeInTheDocument();
    expect(mockRegister).not.toHaveBeenCalled();
  });

  // CP-64: Simulación de registro exitoso y redirección automática al Login/Dashboard
  it('CP-64: Permite completar registro exitoso tras aceptar los términos y condiciones', async () => {
    mockRegister.mockResolvedValue({
      data: {
        token: 'registered-jwt-token',
        usuario: {
          id: 'user-789',
          nombre: 'Juan Perez',
          tipo: 'TRABAJADOR'
        }
      },
      error: null
    });

    const { container } = render(<RegisterPage />);

    // Rellenar campos válidos
    fireEvent.change(container.querySelector('#name')!, { target: { value: 'Juan Perez' } });
    fireEvent.change(container.querySelector('#rut')!, { target: { value: '12345678-9' } });
    fireEvent.change(container.querySelector('#email')!, { target: { value: 'juan@test.com' } });
    fireEvent.change(container.querySelector('#phone')!, { target: { value: '+56 912345678' } });
    fireEvent.change(container.querySelector('#password')!, { target: { value: 'Secured123' } });
    fireEvent.change(container.querySelector('#confirmPassword')!, { target: { value: 'Secured123' } });

    // Hacer submit para abrir modal de términos
    const submitBtn = screen.getByRole('button', { name: /Crear Cuenta de Trabajador/i });
    fireEvent.click(submitBtn);

    // Verificar que se muestre el modal de términos y condiciones
    expect(screen.getByText('Términos, Condiciones y Privacidad')).toBeInTheDocument();

    // Confirmar registro en el modal
    const acceptBtn = screen.getByRole('button', { name: /Aceptar y Registrarse/i });
    fireEvent.click(acceptBtn);

    // Esperar registro y redirección
    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith({
        email: 'juan@test.com',
        password: 'Secured123',
        nombre: 'Juan Perez',
        tipo: 'TRABAJADOR',
        telefono: '+56 912345678'
      });

      // Validar persistencia
      expect(localStorage.getItem('auth_token')).toBe('registered-jwt-token');
      expect(JSON.parse(localStorage.getItem('user_info') || '{}')).toEqual({
        id: 'user-789',
        nombre: 'Juan Perez',
        tipo: 'TRABAJADOR',
        rut: '12345678-9'
      });

      // Redirigir a /trabajador/perfil
      expect(mockPush).toHaveBeenCalledWith('/trabajador/perfil');
    });
  });
});
