"use client";

import React from 'react';
import { APIProvider, Map, Marker, InfoWindow } from '@vis.gl/react-google-maps';
import { MapPin, Navigation, ArrowRightLeft } from 'lucide-react';

interface JobDistanceMapProps {
  origin: { lat: number; lng: number; label: string };
  destination: { lat: number; lng: number; label: string };
  apiKey: string;
}

export default function JobDistanceMap({ origin, destination, apiKey }: JobDistanceMapProps) {
  
  // Calcular distancia usando Haversine
  const calculateDistance = (lat1: number, lon1: number, lat2: number, lon2: number) => {
    const R = 6371; // Radio de la Tierra en km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = 
      Math.sin(dLat/2) * Math.sin(dLat/2) +
      Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
      Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return (R * c).toFixed(1);
  };

  const distance = calculateDistance(origin.lat, origin.lng, destination.lat, destination.lng);

  // Calcular el centro del mapa entre ambos puntos
  const center = {
    lat: (origin.lat + destination.lat) / 2,
    lng: (origin.lng + destination.lng) / 2
  };

  if (!apiKey) {
    return (
      <div className="bg-primary-dark/5 border-2 border-dashed border-primary/20 p-8 rounded-[32px] text-center space-y-4">
        <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center mx-auto shadow-sm text-accent-red">
          <MapPin size={32} />
        </div>
        <div>
          <p className="text-xl font-bold text-primary-dark">Falta API Key de Google Maps</p>
          <p className="text-gray-500 text-sm mt-1">Por favor, configura tu API Key para habilitar el mapa interactivo.</p>
        </div>
        <div className="pt-2">
          <code className="text-[10px] bg-gray-100 px-3 py-1.5 rounded-full text-gray-400">
            NEXT_PUBLIC_GOOGLE_MAPS_API_KEY
          </code>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between bg-primary/5 p-4 rounded-2xl border border-primary/10">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-white rounded-xl flex items-center justify-center shadow-sm text-primary">
            <Navigation size={20} />
          </div>
          <div>
            <p className="text-xs text-gray-500 font-medium">Distancia Estimada</p>
            <p className="text-xl font-bold text-primary-dark">{distance} km</p>
          </div>
        </div>
        <ArrowRightLeft className="text-gray-300" />
        <div className="text-right">
          <p className="text-xs text-gray-500 font-medium">Tiempo aprox.</p>
          <p className="text-sm font-bold text-gray-700">~{Math.round(Number(distance) * 2.5)} min (Auto)</p>
        </div>
      </div>

      <div className="h-[350px] w-full rounded-3xl overflow-hidden border border-gray-100 shadow-xl relative">
        <APIProvider apiKey={apiKey}>
          <Map
            defaultCenter={center}
            defaultZoom={12}
            gestureHandling={'greedy'}
            disableDefaultUI={false}
          >
            {/* Marcador Origen */}
            <Marker position={origin} label="A" />
            
            {/* Marcador Destino */}
            <Marker position={destination} label="B" />
          </Map>
        </APIProvider>
      </div>

      <div className="flex gap-4 text-[11px] text-gray-500 px-2">
        <div className="flex items-center gap-1">
          <div className="w-2 h-2 rounded-full bg-red-500" /> Origen: {origin.label}
        </div>
        <div className="flex items-center gap-1">
          <div className="w-2 h-2 rounded-full bg-blue-500" /> Destino: {destination.label}
        </div>
      </div>
    </div>
  );
}
