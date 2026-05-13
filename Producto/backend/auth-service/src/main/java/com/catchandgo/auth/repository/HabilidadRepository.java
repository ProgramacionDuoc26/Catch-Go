package com.catchandgo.auth.repository;

import com.catchandgo.auth.entity.Habilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HabilidadRepository extends JpaRepository<Habilidad, Long> {
    List<Habilidad> findByCategoriaId(Long categoriaId);
}
