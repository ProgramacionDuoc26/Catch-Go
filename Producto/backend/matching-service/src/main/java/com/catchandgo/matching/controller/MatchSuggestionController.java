package com.catchandgo.matching.controller;

import com.catchandgo.matching.dto.MatchSuggestionDto;
import com.catchandgo.matching.service.MatchSuggestionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matching")
public class MatchSuggestionController {
    private final MatchSuggestionService service;

    public MatchSuggestionController(MatchSuggestionService service) {
        this.service = service;
    }

    @GetMapping
    public List<MatchSuggestionDto> findAll() {
        return service.findAll();
    }

    @PostMapping
    public MatchSuggestionDto create(@RequestBody MatchSuggestionDto dto) {
        return service.create(dto);
    }
}
