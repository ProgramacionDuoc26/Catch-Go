package com.catchandgo.matching.service;

import com.catchandgo.matching.dto.VentanaDisponibilidadDto;
import com.catchandgo.matching.dto.OfertaTrabajoMatchDto;
import com.catchandgo.matching.dto.TrabajadorMatchDto;
import com.catchandgo.matching.entity.SugerenciaMatch;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CalculadoraMatch {
    private static final double PESO_HABILIDADES = 0.40;
    private static final double PESO_EXPERIENCIA = 0.20;
    private static final double PESO_DISTANCIA = 0.20;
    private static final double PESO_DISPONIBILIDAD = 0.20;
    private static final double RADIO_TIERRA_KM = 6371.0;

    public SugerenciaMatch calcular(OfertaTrabajoMatchDto ofertaTrabajo, TrabajadorMatchDto trabajador) {
        double puntajeHabilidades = calcularPuntajeHabilidades(
            ofertaTrabajo.habilidadesRequeridas(),
            trabajador.habilidades()
        );
        double puntajeExperiencia = calcularPuntajeExperiencia(
            ofertaTrabajo.experienciaRequeridaMeses(),
            trabajador.experienciaMeses()
        );
        double puntajeDistancia = calcularPuntajeDistancia(ofertaTrabajo, trabajador);
        double puntajeDisponibilidad = calcularPuntajeDisponibilidad(
            ofertaTrabajo.disponibilidadRequerida(),
            trabajador.disponibilidad()
        );
        double puntajeTotal = redondear(
            puntajeHabilidades * PESO_HABILIDADES
                + puntajeExperiencia * PESO_EXPERIENCIA
                + puntajeDistancia * PESO_DISTANCIA
                + puntajeDisponibilidad * PESO_DISPONIBILIDAD
        );

        SugerenciaMatch sugerencia = new SugerenciaMatch();
        sugerencia.setOfertaTrabajoId(ofertaTrabajo.id());
        sugerencia.setTrabajadorPerfilId(trabajador.id());
        sugerencia.setNombreTrabajador(resolverNombreTrabajador(trabajador));
        sugerencia.setPuntajeTotal(puntajeTotal);
        sugerencia.setPuntajeHabilidades(puntajeHabilidades);
        sugerencia.setPuntajeExperiencia(puntajeExperiencia);
        sugerencia.setPuntajeDistancia(puntajeDistancia);
        sugerencia.setPuntajeDisponibilidad(puntajeDisponibilidad);
        return sugerencia;
    }

    double calcularPuntajeHabilidades(List<String> habilidadesRequeridas, List<String> habilidadesTrabajador) {
        Set<String> requeridas = normalizarConjunto(habilidadesRequeridas);
        if (requeridas.isEmpty()) {
            return 100.0;
        }

        Set<String> disponibles = normalizarConjunto(habilidadesTrabajador);
        long coincidencias = requeridas.stream().filter(disponibles::contains).count();
        return redondear((coincidencias * 100.0) / requeridas.size());
    }

    double calcularPuntajeExperiencia(Integer mesesRequeridos, Integer mesesTrabajador) {
        int mesesMinimos = mesesRequeridos == null ? 0 : mesesRequeridos;
        if (mesesMinimos <= 0) {
            return 100.0;
        }

        int experienciaTrabajador = Math.max(mesesTrabajador == null ? 0 : mesesTrabajador, 0);
        return redondear(Math.min(experienciaTrabajador / (double) mesesMinimos, 1.0) * 100.0);
    }

    double calcularPuntajeDistancia(OfertaTrabajoMatchDto ofertaTrabajo, TrabajadorMatchDto trabajador) {
        if (trabajador.tiempoViajeMinutos() != null) {
            return puntuarTiempoViaje(trabajador.tiempoViajeMinutos());
        }

        if (ofertaTrabajo.latitud() == null || ofertaTrabajo.longitud() == null
            || trabajador.latitud() == null || trabajador.longitud() == null) {
            return 0.0;
        }

        double distanciaKm = haversineKm(
            ofertaTrabajo.latitud(),
            ofertaTrabajo.longitud(),
            trabajador.latitud(),
            trabajador.longitud()
        );
        return puntuarDistancia(distanciaKm);
    }

    double calcularPuntajeDisponibilidad(
        List<VentanaDisponibilidadDto> disponibilidadRequerida,
        List<VentanaDisponibilidadDto> disponibilidadTrabajador
    ) {
        if (disponibilidadRequerida == null || disponibilidadRequerida.isEmpty()) {
            return 100.0;
        }
        if (disponibilidadTrabajador == null || disponibilidadTrabajador.isEmpty()) {
            return 0.0;
        }

        long minutosRequeridos = 0;
        long minutosCubiertos = 0;
        for (VentanaDisponibilidadDto ventanaRequeridaDto : disponibilidadRequerida) {
            Ventana ventanaRequerida = Ventana.from(ventanaRequeridaDto);
            if (ventanaRequerida == null) {
                continue;
            }

            long minutosVentanaRequerida = ventanaRequerida.duracionMinutos();
            minutosRequeridos += minutosVentanaRequerida;
            long minutosCubiertosVentana = disponibilidadTrabajador.stream()
                .map(Ventana::from)
                .filter(ventanaTrabajador -> ventanaTrabajador != null
                    && ventanaTrabajador.diaSemana() == ventanaRequerida.diaSemana())
                .mapToLong(ventanaRequerida::minutosSolapados)
                .sum();
            minutosCubiertos += Math.min(minutosCubiertosVentana, minutosVentanaRequerida);
        }

        if (minutosRequeridos == 0) {
            return 100.0;
        }
        return redondear(Math.min(minutosCubiertos / (double) minutosRequeridos, 1.0) * 100.0);
    }

    private Set<String> normalizarConjunto(List<String> valores) {
        Set<String> normalizados = new HashSet<>();
        if (valores == null) {
            return normalizados;
        }

        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                normalizados.add(valor.trim().toLowerCase(Locale.ROOT));
            }
        }
        return normalizados;
    }

    private String resolverNombreTrabajador(TrabajadorMatchDto trabajador) {
        if (trabajador.nombre() != null && !trabajador.nombre().isBlank()) {
            return trabajador.nombre().trim();
        }
        return "Trabajador " + trabajador.id();
    }

    private double puntuarTiempoViaje(int minutos) {
        if (minutos <= 30) {
            return 100.0;
        }
        if (minutos <= 45) {
            return 80.0;
        }
        if (minutos <= 60) {
            return 60.0;
        }
        if (minutos <= 90) {
            return 30.0;
        }
        return 0.0;
    }

    private double puntuarDistancia(double distanciaKm) {
        if (distanciaKm <= 5.0) {
            return 100.0;
        }
        if (distanciaKm <= 10.0) {
            return 80.0;
        }
        if (distanciaKm <= 20.0) {
            return 60.0;
        }
        if (distanciaKm <= 40.0) {
            return 30.0;
        }
        return 0.0;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat / 2), 2)
            + Math.cos(Math.toRadians(lat1))
            * Math.cos(Math.toRadians(lat2))
            * Math.pow(Math.sin(dLon / 2), 2);
        return RADIO_TIERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private record Ventana(DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        static Ventana from(VentanaDisponibilidadDto dto) {
            if (dto == null || dto.diaSemana() == null || dto.horaInicio() == null || dto.horaFin() == null) {
                return null;
            }

            LocalTime inicio = LocalTime.parse(dto.horaInicio());
            LocalTime fin = LocalTime.parse(dto.horaFin());
            if (!fin.isAfter(inicio)) {
                return null;
            }
            return new Ventana(parsearDia(dto.diaSemana()), inicio, fin);
        }

        long duracionMinutos() {
            return ChronoUnit.MINUTES.between(horaInicio, horaFin);
        }

        long minutosSolapados(Ventana otra) {
            LocalTime inicioSolape = horaInicio.isAfter(otra.horaInicio) ? horaInicio : otra.horaInicio;
            LocalTime finSolape = horaFin.isBefore(otra.horaFin) ? horaFin : otra.horaFin;
            if (!finSolape.isAfter(inicioSolape)) {
                return 0;
            }
            return ChronoUnit.MINUTES.between(inicioSolape, finSolape);
        }

        private static DayOfWeek parsearDia(String valor) {
            return switch (valor.trim().toLowerCase(Locale.ROOT)) {
                case "lunes", "monday" -> DayOfWeek.MONDAY;
                case "martes", "tuesday" -> DayOfWeek.TUESDAY;
                case "miercoles", "wednesday" -> DayOfWeek.WEDNESDAY;
                case "jueves", "thursday" -> DayOfWeek.THURSDAY;
                case "viernes", "friday" -> DayOfWeek.FRIDAY;
                case "sabado", "saturday" -> DayOfWeek.SATURDAY;
                case "domingo", "sunday" -> DayOfWeek.SUNDAY;
                default -> DayOfWeek.valueOf(valor.trim().toUpperCase(Locale.ROOT));
            };
        }
    }
}
