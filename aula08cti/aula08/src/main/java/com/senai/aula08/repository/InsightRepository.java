package com.senai.aula08.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.senai.aula08.models.Insight;
public interface InsightRepository extends JpaRepository<Insight, Long> {
    boolean existsByContratoId(Long idContrato);
}
