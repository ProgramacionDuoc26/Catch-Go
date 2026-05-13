package com.catchandgo.auth.repository;

import com.catchandgo.auth.entity.HabilidadUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HabilidadUsuarioRepository extends JpaRepository<HabilidadUsuario, Long> {
    List<HabilidadUsuario> findByUsuarioId(Long usuarioId);
    Optional<HabilidadUsuario> findByUsuarioIdAndHabilidadId(Long usuarioId, Long habilidadId);
}
