import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { Button } from '@/components/ui/Button';

describe('Button Component', () => {
  // Verifica que el botón renderice el texto hijo correctamente
  it('renderiza el botón con el texto hijo correctamente', () => {
    render(<Button>Haz clic aquí</Button>);
    const buttonElement = screen.getByRole('button', { name: /haz clic aquí/i });
    expect(buttonElement).toBeInTheDocument();
  });

  // Verifica que se apliquen las clases por defecto (variante primaria y tamaño mediano)
  it('aplica las clases por defecto (variante primaria y tamaño mediano)', () => {
    render(<Button>Botón</Button>);
    const buttonElement = screen.getByRole('button');
    // Comprueba la clase de fondo primario
    expect(buttonElement.className).toContain('bg-primary');
    // Comprueba la clase de tamaño mediano (altura h-11)
    expect(buttonElement.className).toContain('h-11');
  });

  // Verifica que se aplique la clase de variante secundaria correctamente
  it('aplica la clase de variante secundaria correctamente', () => {
    render(<Button variant="secondary">Secundario</Button>);
    const buttonElement = screen.getByRole('button');
    expect(buttonElement.className).toContain('bg-gray-100');
  });

  // Verifica que se aplique la clase de variante de peligro (danger) correctamente
  it('aplica la clase de variante de peligro correctamente', () => {
    render(<Button variant="danger">Peligro</Button>);
    const buttonElement = screen.getByRole('button');
    expect(buttonElement.className).toContain('bg-semantic-error');
  });

  // Verifica que se aplique la clase de tamaño pequeño cuando el prop size es sm
  it('aplica la clase de tamaño pequeño cuando el tamaño es sm', () => {
    render(<Button size="sm">Pequeño</Button>);
    const buttonElement = screen.getByRole('button');
    expect(buttonElement.className).toContain('h-9');
  });

  // Verifica que se aplique la clase fullWidth (w-full) cuando el prop fullWidth es verdadero
  it('aplica la clase fullWidth cuando fullWidth es verdadero', () => {
    render(<Button fullWidth>Ancho Completo</Button>);
    const buttonElement = screen.getByRole('button');
    expect(buttonElement.className).toContain('w-full');
  });

  // Verifica que se dispare el evento onClick al hacer clic en el botón
  it('dispara el evento onClick al hacer clic', () => {
    const handleClick = vi.fn();
    render(<Button onClick={handleClick}>Clic</Button>);
    const buttonElement = screen.getByRole('button');
    fireEvent.click(buttonElement);
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  // Verifica que el botón esté deshabilitado cuando el prop disabled es verdadero y tenga opacidad reducida
  it('está deshabilitado cuando el prop disabled es verdadero', () => {
    render(<Button disabled>Deshabilitado</Button>);
    const buttonElement = screen.getByRole('button');
    expect(buttonElement).toBeDisabled();
    expect(buttonElement.className).toContain('disabled:opacity-50');
  });
});
