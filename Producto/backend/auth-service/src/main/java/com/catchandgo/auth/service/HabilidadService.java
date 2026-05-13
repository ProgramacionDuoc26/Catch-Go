package com.catchandgo.auth.service;

import com.catchandgo.auth.dto.*;
import com.catchandgo.auth.entity.*;
import com.catchandgo.auth.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HabilidadService {

    private final CategoriaHabilidadRepository categoriaRepo;
    private final HabilidadRepository habilidadRepo;
    private final HabilidadUsuarioRepository habilidadUsuarioRepo;

    public HabilidadService(CategoriaHabilidadRepository categoriaRepo,
                            HabilidadRepository habilidadRepo,
                            HabilidadUsuarioRepository habilidadUsuarioRepo) {
        this.categoriaRepo = categoriaRepo;
        this.habilidadRepo = habilidadRepo;
        this.habilidadUsuarioRepo = habilidadUsuarioRepo;
    }

    public List<CategoriaConHabilidadesDto> listarCategorias() {
        return categoriaRepo.findAll().stream().map(cat -> {
            List<HabilidadDto> habilidades = habilidadRepo.findByCategoriaId(cat.getId()).stream()
                .map(h -> new HabilidadDto(h.getId(), h.getNombre(), cat.getId(), cat.getNombre(), h.getPredeterminada()))
                .collect(Collectors.toList());
            return new CategoriaConHabilidadesDto(cat.getId(), cat.getNombre(), habilidades);
        }).collect(Collectors.toList());
    }

    public HabilidadDto crearHabilidad(String nombre, Long categoriaId, Long creadorId) {
        CategoriaHabilidad categoria = categoriaRepo.findById(categoriaId)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        Habilidad h = new Habilidad();
        h.setNombre(nombre);
        h.setCategoria(categoria);
        h.setPredeterminada(false);
        h.setCreadorUsuarioId(creadorId);
        Habilidad saved = habilidadRepo.save(h);
        return new HabilidadDto(saved.getId(), saved.getNombre(), categoria.getId(), categoria.getNombre(), false);
    }

    public List<HabilidadUsuarioDto> getHabilidadesUsuario(Long usuarioId) {
        return habilidadUsuarioRepo.findByUsuarioId(usuarioId).stream().map(hu -> {
            Habilidad h = hu.getHabilidad();
            return new HabilidadUsuarioDto(
                hu.getId(), h.getId(), h.getNombre(),
                h.getCategoria().getId(), h.getCategoria().getNombre(),
                hu.getPuntos(), hu.getTrabajosAplicados()
            );
        }).collect(Collectors.toList());
    }

    public HabilidadUsuarioDto asignarHabilidad(Long usuarioId, Long habilidadId, Integer puntos) {
        Habilidad habilidad = habilidadRepo.findById(habilidadId)
            .orElseThrow(() -> new RuntimeException("Habilidad no encontrada"));

        HabilidadUsuario hu = habilidadUsuarioRepo.findByUsuarioIdAndHabilidadId(usuarioId, habilidadId)
            .orElse(new HabilidadUsuario());

        hu.setUsuarioId(usuarioId);
        hu.setHabilidad(habilidad);
        hu.setPuntos(puntos != null ? Math.min(100, Math.max(0, puntos)) : 50);

        HabilidadUsuario saved = habilidadUsuarioRepo.save(hu);
        return new HabilidadUsuarioDto(
            saved.getId(), habilidad.getId(), habilidad.getNombre(),
            habilidad.getCategoria().getId(), habilidad.getCategoria().getNombre(),
            saved.getPuntos(), saved.getTrabajosAplicados()
        );
    }

    public RadarDataDto getRadar(Long usuarioId) {
        List<CategoriaHabilidad> categorias = categoriaRepo.findAll();
        List<HabilidadUsuario> userSkills = habilidadUsuarioRepo.findByUsuarioId(usuarioId);

        List<RadarDataDto.EjeRadarDto> ejes = categorias.stream().map(cat -> {
            double promedio = userSkills.stream()
                .filter(hu -> hu.getHabilidad().getCategoria().getId().equals(cat.getId()))
                .mapToInt(HabilidadUsuario::getPuntos)
                .average()
                .orElse(0.0);
            return new RadarDataDto.EjeRadarDto(cat.getId(), cat.getNombre(), promedio);
        }).collect(Collectors.toList());

        return new RadarDataDto(ejes);
    }
}
