package com.senai.aula08.pratica;

public class Consultor extends Pessoa {
    private final String matricula;
    public Consultor(String nome, String matricula) { super(nome); this.matricula = matricula; }
    @Override public void exibirInformacoes() {
        super.exibirInformacoes();
        System.out.println("Matricula: " + matricula);
    }
}
