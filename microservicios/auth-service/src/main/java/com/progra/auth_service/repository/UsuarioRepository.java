package com.progra.auth_service.repository;

import com.progra.auth_service.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Spring Boot crea la consulta SQL automáticamente por el nombre del método
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByRut(String rut);
}