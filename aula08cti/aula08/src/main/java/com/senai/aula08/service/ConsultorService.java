package com.senai.aula08.service;

import javax.management.RuntimeErrorException;

import org.springframework.stereotype.Service; // Biblioteca que permite colocar a anotação service

import com.senai.aula08.models.Consultor;

import jakarta.transaction.Transactional;

// Anotação de service é onde vai ter as regras de negocio

@Service 
public class ConsultorService {

    // Cria a variavel Consultor repository

    private final ConsultorRepository repository; // cria a variavel repository que permite manipular o banco de dados


    // Cria o construtor

    public ConsultorService(
        ConsultorRepository repository){
            this.repository = repository;
        }


    // ======
    // CREATE
    // ======

    @Transactional 
    public Consultor criar(Consultor consultor){
        if(consultor.getNome() == null || consultor.getNome().isBlank()){
            throw new RuntimeException(
                "Nome é obrigatório !"
            );
        }


        if(consultor.getEmail() == null || consultor.getEmail().isBlank()){

            throw new RuntimeException(
                "Email é obrigatório !"
            );

        }


        if(consultor.getSenha() == null || consultor.getSenha().isBlank()){
            throw new RuntimeException(
                "Senha é obrigatória !"
            );
        }



        // Verifica se já exise consultor com o mesmo email


        if(repository.findByEmail(consultor.getEmail()).isPresent()){
            throw new RuntimeException("Email já cadastrado");
        }
        return repository.save(consultor);

    }


// Login 

// Cria a função

public Consultor login(String email, String senha){

    Consultor consultor = repository.findByEmail(email).orElseThrow(()->new RuntimeException("Consultor não encontrado"));


    // Validação
    if(!consultor.getSenha().equals(senha)){
        throw new RuntimeException("Senha inválida");
    }

    return consultor;

}


// READ - todos

public List<Consultor> listar(){
    return repository.findAll();
}

// READ por ID

public Consultor buscarPorId(Long id){
    return repository.findById(id).orElseThrow(()->new RuntimeException("Consultor não encontrado"));
}


// UPDATE

@Transactional 
public Consultor atualizar(
    Long id, Consultor dados
){

    Consultor consultor = buscarPorId(id);


    consultor.setNome(dados.getNome()); // pega o nome do consultor


    consultor.setEmail(dados.getEmail());


    consultor.setSenha(dados.getSenha());


    return  repository.save(consultor);
}


// Delete

@Transactional 
public void excluir(Long id){
    Consultor consultor = buscarPorId(id);

    repository.deleteById(
        consultor.getIdLong() );
}
    
}
