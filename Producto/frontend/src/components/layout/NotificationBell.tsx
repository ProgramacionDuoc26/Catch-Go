"use client";

import React, { useState } from 'react';
import { Bell, Check, Trash2, Clock, ExternalLink } from 'lucide-react';
import { useNotifications } from '@/context/NotificationContext';
import { motion, AnimatePresence } from 'framer-motion';
import { useRouter } from 'next/navigation';

export function NotificationBell() {
  const { notifications, unreadCount, markAsRead, clearAll } = useNotifications();
  const [isOpen, setIsOpen] = useState(false);
  const router = useRouter();

  return (
    <div className="relative">
      <button 
        onClick={() => setIsOpen(!isOpen)}
        className="p-2 text-slate-500 hover:text-primary hover:bg-slate-100 rounded-full transition-all relative"
      >
        <Bell size={22} />
        {unreadCount > 0 && (
          <span className="absolute top-1.5 right-1.5 w-4 h-4 bg-red-600 text-white text-[10px] font-black flex items-center justify-center rounded-full border-2 border-white">
            {unreadCount}
          </span>
        )}
      </button>

      <AnimatePresence>
        {isOpen && (
          <>
            <div 
              className="fixed inset-0 z-40" 
              onClick={() => setIsOpen(false)} 
            />
            <motion.div 
              initial={{ opacity: 0, y: 10, scale: 0.95 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 10, scale: 0.95 }}
              className="absolute right-0 mt-3 w-80 md:w-96 bg-white rounded-[2rem] shadow-2xl border border-slate-100 z-50 overflow-hidden"
            >
              <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
                <h3 className="font-black text-slate-900 flex items-center gap-2">
                  Notificaciones
                  <span className="bg-primary/10 text-primary px-2 py-0.5 rounded-lg text-xs">
                    {unreadCount} nuevas
                  </span>
                </h3>
                {notifications.length > 0 && (
                  <button 
                    onClick={clearAll}
                    className="text-xs font-bold text-slate-400 hover:text-red-600 flex items-center gap-1"
                  >
                    <Trash2 size={12} /> Limpiar
                  </button>
                )}
              </div>

              <div className="max-h-[400px] overflow-y-auto">
                {notifications.length > 0 ? (
                  <div className="divide-y divide-slate-50">
                    {notifications.map((notif) => (
                      <div 
                        key={notif.id}
                        className={`p-5 hover:bg-slate-50 transition-colors relative group ${notif.link ? 'cursor-pointer' : ''} 
                          ${!notif.read && notif.type === 'success' ? 'bg-green-50/80 border-b border-green-100' : 
                            !notif.read ? 'bg-blue-50/40' : ''}`}
                        onClick={() => {
                          markAsRead(notif.id);
                          if (notif.link) {
                            setIsOpen(false);
                            router.push(notif.link);
                          }
                        }}
                      >
                        {!notif.read && (
                          <div className={`absolute left-0 top-0 bottom-0 w-1.5 ${notif.type === 'success' ? 'bg-green-500 shadow-[0_0_10px_rgba(34,197,94,0.5)]' : 'bg-blue-500'}`} />
                        )}
                        <div className="flex justify-between items-start mb-1">
                          <p className={`font-black text-sm ${
                            !notif.read && notif.type === 'success' ? 'text-green-900 text-base' :
                            !notif.read ? 'text-blue-900' : 'text-slate-600'
                          }`}>
                            {notif.type === 'success' ? '🏆 ' : '📝 '}{notif.title}
                          </p>
                          <span className="text-[10px] text-slate-400 flex items-center gap-1">
                            <Clock size={10} />
                            {new Date(notif.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </span>
                        </div>
                        <p className={`text-xs leading-relaxed font-medium ${notif.type === 'success' ? 'text-green-800' : 'text-blue-800/70'}`}>
                          {notif.message}
                        </p>
                        {!notif.read && (
                          <button 
                            className={`mt-3 text-[10px] font-black uppercase tracking-widest opacity-0 group-hover:opacity-100 transition-opacity flex items-center gap-1 ${notif.type === 'success' ? 'text-green-700' : 'text-blue-600'}`}
                            onClick={(e) => { e.stopPropagation(); markAsRead(notif.id); }}
                          >
                            <Check size={10} /> Marcar como leída
                          </button>
                        )}
                        {notif.link && notif.read && (
                          <div className="mt-2 text-[10px] font-bold text-slate-400 flex items-center gap-1">
                            <ExternalLink size={10} /> Ver detalles
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="p-12 text-center">
                    <div className="w-16 h-16 bg-slate-50 rounded-2xl flex items-center justify-center mx-auto mb-4 text-slate-300">
                      <Bell size={32} />
                    </div>
                    <p className="text-slate-400 font-medium italic">No tienes notificaciones</p>
                  </div>
                )}
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  );
}
