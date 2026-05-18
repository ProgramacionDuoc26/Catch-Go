"use client";

import React, { useState, useCallback, useEffect } from 'react';
import { APIProvider, Map, Marker } from '@vis.gl/react-google-maps';
import { MapPin, Navigation } from 'lucide-react';

interface LocationPickerProps {
  initialLat?: number;
  initialLng?: number;
  onLocationChange: (lat: number, lng: number) => void;
  address?: string;
  onAddressChange?: (address: string) => void;
  apiKey: string;
}

const DEFAULT_CENTER = { lat: -33.4489, lng: -70.6693 }; // Santiago, Chile

export default function LocationPicker({ 
  initialLat, 
  initialLng, 
  onLocationChange, 
  address,
  onAddressChange,
  apiKey 
}: LocationPickerProps) {
  const [markerPos, setMarkerPos] = useState({ 
    lat: initialLat || DEFAULT_CENTER.lat, 
    lng: initialLng || DEFAULT_CENTER.lng 
  });
  const [searchInput, setSearchInput] = useState(address || '');

  // Sincronizar posición si cambian los props iniciales
  useEffect(() => {
    if (initialLat && initialLng) {
      setMarkerPos({ lat: initialLat, lng: initialLng });
    }
  }, [initialLat, initialLng]);

  // Sincronizar input de dirección si cambia desde fuera
  useEffect(() => {
    if (address !== undefined) {
      setSearchInput(address);
    }
  }, [address]);

  const handleSearch = async () => {
    if (!searchInput.trim()) return;

    try {
      const response = await fetch(
        `https://maps.googleapis.com/maps/api/geocode/json?address=${encodeURIComponent(searchInput)}&key=${apiKey}`
      );
      const data = await response.json();
      if (data.status === 'OK' && data.results && data.results.length > 0) {
        const { lat, lng } = data.results[0].geometry.location;
        const formattedAddress = data.results[0].formatted_address;
        setMarkerPos({ lat, lng });
        onLocationChange(lat, lng);
        if (onAddressChange) {
          onAddressChange(formattedAddress);
        }
      } else {
        alert('No se pudo encontrar la dirección. Intenta escribirla con más detalles.');
      }
    } catch (error) {
      console.error('Error geocoding address:', error);
      alert('Error al conectar con el servicio de geocodificación.');
    }
  };

  const handleReverseGeocode = async (lat: number, lng: number) => {
    try {
      const response = await fetch(
        `https://maps.googleapis.com/maps/api/geocode/json?latlng=${lat},${lng}&key=${apiKey}`
      );
      const data = await response.json();
      if (data.status === 'OK' && data.results && data.results.length > 0) {
        const formattedAddress = data.results[0].formatted_address;
        setSearchInput(formattedAddress);
        if (onAddressChange) {
          onAddressChange(formattedAddress);
        }
      }
    } catch (error) {
      console.error('Error reverse geocoding coordinates:', error);
    }
  };

  const handleMapClick = useCallback((e: any) => {
    const newLat = e.detail.latLng.lat;
    const newLng = e.detail.latLng.lng;
    setMarkerPos({ lat: newLat, lng: newLng });
    onLocationChange(newLat, newLng);
    handleReverseGeocode(newLat, newLng);
  }, [onLocationChange, apiKey, onAddressChange]);

  const handleGetCurrentLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          setMarkerPos({ lat: latitude, lng: longitude });
          onLocationChange(latitude, longitude);
          handleReverseGeocode(latitude, longitude);
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
    <div className="space-y-4">
      {/* Address Search Field */}
      <div className="flex flex-col gap-1.5">
        <label className="text-xs font-bold uppercase tracking-wider text-gray-400">
          Buscar Dirección del Lugar de Trabajo
        </label>
        <div className="flex gap-2">
          <input
            type="text"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                e.preventDefault();
                handleSearch();
              }
            }}
            placeholder="Ej. Av. Andrés Bello 2425, Providencia"
            className="flex-1 px-4 py-3 border border-gray-300 rounded-md focus:ring-primary focus:border-primary shadow-sm text-sm"
          />
          <button
            type="button"
            onClick={handleSearch}
            className="bg-primary text-white px-5 py-3 rounded-md hover:bg-primary-dark transition-colors text-sm font-semibold shadow-sm min-h-[44px]"
          >
            Buscar
          </button>
        </div>
      </div>

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
              handleReverseGeocode(newLat, newLng);
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
