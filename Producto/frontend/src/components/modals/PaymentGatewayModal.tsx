import React, { useState } from 'react';
import { X, Upload, CheckCircle2, DollarSign, Building2, CreditCard, Sparkles, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/Button';

interface PaymentGatewayModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (receiptData: { file: File, dataUrl: string }) => void;
  workerProfile: any;
  amount: number;
  onPayWithWebpay: () => Promise<void>;
  isProcessingWebpay: boolean;
}

export default function PaymentGatewayModal({ 
  isOpen, 
  onClose, 
  onSubmit, 
  workerProfile, 
  amount,
  onPayWithWebpay,
  isProcessingWebpay
}: PaymentGatewayModalProps) {
  const [paymentMethod, setPaymentMethod] = useState<'transfer' | 'webpay'>('transfer');
  const [file, setFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  if (!isOpen) return null;

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
    }
  };

  const handleSubmit = async () => {
    if (!file) {
      alert("Debes subir un comprobante de pago para continuar.");
      return;
    }
    
    setIsUploading(true);

    // Convertimos a Base64 para simular envío de archivo
    const reader = new FileReader();
    reader.onloadend = () => {
      const dataUrl = reader.result as string;
      setTimeout(() => {
        onSubmit({ file, dataUrl });
        setIsUploading(false);
        setFile(null);
      }, 1500);
    };
    reader.readAsDataURL(file);
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <div className="bg-white rounded-[28px] max-w-lg w-full shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200 border border-gray-100">
        <div className="p-6 sm:p-8">
          <div className="flex justify-between items-start mb-6">
            <div>
              <h3 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
                <DollarSign className="text-green-600 w-6 h-6" />
                Pago de Honorarios
              </h3>
              <p className="text-gray-500 text-sm mt-1">Selecciona el método de pago para transferir al trabajador.</p>
            </div>
            <button 
              onClick={onClose}
              className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center text-gray-500 hover:bg-gray-200 transition-all"
              disabled={isUploading || isProcessingWebpay}
            >
              <X size={18} />
            </button>
          </div>

          <div className="bg-blue-50/70 border border-blue-100/50 p-4 rounded-2xl mb-6">
            <h4 className="text-sm font-bold text-blue-900 mb-3 flex items-center gap-2">
              <Building2 size={16} />
              Datos Bancarios del Trabajador
            </h4>
            <div className="grid grid-cols-2 gap-y-2 text-sm">
              <span className="text-blue-700 font-medium">Trabajador:</span>
              <span className="font-semibold text-gray-900 text-right sm:text-left">{workerProfile?.name || 'Nombre no disponible'}</span>
              
              <span className="text-blue-700 font-medium">RUT:</span>
              <span className="font-semibold text-gray-900 text-right sm:text-left">{workerProfile?.rut || 'No registrado'}</span>
              
              <span className="text-blue-700 font-medium">Banco:</span>
              <span className="font-semibold text-gray-900 text-right sm:text-left">{workerProfile?.bankName || 'No registrado'}</span>
              
              <span className="text-blue-700 font-medium">Tipo Cuenta:</span>
              <span className="font-semibold text-gray-900 text-right sm:text-left">{workerProfile?.accountType || 'No registrado'}</span>
              
              <span className="text-blue-700 font-medium">N° de Cuenta:</span>
              <span className="font-semibold text-gray-900 text-right sm:text-left">{workerProfile?.accountNumber || 'No registrado'}</span>
              
              <span className="text-blue-700 font-medium">Monto a Pagar:</span>
              <span className="font-extrabold text-green-700 text-right sm:text-left">${amount.toLocaleString('es-CL')}</span>
            </div>
          </div>

          {/* Tab Selector de Métodos de Pago */}
          <div className="flex border-b border-gray-100 mb-6">
            <button
              onClick={() => setPaymentMethod('transfer')}
              className={`flex-1 pb-3 text-sm font-bold border-b-2 transition-colors flex items-center justify-center gap-2 ${paymentMethod === 'transfer' ? 'border-primary text-primary' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
              disabled={isUploading || isProcessingWebpay}
            >
              <Upload size={16} />
              Transferencia Manual
            </button>
            <button
              onClick={() => setPaymentMethod('webpay')}
              className={`flex-1 pb-3 text-sm font-bold border-b-2 transition-colors flex items-center justify-center gap-2 ${paymentMethod === 'webpay' ? 'border-amber-500 text-amber-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
              disabled={isUploading || isProcessingWebpay}
            >
              <CreditCard size={16} />
              Webpay Plus
            </button>
          </div>

          {paymentMethod === 'transfer' ? (
            /* Método 1: Transferencia Manual */
            <>
              <div className="mb-8">
                <label className="text-sm font-semibold text-gray-700 mb-2 flex items-center gap-2">
                  <CreditCard size={16} className="text-primary" />
                  Evidencia de Pago
                </label>
                <div className={`border-2 border-dashed rounded-2xl p-6 text-center transition-colors ${file ? 'border-green-300 bg-green-50/50' : 'border-gray-300 hover:border-primary/50'}`}>
                  <input 
                    type="file" 
                    id="receipt-upload" 
                    className="hidden" 
                    accept="image/*,.pdf"
                    onChange={handleFileChange}
                    disabled={isUploading}
                  />
                  {file ? (
                    <div className="flex flex-col items-center gap-2">
                      <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center text-green-600 shadow-inner">
                        <CheckCircle2 size={24} />
                      </div>
                      <p className="font-medium text-green-800 text-sm">{file.name}</p>
                      <p className="text-xs text-green-600">Comprobante adjuntado correctamente</p>
                      <button 
                        onClick={() => setFile(null)}
                        className="text-xs text-red-500 font-medium hover:underline mt-2"
                        disabled={isUploading}
                      >
                        Quitar archivo
                      </button>
                    </div>
                  ) : (
                    <label htmlFor="receipt-upload" className="cursor-pointer flex flex-col items-center gap-2">
                      <div className="w-12 h-12 bg-gray-100 rounded-full flex items-center justify-center text-gray-500 group-hover:bg-primary/10 group-hover:text-primary transition-colors">
                        <Upload size={24} />
                      </div>
                      <p className="font-medium text-gray-700 text-sm">Haz clic para subir comprobante</p>
                      <p className="text-xs text-gray-500">Formato JPG, PNG o PDF (Max. 5MB)</p>
                    </label>
                  )}
                </div>
              </div>

              <div className="flex gap-3">
                <Button variant="outline" onClick={onClose} className="flex-1" disabled={isUploading}>
                  Cancelar
                </Button>
                <Button 
                  variant="primary" 
                  onClick={handleSubmit} 
                  className="flex-1" 
                  disabled={!file || isUploading}
                >
                  {isUploading ? (
                    <span className="flex items-center gap-2 justify-center">
                      <Loader2 className="w-4 h-4 animate-spin" />
                      Procesando...
                    </span>
                  ) : 'Confirmar Pago'}
                </Button>
              </div>
            </>
          ) : (
            /* Método 2: Webpay Plus */
            <>
              <div className="mb-8 space-y-4">
                <div className="bg-amber-50 border border-amber-100/50 p-6 rounded-2xl text-center relative overflow-hidden">
                  <div className="absolute top-0 right-0 bg-amber-500 text-white text-[9px] font-bold px-2.5 py-0.5 rounded-bl-lg uppercase tracking-wider flex items-center gap-0.5 shadow-sm">
                    <Sparkles size={8} />
                    Instantáneo
                  </div>
                  <p className="text-xs text-amber-800 leading-relaxed max-w-xs mx-auto mb-4 font-medium">
                    Paga mediante **Webpay Plus**. Catch-Go recibe los fondos de forma segura y gestiona la transferencia al trabajador.
                  </p>
                  
                  <div className="border-t border-dashed border-amber-200 pt-3 text-xs text-left space-y-1.5 text-gray-600 max-w-xs mx-auto">
                    <div className="flex justify-between">
                      <span>Honorarios del Trabajador:</span>
                      <span className="font-semibold text-gray-800">${amount.toLocaleString('es-CL')}</span>
                    </div>
                    <div className="flex justify-between text-amber-700">
                      <span>Comisión de Servicio Catch-Go (10%):</span>
                      <span className="font-semibold">+${Math.round(amount * 0.10).toLocaleString('es-CL')}</span>
                    </div>
                    <div className="flex justify-between border-t border-amber-200 pt-2 font-extrabold text-gray-900 text-sm">
                      <span>Total a Pagar:</span>
                      <span className="text-amber-600">${Math.round(amount * 1.10).toLocaleString('es-CL')}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="flex gap-3">
                <Button variant="outline" onClick={onClose} className="flex-1" disabled={isProcessingWebpay}>
                  Cancelar
                </Button>
                <Button 
                  variant="primary" 
                  onClick={onPayWithWebpay} 
                  className="flex-1 bg-amber-500 hover:bg-amber-600 text-white font-bold border-none shadow-md" 
                  disabled={isProcessingWebpay}
                >
                  {isProcessingWebpay ? (
                    <span className="flex items-center gap-2 justify-center">
                      <Loader2 className="w-4 h-4 animate-spin" />
                      Conectando...
                    </span>
                  ) : (
                    <span className="flex items-center gap-1">
                      Pagar con Webpay Plus
                    </span>
                  )}
                </Button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
