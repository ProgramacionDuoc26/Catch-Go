"use client";

import React, { useRef } from 'react';
import ReCAPTCHA from 'react-google-recaptcha';
import { ShieldCheck } from 'lucide-react';

interface SecurityCaptchaProps {
  onVerify: (token: string) => void;
  onReset?: () => void;
}

// Clave pública oficial de prueba de Google reCAPTCHA v2 (funciona universalmente en localhost y producción)
const RECAPTCHA_SITE_KEY = process.env.NEXT_PUBLIC_RECAPTCHA_SITE_KEY || "6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI";

export function SecurityCaptcha({ onVerify, onReset }: SecurityCaptchaProps) {
  const recaptchaRef = useRef<ReCAPTCHA>(null);

  const handleChange = (token: string | null) => {
    if (token) {
      onVerify(token);
    } else {
      if (onReset) onReset();
    }
  };

  const handleExpired = () => {
    if (onReset) onReset();
  };

  return (
    <div className="w-full bg-slate-50 border border-slate-200 rounded-2xl p-4 shadow-sm transition-all flex flex-col items-center justify-center gap-3">
      <div className="flex items-center gap-2 text-slate-800 font-bold text-xs uppercase tracking-wider self-start">
        <ShieldCheck className="w-4 h-4 text-primary" />
        <span>Verificación de Seguridad (Google reCAPTCHA v2)</span>
      </div>

      <div className="overflow-hidden rounded-xl border border-slate-200 shadow-sm bg-white p-1">
        <ReCAPTCHA
          ref={recaptchaRef}
          sitekey={RECAPTCHA_SITE_KEY}
          onChange={handleChange}
          onExpired={handleExpired}
          hl="es"
        />
      </div>
    </div>
  );
}
