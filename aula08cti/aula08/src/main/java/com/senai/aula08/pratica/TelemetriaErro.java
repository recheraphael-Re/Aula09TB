package com.senai.aula08.pratica;

public class TelemetriaErro extends Telemetria {
    private final String mensagemErro;
    public TelemetriaErro(String evento, String mensagemErro) { super(evento, "ERRO"); this.mensagemErro = mensagemErro; }
    @Override public void exibirTelemetria() {
        super.exibirTelemetria(); System.out.println("Mensagem do erro: " + mensagemErro);
    }
}
