package com.senai.aula08.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.senai.aula08.models.Usuario;


// O Spring Data JPA implementa os métodos de acesso ao banco
public interface UsuarioRepository extends  JpaRepository<Usuario,Long>{
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    
} 
    
        
    
    

