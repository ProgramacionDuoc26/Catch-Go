"use client";

import React, { useEffect, useState } from 'react';
import { APIProvider, Map, useMapsLibrary, useMap, AdvancedMarker, Pin } from '@vis.gl/react-google-maps';
import { MapPin, Navigation, ArrowRightLeft, Loader2 } from 'lucide-react';

interface JobDistanceMapProps {
  origin: { lat: number; lng: number; label: string };
  destination: { lat: number; lng: number; label: string };
  apiKey: string;
}

export default function JobDistanceMap({ origin, destination, apiKey }: JobDistanceMapProps) {
  const [distance, setDistance] = useState<string>("...");
  const [duration, setDuration] = useState<string>("...");

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
            <p className="text-xs text-gray-500 font-medium">Distancia de Ruta</p>
            <p className="text-xl font-bold text-primary-dark">{distance}</p>
          </div>
        </div>
        <ArrowRightLeft className="text-gray-300" />
        <div className="text-right">
          <p className="text-xs text-gray-500 font-medium">Tiempo estimado</p>
          <p className="text-sm font-bold text-gray-700">{duration}</p>
        </div>
      </div>

      <div className="h-[350px] w-full rounded-3xl overflow-hidden border border-gray-100 shadow-xl relative">
        <APIProvider apiKey={apiKey}>
          <Map
            defaultCenter={origin}
            defaultZoom={12}
            gestureHandling={'greedy'}
            disableDefaultUI={false}
            mapId="9bff840b89254a0a" // Map ID para usar AdvancedMarkers
          >
            <Directions 
              origin={origin} 
              destination={destination} 
              onRouteCalculated={(dist, dur) => {
                setDistance(dist);
                setDuration(dur);
              }}
            />
          </Map>
        </APIProvider>
      </div>

      <div className="flex gap-4 text-[11px] text-gray-500 px-2">
        <div className="flex items-center gap-1">
          <div className="w-2 h-2 rounded-full bg-primary" /> Origen: {origin.label}
        </div>
        <div className="flex items-center gap-1">
          <div className="w-2 h-2 rounded-full bg-accent-red" /> Destino: {destination.label}
        </div>
      </div>
    </div>
  );
}

function Directions({ 
  origin, 
  destination, 
  onRouteCalculated 
}: { 
  origin: { lat: number; lng: number }; 
  destination: { lat: number; lng: number };
  onRouteCalculated: (dist: string, dur: string) => void;
}) {
  const map = useMap();
  const routesLibrary = useMapsLibrary('routes');
  const [directionsService, setDirectionsService] = useState<google.maps.DirectionsService>();
  const [directionsRenderer, setDirectionsRenderer] = useState<google.maps.DirectionsRenderer>();

  useEffect(() => {
    if (!routesLibrary || !map) return;
    setDirectionsService(new routesLibrary.DirectionsService());
    setDirectionsRenderer(new routesLibrary.DirectionsRenderer({ map }));
  }, [routesLibrary, map]);

  useEffect(() => {
    if (!directionsService || !directionsRenderer) return;

    directionsService.route({
      origin,
      destination,
      travelMode: google.maps.TravelMode.DRIVING
    }).then(response => {
      directionsRenderer.setDirections(response);
      const route = response.routes[0].legs[0];
      onRouteCalculated(route.distance?.text || "", route.duration?.text || "");
    }).catch(e => {
      console.error("Directions request failed", e);
    });
  }, [directionsService, directionsRenderer, origin, destination]);

  return null;
}
