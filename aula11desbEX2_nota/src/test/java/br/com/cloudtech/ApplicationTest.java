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
        mvc.perform(get("/documentacao")).andExpect(jsonPath("$.endpoints.length()").value(6));
        var resposta = mvc.perform(post("/livros/cadastrar").contentType(MediaType.APPLICATION_JSON)
            .content("{\"titulo\": \"Banco de Dados para Iniciantes\", \"autor\": \"Maria Souza\", \"editora\": \"EduTech\", \"ano\": 2023, \"categoria\": \"Informática\", \"status\": \"Disponível\"}")).andExpect(status().isCreated()).andReturn();
        long id = json.readTree(resposta.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(get("/livros")).andExpect(jsonPath("$[0].autor").value("Maria Souza"));
        mvc.perform(get("/livros/" + id)).andExpect(jsonPath("$.ano").value(2023));
        mvc.perform(put("/livros/alterar/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"titulo\": \"Banco de Dados para Iniciantes\", \"autor\": \"Maria Souza\", \"editora\": \"EduTech\", \"ano\": 2024, \"categoria\": \"Informática\", \"status\": \"Emprestado\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("Emprestado"));
        mvc.perform(get("/livros/" + id)).andExpect(jsonPath("$.ano").value(2024));
    }
    @Test void rejeitaDadosInvalidos() throws Exception {
        mvc.perform(get("/livros/999999")).andExpect(status().isNotFound());
        mvc.perform(post("/livros/cadastrar").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(put("/livros/alterar/999999").contentType(MediaType.APPLICATION_JSON).content("{\"titulo\": \"Banco de Dados para Iniciantes\", \"autor\": \"Maria Souza\", \"editora\": \"EduTech\", \"ano\": 2023, \"categoria\": \"Informática\", \"status\": \"Disponível\"}"))
            .andExpect(status().isNotFound());
    }

}
