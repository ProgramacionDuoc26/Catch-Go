package com.catchandgo.profile.service;

import com.catchandgo.profile.dto.ProfileDto;
import com.catchandgo.profile.entity.Profile;
import com.catchandgo.profile.mapper.ProfileMapper;
import com.catchandgo.profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private ProfileRepository repository;

    @Mock
    private ProfileMapper mapper;

    @InjectMocks
    private ProfileService service;

    private Profile profile;
    private ProfileDto profileDto;

    @BeforeEach
    void setUp() {
        profile = new Profile();
        profile.setId(1L);
        profile.setUserId("user-123");
        profile.setName("Juan Pérez");
        profile.setEmail("juan.perez@email.com");
        profile.setPhone("+56912345678");
        profile.setBirthDate(java.time.LocalDate.of(1995, 8, 20));
        profile.setPhotoUrl("http://photo.com");
        profile.setCvUrl("http://cv.com");
        profile.setDescription("Electricista con experiencia");
        profile.setAddress("Calle Falsa 123");
        profile.setType("Trabajador");
        profile.setLatitude(-33.456);
        profile.setLongitude(-70.648);
        profile.setSkills("Electricidad, Reparaciones");
        profile.setRut("12345678-9");
        profile.setRating(5.0);
        profile.setRatingCount(1);

        profileDto = new ProfileDto();
        profileDto.setId(1L);
        profileDto.setUserId("user-123");
        profileDto.setName("Juan Pérez");
        profileDto.setEmail("juan.perez@email.com");
        profileDto.setPhone("+56912345678");
        profileDto.setBirthDate("1995-08-20");
        profileDto.setPhotoUrl("http://photo.com");
        profileDto.setCvUrl("http://cv.com");
        profileDto.setDescription("Electricista con experiencia");
        profileDto.setAddress("Calle Falsa 123");
        profileDto.setType("Trabajador");
        profileDto.setLatitude(-33.456);
        profileDto.setLongitude(-70.648);
        profileDto.setSkills("Electricidad, Reparaciones");
        profileDto.setRut("12345678-9");
        profileDto.setRating(5.0);
        profileDto.setRatingCount(1);
    }

    // Búsqueda de todos los perfiles de usuario.
    // Verifica que el servicio devuelva la lista completa de perfiles en DTOs.
    @Test
    void findAll_success() {
        when(repository.findAll()).thenReturn(List.of(profile));
        when(mapper.toDto(profile)).thenReturn(profileDto);

        List<ProfileDto> result = service.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(profileDto, result.get(0));
    }

    // CP-09 / CP-10: Búsqueda de perfil por ID de usuario exitosa.
    // Verifica que al proveer un ID de usuario registrado, el servicio retorne su perfil en DTO.
    @Test
    void findByUserId_found() {
        when(repository.findFirstByUserId("user-123")).thenReturn(Optional.of(profile));
        when(mapper.toDto(profile)).thenReturn(profileDto);

        ProfileDto result = service.findByUserId("user-123");

        assertNotNull(result);
        assertEquals(profileDto, result);
    }

    // Búsqueda de perfil por ID de usuario no existente.
    // Verifica que retorne null si el ID de usuario no existe en la base de datos.
    @Test
    void findByUserId_notFound() {
        when(repository.findFirstByUserId("user-456")).thenReturn(Optional.empty());

        ProfileDto result = service.findByUserId("user-456");

        assertNull(result);
    }

    // CP-09 / CP-10: Creación de perfil nuevo (saveOrUpdate).
    // Verifica que si el perfil no existe para ese userId, se cree una nueva entidad,
    // se seteen los datos del DTO y se guarde de forma correcta en el repositorio.
    @Test
    void saveOrUpdate_newProfile() {
        when(repository.findFirstByUserId("user-123")).thenReturn(Optional.empty());
        when(repository.save(any(Profile.class))).thenReturn(profile);
        when(mapper.toDto(profile)).thenReturn(profileDto);

        ProfileDto result = service.saveOrUpdate(profileDto);

        assertNotNull(result);
        assertEquals(profileDto, result);
        verify(repository).save(any(Profile.class));
    }

    // CP-09 / CP-10: Actualización de perfil existente (saveOrUpdate).
    // Verifica que si el perfil ya existe en BD, el servicio recupere la entidad actual,
    // sobrescriba sus campos con los nuevos datos del DTO y lo guarde actualizado.
    @Test
    void saveOrUpdate_existingProfile() {
        when(repository.findFirstByUserId("user-123")).thenReturn(Optional.of(profile));
        when(repository.save(profile)).thenReturn(profile);
        when(mapper.toDto(profile)).thenReturn(profileDto);

        ProfileDto result = service.saveOrUpdate(profileDto);

        assertNotNull(result);
        assertEquals(profileDto, result);
        verify(repository).save(profile);
    }

    // Validación de userId inválido en saveOrUpdate.
    // Verifica que lance IllegalArgumentException si se intenta guardar un DTO nulo o con userId vacío.
    @Test
    void saveOrUpdate_invalidUserId() {
        // Caso DTO nulo
        assertThrows(IllegalArgumentException.class, () -> {
            service.saveOrUpdate(null);
        });

        // Caso userId vacío
        ProfileDto invalidDto = new ProfileDto();
        invalidDto.setUserId("   ");
        assertThrows(IllegalArgumentException.class, () -> {
            service.saveOrUpdate(invalidDto);
        });
    }

    // Eliminación de perfil de usuario exitosa.
    // Verifica que al pasar un userId registrado, el servicio recupere la entidad y llame a eliminar.
    @Test
    void deleteByUserId_success() {
        when(repository.findFirstByUserId("user-123")).thenReturn(Optional.of(profile));
        doNothing().when(repository).delete(profile);

        service.deleteByUserId("user-123");

        verify(repository).delete(profile);
    }
}
