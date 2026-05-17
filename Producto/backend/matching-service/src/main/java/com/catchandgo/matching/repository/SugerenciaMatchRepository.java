package com.catchandgo.matching.repository;

import com.catchandgo.matching.entity.SugerenciaMatch;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SugerenciaMatchRepository extends JpaRepository<SugerenciaMatch, Long> {
    List<SugerenciaMatch> findByOfertaTrabajoIdOrderByPuntajeTotalDesc(Long ofertaTrabajoId);

    void deleteByOfertaTrabajoId(Long ofertaTrabajoId);
}
