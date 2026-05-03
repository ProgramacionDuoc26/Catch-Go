package com.catchandgo.matching.service;

import com.catchandgo.matching.dto.MatchSuggestionDto;
import com.catchandgo.matching.mapper.MatchSuggestionMapper;
import com.catchandgo.matching.repository.MatchSuggestionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MatchSuggestionService {
    private final MatchSuggestionRepository repository;
    private final MatchSuggestionMapper mapper;

    public MatchSuggestionService(MatchSuggestionRepository repository, MatchSuggestionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<MatchSuggestionDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public MatchSuggestionDto create(MatchSuggestionDto dto) {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }
}
