package com.catchandgo.profile.service;

import com.catchandgo.profile.dto.ProfileDto;
import com.catchandgo.profile.mapper.ProfileMapper;
import com.catchandgo.profile.repository.ProfileRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final ProfileRepository repository;
    private final ProfileMapper mapper;

    public ProfileService(ProfileRepository repository, ProfileMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ProfileDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public ProfileDto create(ProfileDto dto) {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }
}
