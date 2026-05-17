import React, { useState, useEffect } from 'react';
import { X, CheckCircle2, FileText, AlertCircle, ExternalLink } from 'lucide-react';
import { Button } from '@/components/ui/Button';

const openBase64InNewTab = (dataUrl: string, fileName: string) => {
  try {
    const arr = dataUrl.split(',');
    const mime = arr[0].match(/:(.*?);/)?.[1] || 'application/octet-stream';
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);
    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }
    const blob = new Blob([u8arr], { type: mime });
    const fileURL = URL.createObjectURL(blob);
    
    if (mime.includes('pdf') || mime.includes('image')) {
      window.open(fileURL, '_blank');
    } else {
      const a = document.createElement('a');
      a.href = fileURL;
      a.download = fileName;
      a.click();
    }
  } catch (e) {
    console.error("Error al abrir comprobante base64:", e);
    const win = window.open();
    if (win) {
      win.document.write(`<iframe src="${dataUrl}" frameborder="0" style="border:0; top:0px; left:0px; bottom:0px; right:0px; width:100%; height:100%;" allowfullscreen></iframe>`);
    }
  }
};

interface ValidatePaymentModalProps {
  isOpen: boolean;
  onClose: () => void;
  onValidate: (isValid: boolean) => void;
  companyName: string;
  amount: number;
  receiptInfo?: { fileName: string; date: string; dataUrl?: string } | null;
  appId?: string | number; // NUEVO: ID de postulación para carga global
}

export default function ValidatePaymentModal({ isOpen, onClose, onValidate, companyName, amount, receiptInfo, appId }: ValidatePaymentModalProps) {
  const [isProcessing, setIsProcessing] = useState(false);
  const [localReceipt, setLocalReceipt] = useState<any>(null);
  const [loadingReceipt, setLoadingReceipt] = useState(false);

  useEffect(() => {
    if (isOpen && appId) {
      setLoadingReceipt(true);
      fetch(`/api/receipts?appId=${appId}`)
        .then(res => {
          if (res.ok) return res.json();
          return null;
        })
        .then(data => {
          if (data) setLocalReceipt(data);
        })
        .catch(err => console.error("Error cargando comprobante en modal:", err))
        .finally(() => setLoadingReceipt(false));
    } else {
      setLocalReceipt(null);
    }
  }, [isOpen, appId]);

  if (!isOpen) return null;

  const handleValidation = (isValid: boolean) => {
    setIsProcessing(true);
    setTimeout(() => {
      onValidate(isValid);
      setIsProcessing(false);
    }, 1000);
  };

  const activeReceipt = localReceipt || receiptInfo;
  const isImage = activeReceipt?.dataUrl?.startsWith('data:image');
  const isPdf = activeReceipt?.dataUrl?.startsWith('data:application/pdf') || activeReceipt?.fileName?.toLowerCase().endsWith('.pdf');

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
            
            {activeReceipt && (
              <div className="mt-4 flex flex-col gap-3 text-left">
                <div className="p-3 bg-blue-50 border border-blue-100 rounded-xl flex items-center gap-3">
                  <div className="bg-white p-2 rounded-lg shadow-sm shrink-0">
                    <FileText className="text-blue-600 w-6 h-6" />
                  </div>
                  <div>
                    <p className="font-semibold text-blue-900 text-sm truncate max-w-[200px]" title={activeReceipt.fileName}>
                      {activeReceipt.fileName}
                    </p>
                    <p className="text-xs text-blue-600 mt-0.5">
                      Subido el {new Date(activeReceipt.date).toLocaleDateString('es-CL')} a las {new Date(activeReceipt.date).toLocaleTimeString('es-CL', {hour: '2-digit', minute:'2-digit'})}
                    </p>
                  </div>
                </div>
                
                {isPdf ? (
                  <div className="p-5 bg-slate-50 rounded-xl border border-slate-200 text-center space-y-3">
                    <div className="w-12 h-12 bg-red-50 text-red-600 rounded-xl flex items-center justify-center mx-auto">
                      <FileText size={28} />
                    </div>
                    <div>
                      <p className="font-bold text-slate-800 text-xs">Comprobante de Pago PDF</p>
                    </div>
                    <Button 
                      onClick={() => openBase64InNewTab(activeReceipt.dataUrl, activeReceipt.fileName)}
                      className="w-full bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl py-2 h-auto text-xs flex items-center justify-center gap-2 shadow-sm"
                    >
                      <ExternalLink size={12} /> Abrir y Visualizar PDF
                    </Button>
                  </div>
                ) : (
                  isImage && activeReceipt.dataUrl && (
                    <div className="space-y-2">
                      <div className="mt-2 rounded-xl overflow-hidden border border-gray-200 shadow-sm bg-gray-50 max-h-[300px] flex items-center justify-center">
                        {/* eslint-disable-next-line @next/next/no-img-element */}
                        <img src={activeReceipt.dataUrl} alt="Comprobante" className="max-w-full max-h-[300px] object-contain" />
                      </div>
                      <Button 
                        onClick={() => openBase64InNewTab(activeReceipt.dataUrl, activeReceipt.fileName)}
                        variant="outline"
                        className="w-full text-slate-700 hover:bg-slate-100 font-bold rounded-xl py-2.5 h-auto text-xs flex items-center justify-center gap-2 border-slate-200"
                      >
                        <ExternalLink size={12} /> Ampliar Imagen en Nueva Pestaña
                      </Button>
                    </div>
                  )
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
