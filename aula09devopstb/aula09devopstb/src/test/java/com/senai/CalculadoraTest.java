package com.senai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class CalculadoraTest {
    // anotação para dizar que é uma função de teste
    @Test
    public void testeSomar() {
        // instanciando a classe Calculadora
        Calculadora calculadora = new Calculadora();
        // chamando a função somar da classe Calculadora
        int resultado = calculadora.somar(3, 2);
        // verificando se o resultado é igual a 5
        assertEquals(5, resultado);
    }
}
