import React, { useState } from 'react';
import { X, CheckCircle2, FileText, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/Button';

interface ValidatePaymentModalProps {
  isOpen: boolean;
  onClose: () => void;
  onValidate: (isValid: boolean) => void;
  companyName: string;
  amount: number;
  receiptInfo?: { fileName: string; date: string; dataUrl?: string } | null;
}

export default function ValidatePaymentModal({ isOpen, onClose, onValidate, companyName, amount, receiptInfo }: ValidatePaymentModalProps) {
  const [isProcessing, setIsProcessing] = useState(false);

  if (!isOpen) return null;

  const handleValidation = (isValid: boolean) => {
    setIsProcessing(true);
    setTimeout(() => {
      onValidate(isValid);
      setIsProcessing(false);
    }, 1000);
  };

  const isImage = receiptInfo?.dataUrl?.startsWith('data:image');

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <div className="bg-white rounded-[24px] max-w-md w-full shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="p-6 sm:p-8 max-h-[90vh] overflow-y-auto">
          <div className="flex justify-between items-start mb-6">
            <div>
              <h3 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                <CheckCircle2 className="text-green-600" />
                Validar Recepción de Pago
              </h3>
            </div>
            <button 
              onClick={onClose}
              className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center text-gray-500 hover:bg-gray-200 transition-all shrink-0"
            >
              <X size={18} />
            </button>
          </div>

          <div className="text-center mb-6">
            <div className="w-16 h-16 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <FileText size={32} />
            </div>
            <p className="text-gray-700 text-sm">
              La empresa <strong>{companyName}</strong> ha subido un comprobante de pago por tus honorarios del turno.
            </p>
            <div className="mt-4 p-4 bg-gray-50 rounded-xl border border-gray-100">
              <p className="text-sm text-gray-500 uppercase tracking-wider font-semibold mb-1">Monto a recibir</p>
              <p className="text-3xl font-bold text-green-600">${amount.toLocaleString('es-CL')}</p>
            </div>
            
            {receiptInfo && (
              <div className="mt-4 flex flex-col gap-3 text-left">
                <div className="p-3 bg-blue-50 border border-blue-100 rounded-xl flex items-center gap-3">
                  <div className="bg-white p-2 rounded-lg shadow-sm shrink-0">
                    <FileText className="text-blue-600 w-6 h-6" />
                  </div>
                  <div>
                    <p className="font-semibold text-blue-900 text-sm truncate max-w-[200px]" title={receiptInfo.fileName}>
                      {receiptInfo.fileName}
                    </p>
                    <p className="text-xs text-blue-600 mt-0.5">
                      Subido el {new Date(receiptInfo.date).toLocaleDateString('es-CL')} a las {new Date(receiptInfo.date).toLocaleTimeString('es-CL', {hour: '2-digit', minute:'2-digit'})}
                    </p>
                  </div>
                </div>
                
                {isImage && (
                  <div className="mt-2 rounded-xl overflow-hidden border border-gray-200 shadow-sm bg-gray-50 max-h-[300px] flex items-center justify-center">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img src={receiptInfo.dataUrl} alt="Comprobante" className="max-w-full max-h-[300px] object-contain" />
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="bg-amber-50 border border-amber-200 p-4 rounded-xl mb-6 flex gap-3">
            <AlertCircle className="text-amber-600 shrink-0 mt-0.5" size={20} />
            <p className="text-xs text-amber-800">
              Por favor, revisa tu cuenta bancaria (Cuenta RUT u otra). Solo haz clic en &quot;Confirmar&quot; si el dinero ya está en tu cuenta.
            </p>
          </div>

          <div className="flex flex-col gap-3">
            <Button 
              variant="primary" 
              onClick={() => handleValidation(true)} 
              className="w-full bg-green-600 hover:bg-green-700 border-green-600 text-white shadow-md shadow-green-200"
              disabled={isProcessing}
            >
              {isProcessing ? 'Procesando...' : 'Sí, el pago está OK'}
            </Button>
            <Button 
              variant="outline" 
              onClick={() => handleValidation(false)} 
              className="w-full text-red-600 hover:bg-red-50 hover:text-red-700 border-red-200"
              disabled={isProcessing}
            >
              No he recibido el pago
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
