package com.senai.aula08.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.senai.aula08.models.Servico;
public interface ServicoRepository extends JpaRepository<Servico, Long> {
}
