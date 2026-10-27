package com.CalcIMC;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ImcApiTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void retornaCalculoEmJson() throws Exception {
        mvc.perform(get("/imc").param("peso", "70").param("altura", "1.75"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.peso").value(70.0))
                .andExpect(jsonPath("$.altura").value(1.75))
                .andExpect(jsonPath("$.imc").value(closeTo(22.857142857, 0.0001)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "peso=70&altura=0", "peso=-1&altura=1.75", "peso=70",
            "altura=1.75", "peso=abc&altura=1.75", "peso=70&altura=1,75",
            "peso=NaN&altura=1.75", "peso=70&altura=Infinity",
            "peso=70&altura=1e-300"
    })
    void rejeitaParametrosInvalidos(String parametros) throws Exception {
        mvc.perform(get("/imc?" + parametros)).andExpect(status().isBadRequest());
    }
}
