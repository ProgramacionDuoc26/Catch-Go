import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import PaymentGatewayModal from '@/components/modals/PaymentGatewayModal';

const mockWorkerProfile = {
  name: 'María González',
  rut: '12.345.678-9',
  bankName: 'Banco Estado',
  accountType: 'Cuenta RUT',
  accountNumber: '12345678',
};

describe('PaymentGatewayModal Component', () => {
  const defaultProps = {
    isOpen: true,
    onClose: vi.fn(),
    onSubmit: vi.fn(),
    workerProfile: mockWorkerProfile,
    amount: 15000,
    onPayWithWebpay: vi.fn(),
    isProcessingWebpay: false,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  // Escenario 1: No renderiza nada si isOpen es falso
  it('no renderiza el modal si isOpen es falso', () => {
    render(<PaymentGatewayModal {...defaultProps} isOpen={false} />);
    expect(screen.queryByText('Pago de Honorarios')).not.toBeInTheDocument();
  });

  // Escenario 2: Renderizado correcto de datos de trabajador y monto
  it('renderiza correctamente los datos bancarios del trabajador y el monto a pagar', () => {
    render(<PaymentGatewayModal {...defaultProps} />);

    expect(screen.getByText('Pago de Honorarios')).toBeInTheDocument();
    expect(screen.getByText('María González')).toBeInTheDocument();
    expect(screen.getByText('12.345.678-9')).toBeInTheDocument();
    expect(screen.getByText('Banco Estado')).toBeInTheDocument();
    expect(screen.getByText('Cuenta RUT')).toBeInTheDocument();
    expect(screen.getByText('12345678')).toBeInTheDocument();
    // Monto formateado en pesos chilenos
    expect(screen.getByText('$15.000')).toBeInTheDocument();
  });

  // Escenario 3: Validación de archivo de comprobante obligatorio para transferencias
  it('deshabilita el botón de confirmar pago si no se ha subido un archivo de comprobante', () => {
    render(<PaymentGatewayModal {...defaultProps} />);

    // El botón Confirmar Pago debe estar deshabilitado por defecto
    const confirmBtn = screen.getByRole('button', { name: 'Confirmar Pago' });
    expect(confirmBtn).toBeDisabled();
    expect(defaultProps.onSubmit).not.toHaveBeenCalled();
  });

  // Escenario 4: Envío exitoso del archivo en transferencia manual
  it('llama al callback onSubmit tras cargar el archivo de comprobante y presionar confirmar', async () => {
    // Mockear FileReader.prototype.readAsDataURL para que actúe de forma síncrona
    const fileReaderSpy = vi.spyOn(FileReader.prototype, 'readAsDataURL').mockImplementation(function(this: any) {
      Object.defineProperty(this, 'result', {
        value: 'data:application/pdf;base64,dummy',
        configurable: true
      });
      if (this.onloadend) {
        this.onloadend();
      }
    });

    vi.useFakeTimers();
    render(<PaymentGatewayModal {...defaultProps} />);

    // Simular la carga de un archivo en el input
    const file = new File(['dummy-content'], 'comprobante_deposito.pdf', { type: 'application/pdf' });
    const fileInput = screen.getByLabelText(/Haz clic para subir comprobante/i) as HTMLInputElement;

    // Simular el evento de selección de archivo
    fireEvent.change(fileInput, { target: { files: [file] } });

    // Verificar que el nombre del archivo se muestre en pantalla indicando carga
    expect(screen.getByText('comprobante_deposito.pdf')).toBeInTheDocument();

    // Confirmar pago (ahora el botón está habilitado porque hay un archivo)
    const confirmBtn = screen.getByRole('button', { name: 'Confirmar Pago' });
    expect(confirmBtn).not.toBeDisabled();
    fireEvent.click(confirmBtn);

    // Adelantar los temporizadores de forma asíncrona para vaciar la cola de microtareas y temporizadores
    await vi.advanceTimersByTimeAsync(1600);

    // Aserción directa
    expect(defaultProps.onSubmit).toHaveBeenCalledTimes(1);
    expect(defaultProps.onSubmit).toHaveBeenCalledWith({
      file,
      dataUrl: 'data:application/pdf;base64,dummy'
    });

    vi.useRealTimers();
    fileReaderSpy.mockRestore();
  });

  // Escenario 5: Interacción con la pestaña Webpay Plus y envío de formulario
  it('cambia de pestaña a Webpay Plus y llama a la pasarela de pago al presionar el botón', () => {
    render(<PaymentGatewayModal {...defaultProps} />);

    // Cambiar a la pestaña de Webpay Plus
    const webpayTab = screen.getByRole('button', { name: /Webpay Plus/i });
    fireEvent.click(webpayTab);

    // Debe mostrar el desglose de tarifas de la pasarela
    expect(screen.getByText('Comisión de Servicio Catch-Go (10%):')).toBeInTheDocument();
    // Monto final calculado: $15.000 + 10% ($1.500) = $16.500
    expect(screen.getByText('$16.500')).toBeInTheDocument();

    // Buscar y hacer clic en el botón de pagar con Webpay Plus
    const webpaySubmitBtn = screen.getByRole('button', { name: 'Pagar con Webpay Plus' });
    fireEvent.click(webpaySubmitBtn);

    expect(defaultProps.onPayWithWebpay).toHaveBeenCalledTimes(1);
  });

  // Escenario 6: Deshabilitación del botón de Webpay en procesamiento
  it('deshabilita el botón de Webpay y muestra spinner de carga cuando se está procesando', () => {
    const { rerender } = render(<PaymentGatewayModal {...defaultProps} isProcessingWebpay={false} />);

    // Cambiar a la pestaña de Webpay Plus cuando no está bloqueado
    const webpayTab = screen.getByRole('button', { name: /Webpay Plus/i });
    fireEvent.click(webpayTab);

    // Re-renderizar con isProcessingWebpay=true
    rerender(<PaymentGatewayModal {...defaultProps} isProcessingWebpay={true} />);

    // Verificar que el botón muestre el estado de carga y esté bloqueado
    const loadingBtn = screen.getByRole('button', { name: 'Conectando...' });
    expect(loadingBtn).toBeDisabled();
  });

  // Escenario 7: Quitar el archivo seleccionado
  it('permite quitar el archivo seleccionado al hacer clic en Quitar archivo', () => {
    render(<PaymentGatewayModal {...defaultProps} />);
    
    const file = new File(['dummy-content'], 'comprobante.pdf', { type: 'application/pdf' });
    const fileInput = screen.getByLabelText(/Haz clic para subir comprobante/i);
    
    fireEvent.change(fileInput, { target: { files: [file] } });
    expect(screen.getByText('comprobante.pdf')).toBeInTheDocument();
    
    const removeBtn = screen.getByRole('button', { name: 'Quitar archivo' });
    fireEvent.click(removeBtn);
    
    expect(screen.queryByText('comprobante.pdf')).not.toBeInTheDocument();
  });

  // Escenario 8: Carga y compresión de imagen
  it('ejecuta la lógica de compresión al subir un comprobante tipo imagen', async () => {
    const fileReaderSpy = vi.spyOn(FileReader.prototype, 'readAsDataURL').mockImplementation(function(this: any) {
      Object.defineProperty(this, 'result', {
        value: 'data:image/png;base64,dummy',
        configurable: true
      });
      if (this.onloadend) {
        this.onloadend();
      }
    });

    // Mockear el constructor de Image para forzar el onload
    const originalImage = global.Image;
    global.Image = class {
      onload: (() => void) | null = null;
      onerror: (() => void) | null = null;
      src: string = '';
      width: number = 800;
      height: number = 600;
      constructor() {
        setTimeout(() => {
          if (this.onload) this.onload();
        }, 10);
      }
    } as any;

    vi.useFakeTimers();
    render(<PaymentGatewayModal {...defaultProps} />);

    const file = new File(['image-content'], 'comprobante.png', { type: 'image/png' });
    const fileInput = screen.getByLabelText(/Haz clic para subir comprobante/i);

    fireEvent.change(fileInput, { target: { files: [file] } });
    expect(screen.getByText('comprobante.png')).toBeInTheDocument();

    const confirmBtn = screen.getByRole('button', { name: 'Confirmar Pago' });
    fireEvent.click(confirmBtn);

    await vi.advanceTimersByTimeAsync(20); // Gatillar Image.onload
    await vi.advanceTimersByTimeAsync(1600); // Gatillar setTimeout en submit

    expect(defaultProps.onSubmit).toHaveBeenCalledTimes(1);

    vi.useRealTimers();
    fileReaderSpy.mockRestore();
    global.Image = originalImage;
  });
});
