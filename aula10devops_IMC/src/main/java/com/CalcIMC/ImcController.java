package com.CalcIMC;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ImcController {
    private final CalcIMC calculadora = new CalcIMC();

    @GetMapping("/imc")
    public ResultadoImc calcular(@RequestParam("peso") double peso,
                                @RequestParam("altura") double altura) {
        if (!Double.isFinite(peso) || !Double.isFinite(altura) || peso <= 0 || altura <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Peso e altura devem ser números positivos e finitos.");
        }
        double imc = calculadora.calcularIMC(peso, altura);
        if (!Double.isFinite(imc) || imc <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Valores fora do intervalo suportado para o cálculo.");
        }
        return new ResultadoImc(peso, altura, imc);
    }

    public record ResultadoImc(double peso, double altura, double imc) {}
}
