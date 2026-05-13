"use client";

import React, { useState, useCallback, useEffect } from 'react';
import { APIProvider, Map, Marker, useApiIsLoaded } from '@vis.gl/react-google-maps';
import { Loader2, MapPin, Navigation } from 'lucide-react';

interface LocationPickerProps {
  initialLat?: number;
  initialLng?: number;
  onLocationChange: (lat: number, lng: number) => void;
  apiKey: string;
}

const DEFAULT_CENTER = { lat: -33.4489, lng: -70.6693 }; // Santiago, Chile

export default function LocationPicker({ initialLat, initialLng, onLocationChange, apiKey }: LocationPickerProps) {
  const [markerPos, setMarkerPos] = useState({ 
    lat: initialLat || DEFAULT_CENTER.lat, 
    lng: initialLng || DEFAULT_CENTER.lng 
  });

  // Sincronizar posición si cambian los props iniciales (ej. después de cargar de API)
  useEffect(() => {
    if (initialLat && initialLng) {
      setMarkerPos({ lat: initialLat, lng: initialLng });
    }
  }, [initialLat, initialLng]);

  const handleMapClick = useCallback((e: any) => {
    const newLat = e.detail.latLng.lat;
    const newLng = e.detail.latLng.lng;
    setMarkerPos({ lat: newLat, lng: newLng });
    onLocationChange(newLat, newLng);
  }, [onLocationChange]);

  const handleGetCurrentLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          setMarkerPos({ lat: latitude, lng: longitude });
          onLocationChange(latitude, longitude);
        },
        (error) => {
          console.error("Error getting location:", error);
          alert("No se pudo obtener tu ubicación actual. Revisa los permisos de tu navegador.");
        }
      );
    }
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
    <div className="space-y-3">
      <div className="flex justify-between items-center">
        <label className="text-xs font-bold uppercase tracking-wider text-gray-400">
          Ubicación en el Mapa
        </label>
        <button 
          type="button"
          onClick={handleGetCurrentLocation}
          className="text-xs font-bold text-primary flex items-center gap-1 hover:underline"
        >
          <Navigation size={12} /> Usar mi ubicación actual
        </button>
      </div>

      <div className="h-[300px] w-full rounded-2xl overflow-hidden border border-gray-200 shadow-inner relative">
        <APIProvider apiKey={apiKey}>
          <Map
            center={markerPos}
            defaultZoom={17}
            gestureHandling={'greedy'}
            disableDefaultUI={false}
            onClick={handleMapClick}
            mapId="CATCH_GO_MAP_ID"
          >
            <Marker position={markerPos} draggable={true} onDragEnd={(e: any) => {
              const newLat = e.latLng.lat();
              const newLng = e.latLng.lng();
              setMarkerPos({ lat: newLat, lng: newLng });
              onLocationChange(newLat, newLng);
            }} />
          </Map>
        </APIProvider>
      </div>
      <p className="text-[10px] text-gray-400 italic">
        Haz clic en el mapa o arrastra el marcador para precisar tu ubicación exacta.
      </p>
    </div>
  );
}
