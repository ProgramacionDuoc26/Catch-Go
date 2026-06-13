import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { Button } from './Button';

describe('Button Component', () => {
  it('renders the button with children text correctly', () => {
    render(<Button>Click me</Button>);
    const buttonElement = screen.getByRole('button', { name: /click me/i });
    expect(buttonElement).toBeInTheDocument();
  });

  it('applies default classes (primary variant and md size)', () => {
    render(<Button>Button</Button>);
    const buttonElement = screen.getByRole('button');
    // Check primary background class
    expect(buttonElement.className).toContain('bg-primary');
    // Check md size height class
    expect(buttonElement.className).toContain('h-11');
  });

  it('applies the secondary variant class correctly', () => {
    render(<Button variant="secondary">Secondary</Button>);
    const buttonElement = screen.getByRole('button');
    expect(buttonElement.className).toContain('bg-gray-100');
  });

  it('applies the danger variant class correctly', () => {
    render(<Button variant="danger">Danger</Button>);
    const buttonElement = screen.getByRole('button');
    expect(buttonElement.className).toContain('bg-semantic-error');
  });

  it('applies small size class when size is sm', () => {
    render(<Button size="sm">Small</Button>);
    const buttonElement = screen.getByRole('button');
    expect(buttonElement.className).toContain('h-9');
  });

  it('applies fullWidth class when fullWidth is true', () => {
    render(<Button fullWidth>Full Width</Button>);
    const buttonElement = screen.getByRole('button');
    expect(buttonElement.className).toContain('w-full');
  });

  it('fires onClick event when clicked', () => {
    const handleClick = vi.fn();
    render(<Button onClick={handleClick}>Click</Button>);
    const buttonElement = screen.getByRole('button');
    fireEvent.click(buttonElement);
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('is disabled when disabled prop is true', () => {
    render(<Button disabled>Disabled</Button>);
    const buttonElement = screen.getByRole('button');
    expect(buttonElement).toBeDisabled();
    expect(buttonElement.className).toContain('disabled:opacity-50');
  });
});
