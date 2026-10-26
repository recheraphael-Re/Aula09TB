package br.com.cloudtech;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTest {
    @Autowired MockMvc mvc;
    @Autowired com.fasterxml.jackson.databind.ObjectMapper json;
    
    @Test void fluxoCompleto() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/status")).andExpect(jsonPath("$.status").value("online"));
        mvc.perform(get("/versao")).andExpect(jsonPath("$.versao").value("1.0.0"));
        var resposta = mvc.perform(post("/maquinas/cadastro").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\": \"Máquina CNC 01\", \"tipo\": \"CNC\", \"setor\": \"Usinagem\", \"status\": \"em operação\"}")).andExpect(status().isCreated()).andReturn();
        long id = json.readTree(resposta.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(get("/maquinas")).andExpect(jsonPath("$[0].tipo").value("CNC"));
        String producao = "{\"maquinaId\":" + id + ",\"consumoKw\":12.5,\"horarioAcionamento\":\"08:30\",\"pecasProduzidas\":150}";
        mvc.perform(post("/producao").contentType(MediaType.APPLICATION_JSON).content(producao))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.pecasProduzidas").value(150));
        mvc.perform(post("/producao").contentType(MediaType.APPLICATION_JSON).content(producao))
            .andExpect(status().isCreated());
        mvc.perform(get("/producao").param("maquinaId", String.valueOf(id)))
            .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/producao")).andExpect(status().isOk());
    }
    @Test void rejeitaDadosInvalidos() throws Exception {
        mvc.perform(post("/maquinas/cadastro").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/producao").contentType(MediaType.APPLICATION_JSON)
            .content("{\"maquinaId\":999999,\"consumoKw\":-1,\"horarioAcionamento\":\"08:30\",\"pecasProduzidas\":1}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/producao").contentType(MediaType.APPLICATION_JSON).content("{\"maquinaId\": 999999, \"consumoKw\": 12.5, \"horarioAcionamento\": \"08:30\", \"pecasProduzidas\": 150}"))
            .andExpect(status().isNotFound());
        mvc.perform(get("/producao?maquinaId=999999")).andExpect(status().isNotFound());
    }

}
