package com.catchandgo.auth.service;

import com.catchandgo.auth.dto.UserAccountDto;
import com.catchandgo.auth.mapper.UserAccountMapper;
import com.catchandgo.auth.repository.UserAccountRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserAccountService {
    private final UserAccountRepository repository;
    private final UserAccountMapper mapper;

    public UserAccountService(UserAccountRepository repository, UserAccountMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<UserAccountDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public UserAccountDto create(UserAccountDto dto) {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }
}
