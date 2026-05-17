"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { Search, MapPin, Calendar, Clock } from "lucide-react";

export default function TrabajadorDashboard() {
  const [userName, setUserName] = useState("Trabajador");

  useEffect(() => {
    const loadUserName = async () => {
      // 1. Intentar con localStorage (login local)
      const storedUser = localStorage.getItem('user_info');
      if (storedUser) {
        try {
          const parsed = JSON.parse(storedUser);
          const name = parsed.nombre || parsed.name || '';
          if (name) {
            setUserName(name.split(' ')[0]);
            return;
          }
        } catch { /* ignore */ }
      }

    };
    loadUserName();
  }, []);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
        <div>
          <h1 className="text-3xl font-bold text-text-main">Hola, {userName}</h1>
          <p className="text-text-muted mt-1">Aquí tienes turnos sugeridos según tu perfil.</p>
        </div>
        <button className="bg-primary text-white px-4 py-2 rounded-md hover:bg-primary-dark transition-colors font-semibold flex items-center gap-2 min-h-[44px]">
          <Search className="w-5 h-5" />
          Buscar Turnos
        </button>
      </div>

      <h2 className="text-xl font-bold text-text-main mb-4">Recomendados para ti</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="bg-surface rounded-xl border border-gray-100 shadow-sm p-6 flex flex-col h-full hover:shadow-md transition-shadow">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h3 className="font-semibold text-text-main text-lg">Conserje Reemplazo Fin de Semana</h3>
                <p className="text-sm text-text-muted mt-1">Comunidad Edificio Central</p>
              </div>
              <span className="text-semantic-success font-bold text-lg whitespace-nowrap ml-2">$45.000</span>
            </div>
            
            <div className="space-y-2 mt-auto">
              <div className="flex items-center text-sm text-text-muted gap-2">
                <MapPin className="w-4 h-4" />
                <span>Providencia, RM</span>
              </div>
              <div className="flex items-center text-sm text-text-muted gap-2">
                <Calendar className="w-4 h-4" />
                <span>Sábado 14 - Domingo 15</span>
              </div>
              <div className="flex items-center text-sm text-text-muted gap-2">
                <Clock className="w-4 h-4" />
                <span>08:00 - 20:00 hrs</span>
              </div>
            </div>

            <button className="w-full mt-6 bg-surface border border-primary text-primary px-4 py-2 rounded-md hover:bg-blue-50 transition-colors font-semibold min-h-[44px]">
              Ver Detalles
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
