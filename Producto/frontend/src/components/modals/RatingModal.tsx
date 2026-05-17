import React, { useState } from 'react';
import { Star, X, MessageSquare } from 'lucide-react';
import { Button } from '@/components/ui/Button';

interface RatingModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (stars: number, feedback: string) => void;
  title: string;
  subtitle?: string;
  targetName: string;
}

export default function RatingModal({ isOpen, onClose, onSubmit, title, subtitle, targetName }: RatingModalProps) {
  const [rating, setRating] = useState(0);
  const [hoveredRating, setHoveredRating] = useState(0);
  const [feedback, setFeedback] = useState('');

  if (!isOpen) return null;

  const handleSubmit = () => {
    if (rating === 0) {
      alert("Por favor, selecciona una calificación de 1 a 5 estrellas.");
      return;
    }
    onSubmit(rating, feedback);
    // Reset state for future uses
    setRating(0);
    setFeedback('');
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <div className="bg-white rounded-[24px] max-w-md w-full shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="p-6 sm:p-8">
          <div className="flex justify-between items-start mb-6">
            <div>
              <h3 className="text-xl font-bold text-gray-900">{title}</h3>
              {subtitle && <p className="text-gray-500 text-sm mt-1">{subtitle}</p>}
            </div>
            <button 
              onClick={onClose}
              className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center text-gray-500 hover:bg-gray-200 transition-all"
            >
              <X size={18} />
            </button>
          </div>

          <div className="text-center mb-8">
            <p className="text-sm font-medium text-gray-600 mb-4">¿Cómo fue tu experiencia con <span className="font-bold text-gray-900">{targetName}</span>?</p>
            <div className="flex justify-center gap-2">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  type="button"
                  onMouseEnter={() => setHoveredRating(star)}
                  onMouseLeave={() => setHoveredRating(0)}
                  onClick={() => setRating(star)}
                  className="transition-transform hover:scale-110 focus:outline-none"
                >
                  <Star 
                    size={40} 
                    className={`transition-colors ${
                      (hoveredRating || rating) >= star 
                        ? 'fill-amber-400 text-amber-400' 
                        : 'fill-gray-100 text-gray-300'
                    }`} 
                  />
                </button>
              ))}
            </div>
            <div className="mt-3 text-sm font-bold text-amber-600 min-h-[20px]">
              {rating === 1 && "Muy mala"}
              {rating === 2 && "Mala"}
              {rating === 3 && "Regular"}
              {rating === 4 && "Buena"}
              {rating === 5 && "¡Excelente!"}
            </div>
          </div>

          <div className="space-y-3 mb-6">
            <label className="text-sm font-semibold text-gray-700 flex items-center gap-2">
              <MessageSquare size={16} className="text-primary" />
              Comentarios adicionales (Opcional)
            </label>
            <textarea 
              value={feedback}
              onChange={(e) => setFeedback(e.target.value)}
              placeholder="¿Qué destacarías o qué se podría mejorar?"
              className="w-full p-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary resize-none text-sm"
              rows={3}
            />
          </div>

          <div className="flex gap-3 mt-8">
            <Button variant="outline" onClick={onClose} className="flex-1">
              Cancelar
            </Button>
            <Button variant="primary" onClick={handleSubmit} className="flex-1" disabled={rating === 0}>
              Enviar Calificación
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
