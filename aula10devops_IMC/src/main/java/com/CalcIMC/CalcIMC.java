package com.CalcIMC;

public class CalcIMC {

    // Peso em quilogramas e altura em metros.
    public double calcularIMC(double peso, double altura) {
        return peso / (altura * altura);
    }
}
