package com.catchandgo.matching.mapper;

import com.catchandgo.matching.dto.MatchSuggestionDto;
import com.catchandgo.matching.entity.MatchSuggestion;
import org.springframework.stereotype.Component;

@Component
public class MatchSuggestionMapper {
    public MatchSuggestionDto toDto(MatchSuggestion entity) {
        return new MatchSuggestionDto(entity.getId(), entity.getName());
    }

    public MatchSuggestion toEntity(MatchSuggestionDto dto) {
        MatchSuggestion entity = new MatchSuggestion();
        entity.setId(dto.id());
        entity.setName(dto.name());
        return entity;
    }
}
