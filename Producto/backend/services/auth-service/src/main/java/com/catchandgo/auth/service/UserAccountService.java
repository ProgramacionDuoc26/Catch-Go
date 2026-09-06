package com.catchandgo.auth.service;

import com.catchandgo.auth.dto.*;
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
    private final CaptchaService captchaService;
    private final OtpService otpService;

    public UserAccountService(UserAccountRepository repository, UserAccountMapper mapper, 
                              PasswordEncoder passwordEncoder, JwtService jwtService,
                              CaptchaService captchaService, OtpService otpService) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.captchaService = captchaService;
        this.otpService = otpService;
    }

    public AuthResponseDto register(RegisterRequestDto dto) {
        captchaService.validateCaptcha(dto.captchaToken());

        if (repository.findByEmail(dto.email()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        if (dto.otpCode() != null && !dto.otpCode().isBlank()) {
            boolean valid = otpService.verifyOtp(dto.email(), dto.otpCode(), "REGISTER");
            if (!valid) {
                throw new RuntimeException("El código de verificación enviado al correo es inválido o expiró");
            }
        }

        UserAccount entity = mapper.toEntity(dto);
        entity.setPassword(passwordEncoder.encode(dto.password()));
        
        UserAccount saved = repository.save(entity);
        
        String token = jwtService.generateToken(saved.getId().toString(), 86400); // 1 dia
        return new AuthResponseDto(token, mapper.toUserDto(saved));
    }

    public AuthResponseDto login(LoginRequestDto dto) {
        captchaService.validateCaptcha(dto.captchaToken());

        UserAccount user = repository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user.getId().toString(), 86400);
        return new AuthResponseDto(token, mapper.toUserDto(user));
    }

    public String sendOtp(SendOtpRequestDto dto) {
        if ("REGISTER".equalsIgnoreCase(dto.purpose())) {
            if (repository.findByEmail(dto.email()).isPresent()) {
                throw new RuntimeException("El correo ya está registrado");
            }
        }
        return otpService.generateAndSendOtp(dto.email(), dto.purpose());
    }

    public boolean verifyOtp(VerifyOtpRequestDto dto) {
        return otpService.verifyOtp(dto.email(), dto.code(), dto.purpose());
    }

    public String forgotPassword(ForgotPasswordRequestDto dto) {
        captchaService.validateCaptcha(dto.captchaToken());

        UserAccount user = repository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("No se encontró ninguna cuenta registrada con el correo indicado"));

        otpService.generateAndSendOtp(user.getEmail(), "RESET_PASSWORD");
        return "Se ha enviado un código de verificación de 6 dígitos a tu correo electrónico.";
    }

    public String resetPassword(ResetPasswordRequestDto dto) {
        boolean validOtp = otpService.verifyOtp(dto.email(), dto.code(), "RESET_PASSWORD");
        if (!validOtp) {
            throw new RuntimeException("El código de verificación al correo es inválido o ha expirado");
        }

        UserAccount user = repository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        repository.save(user);

        return "Contraseña actualizada exitosamente. Ahora puedes iniciar sesión con tu nueva clave.";
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
