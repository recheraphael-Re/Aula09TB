package com.CalcIMC;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalcIMCTest {

    @Test
    void deveCalcularIMCComResultadoExato() {
        CalcIMC calculadora = new CalcIMC();

        double resultado = calculadora.calcularIMC(80.0, 2.0);

        assertEquals(20.0, resultado, 0.0001);
    }

    @Test
    void deveCalcularIMCComValoresDecimais() {
        CalcIMC calculadora = new CalcIMC();

        double resultado = calculadora.calcularIMC(70.0, 1.75);

        assertEquals(22.857142857, resultado, 0.0001);
    }
}
