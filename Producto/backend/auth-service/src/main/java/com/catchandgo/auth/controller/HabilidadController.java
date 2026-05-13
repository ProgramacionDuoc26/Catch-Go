package com.catchandgo.auth.controller;

import com.catchandgo.auth.dto.*;
import com.catchandgo.auth.service.HabilidadService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class HabilidadController {

    private final HabilidadService service;

    public HabilidadController(HabilidadService service) {
        this.service = service;
    }

    @GetMapping("/habilidades/categorias")
    public List<CategoriaConHabilidadesDto> listarCategorias() {
        return service.listarCategorias();
    }

    @PostMapping("/habilidades")
    public HabilidadDto crearHabilidad(@RequestBody Map<String, Object> body) {
        String nombre = (String) body.get("nombre");
        Long categoriaId = Long.valueOf(body.get("categoriaId").toString());
        Long creadorId = Long.valueOf(body.get("creadorId").toString());
        return service.crearHabilidad(nombre, categoriaId, creadorId);
    }

    @GetMapping("/user/{id}/habilidades")
    public List<HabilidadUsuarioDto> getHabilidadesUsuario(@PathVariable("id") Long id) {
        return service.getHabilidadesUsuario(id);
    }

    @PostMapping("/user/{id}/habilidades")
    public HabilidadUsuarioDto asignarHabilidad(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        Long habilidadId = Long.valueOf(body.get("habilidadId").toString());
        Integer puntos = body.get("puntos") != null ? Integer.valueOf(body.get("puntos").toString()) : 50;
        return service.asignarHabilidad(id, habilidadId, puntos);
    }

    @GetMapping("/user/{id}/radar")
    public RadarDataDto getRadar(@PathVariable("id") Long id) {
        return service.getRadar(id);
    }
}
