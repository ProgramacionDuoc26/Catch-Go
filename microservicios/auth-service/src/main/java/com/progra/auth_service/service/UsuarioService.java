package com.progra.auth_service.service;

import com.progra.auth_service.model.Usuario;
import com.progra.auth_service.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario registrarUsuario(Usuario usuario) {
        // Todo: Más adelante aquí encriptaremos la contraseña con BCrypt
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado con ID: " + id));
    }
}