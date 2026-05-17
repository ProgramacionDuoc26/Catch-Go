"use client";

import React, { createContext, useContext, useEffect, useState, useRef } from 'react';
import { toast } from 'sonner';
import { usePathname } from 'next/navigation';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

interface Notification {
  id: string;
  title: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  timestamp: Date;
  read: boolean;
  link?: string;
}

interface NotificationContextType {
  notifications: Notification[];
  unreadCount: number;
  markAsRead: (id: string) => void;
  clearAll: () => void;
  addNotification: (title: string, message: string, type?: 'info' | 'success' | 'warning' | 'error', link?: string) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export function NotificationProvider({ children }: { children: React.ReactNode }) {
  const [notifications, setNotifications] = useState<Notification[]>(() => {
    if (typeof window !== 'undefined') {
      const saved = localStorage.getItem('catchgo_notifications');
      if (saved) {
        try {
          return JSON.parse(saved).map((n: any) => ({ ...n, timestamp: new Date(n.timestamp) }));
        } catch (e) {
          console.error('Error parsing notifications from local storage:', e);
        }
      }
    }
    return [];
  });
  const pathname = usePathname();
  const stompClient = useRef<Client | null>(null);

  const unreadCount = notifications.filter(n => !n.read).length;

  useEffect(() => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('catchgo_notifications', JSON.stringify(notifications));
    }
  }, [notifications]);

  const addNotification = (title: string, message: string, type: 'info' | 'success' | 'warning' | 'error' = 'info', link?: string) => {
    const newNotif: Notification = {
      id: Math.random().toString(36).substr(2, 9),
      title,
      message,
      type,
      timestamp: new Date(),
      read: false,
      link,
    };

    setNotifications(prev => [newNotif, ...prev]);

    // Show toast
    toast[type === 'error' ? 'error' : (type as 'info' | 'success' | 'warning')](title, {
      description: message,
    });
  };

  const markAsRead = (id: string) => {
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
  };

  const clearAll = () => {
    setNotifications([]);
  };

  useEffect(() => {
    let client: Client | null = null;
    let checkInterval: NodeJS.Timeout;

    const connectWebSocket = (userId: string) => {
      if (stompClient.current) return;

      console.log('Connecting to WebSocket for user:', userId);
      const socket = new SockJS('http://localhost:8088/ws-notifications');
      const stomp = new Client({
        webSocketFactory: () => socket,
        debug: (str) => console.log('STOMP: ' + str),
        reconnectDelay: 5000,
        onConnect: () => {
          console.log('Connected to Notification Service');
          stomp.subscribe(`/topic/user/${userId}`, (message) => {
            const payload = JSON.parse(message.body);
            addNotification(
              payload.title || 'Nueva Notificación',
              payload.message || '',
              (payload.type as any) || 'info'
            );
          });
        },
        onStompError: (frame) => {
          console.error('STOMP error', frame);
        }
      });

      stomp.activate();
      stompClient.current = stomp;
      client = stomp;
    };

    const checkUser = () => {
      const storedUser = localStorage.getItem('user_info');
      if (storedUser) {
        try {
          const user = JSON.parse(storedUser);
          const userId = user.id?.toString();
          if (userId && !stompClient.current) {
            connectWebSocket(userId);
          }
        } catch (e) {
          console.error('Error parsing user_info', e);
        }
      } else if (stompClient.current) {
        // User logged out
        console.log('User logged out, deactivating WebSocket');
        stompClient.current.deactivate();
        stompClient.current = null;
      }
    };

    // Check immediately and then every 2 seconds for login/logout
    checkUser();
    checkInterval = setInterval(checkUser, 2000);

    return () => {
      clearInterval(checkInterval);
      if (stompClient.current) {
        stompClient.current.deactivate();
        stompClient.current = null;
      }
    };
  }, []);

  return (
    <NotificationContext.Provider value={{ notifications, unreadCount, markAsRead, clearAll, addNotification }}>
      {children}
    </NotificationContext.Provider>
  );
}

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (context === undefined) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
};
