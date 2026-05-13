package com.catchandgo.auth.repository;

import com.catchandgo.auth.entity.DocumentoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoUsuarioRepository extends JpaRepository<DocumentoUsuario, Long> {
    List<DocumentoUsuario> findByUsuarioId(Long usuarioId);
}
