package com.senai.aula08.controller;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestControllerAdvice
public class IntegridadeHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,String>> conflito(DataIntegrityViolationException erro) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro",
            "Operacao viola uma restricao do banco. Verifique campos unicos e registros vinculados."));
    }
}
