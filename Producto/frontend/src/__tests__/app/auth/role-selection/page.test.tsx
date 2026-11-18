import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import RoleSelectionPage from '@/app/auth/role-selection/page';

const { push, getUser, getSession, exchange } = vi.hoisted(() => ({
  push: vi.fn(), getUser: vi.fn(), getSession: vi.fn(), exchange: vi.fn(),
}));
vi.mock('next/navigation', () => ({ useRouter: () => ({ push }) }));
vi.mock('@/lib/supabase/client', () => ({ createClient: () => ({ auth: { getUser, getSession } }) }));
vi.mock('@/lib/api/auth', () => ({ authApi: { exchangeSupabase: exchange } }));
vi.mock('framer-motion', () => ({ motion: {
  div: ({ children, initial, animate, transition, layoutId, ...props }: any) => <div {...props}>{children}</div>,
  button: ({ children, initial, animate, transition, ...props }: any) => <button {...props}>{children}</button>,
  h1: ({ children, initial, animate, transition, ...props }: any) => <h1 {...props}>{children}</h1>,
  p: ({ children, initial, animate, transition, ...props }: any) => <p {...props}>{children}</p>,
}}));

describe('Google role selection', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    getUser.mockResolvedValue({ data: { user: { id: 'provider-id', email: 'user@example.com', user_metadata: {} } } });
    getSession.mockResolvedValue({ data: { session: { access_token: 'supabase-token' } }, error: null });
  });

  it('stores the verified backend session and follows the saved server role', async () => {
    exchange.mockResolvedValue({ data: { token: 'catchgo-token', usuario: { id: 'provider-id', tipo: 'TRABAJADOR' } }, error: null });
    render(<RoleSelectionPage />);
    fireEvent.click(screen.getByText('Busco Personal'));
    fireEvent.click(screen.getByRole('button', { name: /Confirmar y continuar/i }));
    await waitFor(() => expect(push).toHaveBeenCalledWith('/trabajador/perfil'));
    expect(exchange).toHaveBeenCalledWith({ accessToken: 'supabase-token', tipo: 'EMPRESA' });
    expect(localStorage.getItem('auth_token')).toBe('catchgo-token');
    expect(JSON.parse(localStorage.getItem('user_info') || '{}').id).toBe('provider-id');
  });

  it('does not create a local session when exchange fails', async () => {
    localStorage.setItem('auth_token', 'old-token');
    exchange.mockResolvedValue({ data: null, error: 'Sesion invalida' });
    render(<RoleSelectionPage />);
    fireEvent.click(screen.getByText('Busco Trabajo'));
    fireEvent.click(screen.getByRole('button', { name: /Confirmar y continuar/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Sesion invalida');
    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(localStorage.getItem('user_info')).toBeNull();
    expect(push).not.toHaveBeenCalled();
  });
});
