package com.senai.aula08.models;

import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*; // Com asterisco ele importa todas as dependencias
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity // Relaciona esta classe a uma entidade persistente
@Table(name = "usuarios",uniqueConstraints = @UniqueConstraint (columnNames = "email"))
public class Usuario {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Identificador gerado pelo banco
    private  Long id;
    
    @Column(nullable = false, length = 120) // Coluna não pode ser nula e tem tamanho máximo de 100
    private String nome;

    @Column(nullable = false, length = 160)
    private  String email;
    @Column(nullable = false)
    private String senhaHash; // nunca armazenar a senha original

    @Enumerated(EnumType.STRING) // Armazena o valor do enum como String
    @Column(nullable = false, length = 20)
    private Perfil perfil = Perfil.USER; // Valor padrão do perfil

    // Cria o Construtor padrão (sem parâmetros) para o JPA
    public Usuario() {} // existe para o JPA, mas não é usado diretamente

    public Long getId(){return id;}
    public String getNome(){return nome;}
    public void setNome(String nome){this.nome = nome;}
    public String getEmail(){return email;}
    public void setEmail(String email){this.email = email;}
    public String getSenhaHash(){return  senhaHash;}
    public void setSenhaHash(String senhaHash){this.senhaHash = senhaHash;}
    public Perfil getPerfil(){return  perfil;}
    public  void setPerfil(Perfil perfil){this.perfil = perfil;}
}
