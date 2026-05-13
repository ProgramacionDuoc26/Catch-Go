package com.catchandgo.auth.service;

import com.catchandgo.auth.dto.AuthResponseDto;
import com.catchandgo.auth.dto.LoginRequestDto;
import com.catchandgo.auth.dto.RegisterRequestDto;
import com.catchandgo.auth.entity.UserAccount;
import com.catchandgo.auth.mapper.UserAccountMapper;
import com.catchandgo.auth.repository.UserAccountRepository;
import com.catchandgo.common.jwt.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserAccountService {
    private final UserAccountRepository repository;
    private final UserAccountMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserAccountService(UserAccountRepository repository, UserAccountMapper mapper, 
                              PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponseDto register(RegisterRequestDto dto) {
        if (repository.findByEmail(dto.email()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        UserAccount entity = mapper.toEntity(dto);
        entity.setPassword(passwordEncoder.encode(dto.password()));
        
        UserAccount saved = repository.save(entity);
        
        String token = jwtService.generateToken(saved.getId().toString(), 86400); // 1 dia
        return new AuthResponseDto(token, mapper.toUserDto(saved));
    }

    public AuthResponseDto login(LoginRequestDto dto) {
        UserAccount user = repository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user.getId().toString(), 86400);
        return new AuthResponseDto(token, mapper.toUserDto(user));
    }

    public AuthResponseDto.UserDto findById(Long id) {
        UserAccount user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return mapper.toUserDto(user);
    }

    public boolean verifyPassword(Long id, String rawPassword) {
        UserAccount user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public void completarTrabajo(Long usuarioId) {
        UserAccount user = repository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        int nuevos = user.getTrabajosCompletados() + 1;
        user.setTrabajosCompletados(nuevos);
        user.setNivel(calcularNivel(nuevos));
        repository.save(user);
    }

    private int calcularNivel(int completados) {
        if (completados >= 200) return 7;
        if (completados >= 100) return 6;
        if (completados >= 50)  return 5;
        if (completados >= 25)  return 4;
        if (completados >= 10)  return 3;
        if (completados >= 3)   return 2;
        return 1;
    }
}
