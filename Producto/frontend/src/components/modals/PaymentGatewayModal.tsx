import React, { useState } from 'react';
import { X, Upload, CheckCircle2, DollarSign, Building2, CreditCard } from 'lucide-react';
import { Button } from '@/components/ui/Button';

interface PaymentGatewayModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (receiptData: { file: File, dataUrl: string }) => void;
  workerProfile: any;
  amount: number;
}

export default function PaymentGatewayModal({ isOpen, onClose, onSubmit, workerProfile, amount }: PaymentGatewayModalProps) {
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
      <div className="bg-white rounded-[24px] max-w-lg w-full shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="p-6 sm:p-8">
          <div className="flex justify-between items-start mb-6">
            <div>
              <h3 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                <DollarSign className="text-green-600" />
                Pago de Honorarios
              </h3>
              <p className="text-gray-500 text-sm mt-1">Sube el comprobante de transferencia al trabajador.</p>
            </div>
            <button 
              onClick={onClose}
              className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center text-gray-500 hover:bg-gray-200 transition-all"
            >
              <X size={18} />
            </button>
          </div>

          <div className="bg-blue-50 border border-blue-100 p-4 rounded-xl mb-6">
            <h4 className="text-sm font-bold text-blue-900 mb-3 flex items-center gap-2">
              <Building2 size={16} />
              Datos Bancarios del Trabajador
            </h4>
            <div className="grid grid-cols-2 gap-y-2 text-sm">
              <span className="text-blue-700">Trabajador:</span>
              <span className="font-semibold text-gray-900">{workerProfile?.name || 'Nombre no disponible'}</span>
              
              <span className="text-blue-700">RUT:</span>
              <span className="font-semibold text-gray-900">{workerProfile?.rut || 'No registrado'}</span>
              
              <span className="text-blue-700">Banco:</span>
              <span className="font-semibold text-gray-900">{workerProfile?.bankName || 'No registrado'}</span>
              
              <span className="text-blue-700">Tipo de Cuenta:</span>
              <span className="font-semibold text-gray-900">{workerProfile?.accountType || 'No registrado'}</span>
              
              <span className="text-blue-700">N° de Cuenta:</span>
              <span className="font-semibold text-gray-900">{workerProfile?.accountNumber || 'No registrado'}</span>
              
              <span className="text-blue-700">Monto a Pagar:</span>
              <span className="font-bold text-green-700">${amount.toLocaleString('es-CL')}</span>
            </div>
          </div>

          <div className="mb-8">
            <label className="text-sm font-semibold text-gray-700 mb-2 flex items-center gap-2">
              <CreditCard size={16} className="text-primary" />
              Evidencia de Pago
            </label>
            <div className={`border-2 border-dashed rounded-xl p-6 text-center transition-colors ${file ? 'border-green-300 bg-green-50' : 'border-gray-300 hover:border-primary/50'}`}>
              <input 
                type="file" 
                id="receipt-upload" 
                className="hidden" 
                accept="image/*,.pdf"
                onChange={handleFileChange}
              />
              {file ? (
                <div className="flex flex-col items-center gap-2">
                  <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center text-green-600">
                    <CheckCircle2 size={24} />
                  </div>
                  <p className="font-medium text-green-800 text-sm">{file.name}</p>
                  <p className="text-xs text-green-600">Comprobante adjuntado correctamente</p>
                  <button 
                    onClick={() => setFile(null)}
                    className="text-xs text-red-500 font-medium hover:underline mt-2"
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
            <Button variant="outline" onClick={onClose} className="flex-1">
              Cancelar
            </Button>
            <Button 
              variant="primary" 
              onClick={handleSubmit} 
              className="flex-1" 
              disabled={!file || isUploading}
            >
              {isUploading ? 'Procesando...' : 'Confirmar Pago'}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
