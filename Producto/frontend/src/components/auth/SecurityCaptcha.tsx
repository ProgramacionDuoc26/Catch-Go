"use client";

import React, { useState, useEffect } from 'react';
import { ShieldCheck, RefreshCw, CheckCircle2, Lock } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

interface SecurityCaptchaProps {
  onVerify: (token: string) => void;
  onReset?: () => void;
}

export function SecurityCaptcha({ onVerify, onReset }: SecurityCaptchaProps) {
  const [num1, setNum1] = useState(0);
  const [num2, setNum2] = useState(0);
  const [userAnswer, setUserAnswer] = useState('');
  const [isVerified, setIsVerified] = useState(false);
  const [error, setError] = useState('');

  const generateProblem = () => {
    const n1 = Math.floor(Math.random() * 9) + 1;
    const n2 = Math.floor(Math.random() * 9) + 1;
    setNum1(n1);
    setNum2(n2);
    setUserAnswer('');
    setIsVerified(false);
    setError('');
    if (onReset) onReset();
  };

  useEffect(() => {
    generateProblem();
  }, []);

  const handleVerify = () => {
    const correctAnswer = num1 + num2;
    if (parseInt(userAnswer.trim(), 10) === correctAnswer) {
      setIsVerified(true);
      setError('');
      const token = `captcha-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
      onVerify(token);
    } else {
      setError('Respuesta incorrecta. Inténtalo de nuevo.');
      generateProblem();
    }
  };

  return (
    <div className="w-full bg-slate-50 border border-slate-200 rounded-2xl p-5 shadow-sm transition-all">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2 text-slate-800 font-bold text-sm">
          <ShieldCheck className="w-5 h-5 text-primary" />
          <span>Verificación de Seguridad (Captcha)</span>
        </div>
        <button
          type="button"
          onClick={generateProblem}
          className="text-slate-400 hover:text-slate-600 transition-colors p-1 rounded-lg hover:bg-slate-200/50"
          title="Generar nuevo reto"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      <AnimatePresence mode="wait">
        {isVerified ? (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="flex items-center gap-3 bg-emerald-50 border border-emerald-200 rounded-xl p-3 text-emerald-800 font-semibold text-sm"
          >
            <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0" />
            <span>Verificación completada correctamente</span>
          </motion.div>
        ) : (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="space-y-3"
          >
            <div className="flex items-center gap-3">
              <div className="bg-white border border-slate-200 rounded-xl px-4 py-2 text-lg font-bold text-slate-800 tracking-wider shadow-inner flex items-center gap-2">
                <Lock className="w-4 h-4 text-slate-400" />
                <span>{num1} + {num2} =</span>
              </div>
              <input
                type="number"
                value={userAnswer}
                onChange={(e) => setUserAnswer(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleVerify();
                  }
                }}
                placeholder="?"
                className="w-20 bg-white border border-slate-200 rounded-xl px-3 py-2 text-center text-lg font-bold text-slate-900 focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary shadow-sm"
              />
              <button
                type="button"
                onClick={handleVerify}
                disabled={!userAnswer}
                className="bg-primary hover:bg-primary-dark text-white font-bold px-4 py-2 rounded-xl text-sm transition-all disabled:opacity-50 shadow-sm"
              >
                Validar
              </button>
            </div>
            {error && (
              <p className="text-xs font-semibold text-red-500 ml-1">{error}</p>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
