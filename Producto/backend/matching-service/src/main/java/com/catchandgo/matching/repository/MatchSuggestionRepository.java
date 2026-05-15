package com.catchandgo.matching.repository;

import com.catchandgo.matching.entity.MatchSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchSuggestionRepository extends JpaRepository<MatchSuggestion, Long> {
}
