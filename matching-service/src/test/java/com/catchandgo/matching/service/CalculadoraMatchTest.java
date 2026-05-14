package com.catchandgo.matching.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.catchandgo.matching.dto.VentanaDisponibilidadDto;
import com.catchandgo.matching.dto.OfertaTrabajoMatchDto;
import com.catchandgo.matching.dto.TrabajadorMatchDto;
import com.catchandgo.matching.entity.SugerenciaMatch;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalculadoraMatchTest {
    private final CalculadoraMatch calculadora = new CalculadoraMatch();

    @Test
    void calculaConPesosConfigurados() {
        OfertaTrabajoMatchDto oferta = new OfertaTrabajoMatchDto(
            10L,
            "Operario de bodega",
            20L,
            List.of("bodega", "carga"),
            12,
            -33.4489,
            -70.6693,
            List.of(new VentanaDisponibilidadDto("lunes", "08:00", "16:00"))
        );
        TrabajadorMatchDto trabajador = new TrabajadorMatchDto(
            30L,
            "Camila Rojas",
            List.of("bodega", "carga", "aseo"),
            18,
            -33.4489,
            -70.6693,
            null,
            List.of(new VentanaDisponibilidadDto("lunes", "07:00", "17:00"))
        );

        SugerenciaMatch sugerencia = calculadora.calcular(oferta, trabajador);

        assertThat(sugerencia.getPuntajeHabilidades()).isEqualTo(100.0);
        assertThat(sugerencia.getPuntajeExperiencia()).isEqualTo(100.0);
        assertThat(sugerencia.getPuntajeDistancia()).isEqualTo(100.0);
        assertThat(sugerencia.getPuntajeDisponibilidad()).isEqualTo(100.0);
        assertThat(sugerencia.getPuntajeTotal()).isEqualTo(100.0);
    }

    @Test
    void calculaPuntajesParciales() {
        OfertaTrabajoMatchDto oferta = new OfertaTrabajoMatchDto(
            10L,
            "Apoyo evento",
            20L,
            List.of("atencion", "caja", "inventario"),
            12,
            -33.4489,
            -70.6693,
            List.of(new VentanaDisponibilidadDto("martes", "08:00", "16:00"))
        );
        TrabajadorMatchDto trabajador = new TrabajadorMatchDto(
            30L,
            "Luis Perez",
            List.of("caja"),
            6,
            null,
            null,
            50,
            List.of(new VentanaDisponibilidadDto("martes", "08:00", "12:00"))
        );

        SugerenciaMatch sugerencia = calculadora.calcular(oferta, trabajador);

        assertThat(sugerencia.getPuntajeHabilidades()).isEqualTo(33.33);
        assertThat(sugerencia.getPuntajeExperiencia()).isEqualTo(50.0);
        assertThat(sugerencia.getPuntajeDistancia()).isEqualTo(60.0);
        assertThat(sugerencia.getPuntajeDisponibilidad()).isEqualTo(50.0);
        assertThat(sugerencia.getPuntajeTotal()).isEqualTo(45.33);
    }
}
