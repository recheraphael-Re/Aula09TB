package com.senai.aula08.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.senai.aula08.models.Contrato;
public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    boolean existsByServicoId(Long idServico);
}
