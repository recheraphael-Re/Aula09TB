package com.senai.aula08.dto;
import  jakarta.validation.constraints.NotBlank;
import  jakarta.validation.constraints.Email;
// DTO com as  credencias enviadas pelo cliente
public record LoginRequest(@NotBlank (message = "O email é obrigatório")
@Email(message="Informe um email válido")

        
     String email,
     @NotBlank (message = "A senha é obrigatória")
     
     String senha) {}
