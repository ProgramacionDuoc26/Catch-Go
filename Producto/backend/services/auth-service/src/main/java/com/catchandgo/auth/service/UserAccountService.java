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

@Service
@SuppressWarnings("null")
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
        entity.setIsVerified(false);

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        entity.setVerificationOtp(otp);
        
        UserAccount saved = repository.save(entity);
        System.out.println("[EMAIL SIMULATOR] Enviando OTP a: " + dto.email() + " | Código: " + otp);
        
        return new AuthResponseDto(null, mapper.toUserDto(saved));
    }

    public AuthResponseDto login(LoginRequestDto dto) {
        UserAccount user = repository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        if (user.getIsVerified() == null || !user.getIsVerified()) {
            throw new RuntimeException("Debe verificar su cuenta ingresando el código OTP enviado a su correo");
        }

        String token = jwtService.generateToken(user.getId().toString(), 86400);
        return new AuthResponseDto(token, mapper.toUserDto(user));
    }

    public AuthResponseDto verifyOtp(String email, String otp) {
        UserAccount user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getIsVerified() != null && user.getIsVerified()) {
            throw new RuntimeException("La cuenta ya está verificada");
        }

        if (user.getVerificationOtp() == null || !user.getVerificationOtp().equals(otp)) {
            throw new RuntimeException("Código OTP inválido o vencido");
        }

        user.setIsVerified(true);
        user.setVerificationOtp(null);
        UserAccount saved = repository.save(user);

        String token = jwtService.generateToken(saved.getId().toString(), 86400);
        return new AuthResponseDto(token, mapper.toUserDto(saved));
    }

    public void resendOtp(String email) {
        UserAccount user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getIsVerified() != null && user.getIsVerified()) {
            throw new RuntimeException("La cuenta ya está verificada");
        }

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setVerificationOtp(otp);
        repository.save(user);

        System.out.println("[EMAIL SIMULATOR] Reenviando OTP a: " + email + " | Nuevo Código: " + otp);
    }

    public boolean verifyPassword(Long id, String rawPassword) {
        UserAccount user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public AuthResponseDto.UserDto findById(Long id) {
        UserAccount user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return mapper.toUserDto(user);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
