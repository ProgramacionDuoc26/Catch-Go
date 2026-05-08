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
        return repository.findById(id)
                .map(mapper::toUserDto)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}
