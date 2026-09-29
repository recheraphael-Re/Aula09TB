package com.senai.aula08.service;

import org.springframework.stereotype.Service;

import com.senai.aula08.repository.ClienteRepository;
import com.senai.aula08.repository.ConsultorRepository;

@Service 
public class ClienteService {

    // Cria variaveis clienterepository e consultorrepository

    private  final ClienteRepository clienteRepository;
    private  final ConsultorRepository consultorRepository;

    // Cria o construtor
    public ClienteService(
        ClienteRepository clienteRepository,
        ConsultorRepository consultorRepository
    ){
        this.clienteRepository = clienteRepository;
        this.consultorRepository = consultorRepository;
    }

// Create 

    @Transactional 
    public Cliente criar(Long idConsultor, Cliente cliente){

        // Primeiro verifica se o consultor existe

        Consultor consultor = consultorRepository.findById(idConsultor).orElseThrow(
            ()->new RuntimeException("Consultor não encontrado")
        );
    }

    
    
}
