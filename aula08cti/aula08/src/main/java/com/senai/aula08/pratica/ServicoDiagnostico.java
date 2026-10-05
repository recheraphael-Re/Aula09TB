package com.senai.aula08.pratica;

public class ServicoDiagnostico extends Servico {
    public ServicoDiagnostico(String nome) { super(nome, "Diagnostico"); }
    @Override public void executar() { System.out.println("Executando diagnostico do cliente..."); }
}
