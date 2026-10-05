package com.senai.aula08.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.senai.aula08.models.Telemetria;
public interface TelemetriaRepository extends JpaRepository<Telemetria, Long> {
    java.util.List<Telemetria> findByServicoId(Long idServico);
    boolean existsByContratoId(Long idContrato);
}
