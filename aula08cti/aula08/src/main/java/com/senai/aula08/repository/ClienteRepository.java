package com.senai.aula08.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.senai.aula08.models.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente,Long> {
    
}
