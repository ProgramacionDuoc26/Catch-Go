package com.catchandgo.auth.service;

import com.catchandgo.auth.dto.AuthResponseDto;
import com.catchandgo.auth.dto.LoginRequestDto;
import com.catchandgo.auth.dto.RegisterRequestDto;
import com.catchandgo.auth.entity.UserAccount;
import com.catchandgo.auth.mapper.UserAccountMapper;
import com.catchandgo.auth.repository.UserAccountRepository;
import com.catchandgo.common.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class UserAccountServiceTest {

    @Mock
    private UserAccountRepository repository;

    @Mock
    private UserAccountMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserAccountService service;

    private UserAccount userAccount;
    private AuthResponseDto.UserDto userDto;

    @BeforeEach
    void setUp() {
        userAccount = new UserAccount();
        userAccount.setId(1L);
        userAccount.setEmail("test@email.com");
        userAccount.setName("Test User");
        userAccount.setPassword("encodedPassword");
        userAccount.setTipo("Trabajador");
        userAccount.setPhone("+56912345678");

        userDto = new AuthResponseDto.UserDto(
                1L,
                "test@email.com",
                "Test User",
                "Trabajador",
                "+56912345678"
        );
    }

    // CP-01 / CP-02: Registro de usuario exitoso (Trabajador o Empresa).
    // Verifica que si el correo no está registrado en el sistema, se mapee la petición,
    // se encripte la contraseña, se guarde en base de datos, se genere un token JWT válido
    // y retorne la estructura completa de respuesta exitosa.
    @Test
    void register_success() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "test@email.com",
                "Password123!",
                "Test User",
                "Trabajador",
                "+56912345678"
        );

        when(repository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(mapper.toEntity(dto)).thenReturn(userAccount);
        when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword");
        when(repository.save(userAccount)).thenReturn(userAccount);
        when(jwtService.generateToken(anyString(), anyLong())).thenReturn("mocked-jwt-token");
        when(mapper.toUserDto(userAccount)).thenReturn(userDto);

        AuthResponseDto response = service.register(dto);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.token());
        assertEquals(userDto, response.usuario());
        verify(repository).save(userAccount);
    }

    // CP-03 / CP-04: Registro fallido por correo ya registrado.
    // Verifica que si se intenta registrar una cuenta con un correo que ya existe en la base de datos,
    // el servicio lance una excepción RuntimeException informando el error y no realice la persistencia.
    @Test
    void register_emailAlreadyRegistered() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "test@email.com",
                "Password123!",
                "Test User",
                "Trabajador",
                "+56912345678"
        );

        when(repository.findByEmail(dto.email())).thenReturn(Optional.of(userAccount));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.register(dto);
        });

        assertEquals("El correo ya está registrado", exception.getMessage());
        verify(repository, never()).save(any());
    }

    // CP-05 / CP-06: Inicio de sesión exitoso.
    // Verifica que al proveer credenciales válidas (correo y contraseña correctos),
    // el sistema verifique el hash de contraseña, cree un token JWT válido y devuelva los datos de sesión.
    @Test
    void login_success() {
        LoginRequestDto dto = new LoginRequestDto("test@email.com", "Password123!");

        when(repository.findByEmail(dto.email())).thenReturn(Optional.of(userAccount));
        when(passwordEncoder.matches(dto.password(), userAccount.getPassword())).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyLong())).thenReturn("mocked-jwt-token");
        when(mapper.toUserDto(userAccount)).thenReturn(userDto);

        AuthResponseDto response = service.login(dto);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.token());
        assertEquals(userDto, response.usuario());
    }

    // CP-07 (Parte 1): Inicio de sesión fallido por correo no registrado.
    // Verifica que si se intenta iniciar sesión con un correo electrónico que no existe en la BD,
    // el servicio lance una excepción de credenciales inválidas.
    @Test
    void login_invalidEmail() {
        LoginRequestDto dto = new LoginRequestDto("notfound@email.com", "Password123!");

        when(repository.findByEmail(dto.email())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.login(dto);
        });

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    // CP-07 (Parte 2): Inicio de sesión fallido por contraseña incorrecta.
    // Verifica que si se intenta iniciar sesión con la contraseña incorrecta para un correo registrado,
    // el servicio lance una excepción de credenciales inválidas para proteger el acceso.
    @Test
    void login_invalidPassword() {
        LoginRequestDto dto = new LoginRequestDto("test@email.com", "WrongPassword");

        when(repository.findByEmail(dto.email())).thenReturn(Optional.of(userAccount));
        when(passwordEncoder.matches(dto.password(), userAccount.getPassword())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.login(dto);
        });

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    // Prueba de verificación de contraseña exitosa.
    // Verifica que al validar la contraseña actual de un usuario existente en la BD,
    // retorne true si coincide con la contraseña encriptada almacenada.
    @Test
    void verifyPassword_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(userAccount));
        when(passwordEncoder.matches("Password123!", userAccount.getPassword())).thenReturn(true);

        boolean result = service.verifyPassword(1L, "Password123!");

        assertTrue(result);
    }

    // Prueba de verificación de contraseña fallida.
    // Verifica que al validar la contraseña actual de un usuario existente,
    // retorne false si esta no coincide con la almacenada.
    @Test
    void verifyPassword_fail() {
        when(repository.findById(1L)).thenReturn(Optional.of(userAccount));
        when(passwordEncoder.matches("WrongPassword", userAccount.getPassword())).thenReturn(false);

        boolean result = service.verifyPassword(1L, "WrongPassword");

        assertFalse(result);
    }

    // Prueba de verificación de contraseña con usuario no existente.
    // Verifica que lance excepción si se intenta verificar la contraseña de un ID de usuario que no existe.
    @Test
    void verifyPassword_userNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.verifyPassword(1L, "anyPassword");
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    // Prueba de búsqueda de usuario por ID exitosa.
    // Verifica que el servicio recupere el usuario por ID y retorne su DTO mapeado correctamente.
    @Test
    void findById_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(userAccount));
        when(mapper.toUserDto(userAccount)).thenReturn(userDto);

        AuthResponseDto.UserDto result = service.findById(1L);

        assertNotNull(result);
        assertEquals(userDto, result);
    }

    // Prueba de búsqueda de usuario por ID fallida.
    // Verifica que lance una excepción 'Usuario no encontrado' si el ID consultado no existe.
    @Test
    void findById_userNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.findById(1L);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    // Prueba de eliminación de usuario exitosa.
    // Verifica que llame al repositorio para eliminar el usuario del sistema por su ID.
    @Test
    void deleteById_success() {
        doNothing().when(repository).deleteById(1L);

        service.deleteById(1L);

        verify(repository).deleteById(1L);
    }
}
