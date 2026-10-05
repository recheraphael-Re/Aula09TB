package com.senai.aula08.pratica;

public class ServicoConsultoria extends Servico {
    public ServicoConsultoria(String nome) { super(nome, "Consultoria"); }
    @Override public void executar() { System.out.println("Executando consultoria especializada..."); }
}
