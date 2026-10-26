import json
from pathlib import Path

def write(base, name, content):
    path = Path(base) / name
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding='utf-8')

projects = [('exercicio1-monitoramento-maquinas', 'monitoramento', 8080), ('exercicio2-biblioteca', 'biblioteca', 8081)]
for base, artifact, port in projects:
    write(base, 'pom.xml', f'''<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-parent</artifactId><version>3.4.2</version><relativePath/></parent>
  <groupId>br.com.cloudtech</groupId><artifactId>{artifact}</artifactId><version>1.0.0</version>
  <properties><java.version>21</java.version></properties>
  <dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
  </dependencies>
  <build><plugins><plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin></plugins></build>
</project>
''')
    write(base, 'src/main/resources/application.properties', f'spring.application.name={artifact}\nserver.port={port}\nserver.error.include-message=always\n')
    write(base, 'src/main/java/br/com/cloudtech/Application.java', '''package br.com.cloudtech;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class Application {
    public static void main(String[] args) { SpringApplication.run(Application.class, args); }
}
''')
    write(base, '.gitignore', 'target/\n.idea/\n.vscode/\n*.log\n')
    write(base, '.github/workflows/pipeline.yml', '''name: Testes Java
on: [push, pull_request]
jobs:
  testes:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      - run: mvn -B verify
''')
    write(base, 'Dockerfile', f'''FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn -B package
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/{artifact}-1.0.0.jar app.jar
EXPOSE {port}
ENTRYPOINT ["java", "-jar", "app.jar"]
''')

write(projects[0][0], 'src/main/java/br/com/cloudtech/MaquinaController.java', '''package br.com.cloudtech;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MaquinaController {
    public record Cadastro(@NotBlank String nome, @NotBlank String tipo,
                           @NotBlank String setor, @NotBlank String status) {}
    public record Maquina(long id, String nome, String tipo, String setor, String status) {}
    public record DadosProducao(@NotNull @Positive Long maquinaId,
        @NotNull @DecimalMin("0") BigDecimal consumoKw,
        @NotNull LocalTime horarioAcionamento, @NotNull @PositiveOrZero Integer pecasProduzidas) {}
    public record Producao(long id, long maquinaId, BigDecimal consumoKw,
        LocalTime horarioAcionamento, int pecasProduzidas, LocalDateTime registradoEm) {}
    private final Map<Long, Maquina> maquinas = new ConcurrentHashMap<>();
    private final List<Producao> historico = new CopyOnWriteArrayList<>();
    private final AtomicLong ids = new AtomicLong();
    private final AtomicLong registros = new AtomicLong();

    @GetMapping("/")
    public Map<String, String> inicio() { return Map.of("mensagem", "Bem-vindo ao monitoramento de máquinas da CloudTech Solutions"); }
    @GetMapping("/status")
    public Map<String, String> status() { return Map.of("status", "online", "mensagem", "Aplicação de monitoramento em execução"); }
    @GetMapping("/versao")
    public Map<String, String> versao() { return Map.of("versao", "1.0.0"); }
    @GetMapping("/maquinas")
    public List<Maquina> listar() { return maquinas.values().stream().sorted(Comparator.comparingLong(Maquina::id)).toList(); }
    @PostMapping("/maquinas/cadastro")
    public ResponseEntity<Maquina> cadastrar(@Valid @RequestBody Cadastro dados) {
        long id = ids.incrementAndGet();
        Maquina maquina = new Maquina(id, dados.nome(), dados.tipo(), dados.setor(), dados.status());
        maquinas.put(id, maquina);
        return ResponseEntity.status(HttpStatus.CREATED).body(maquina);
    }
    @PostMapping("/producao")
    public ResponseEntity<Producao> registrar(@Valid @RequestBody DadosProducao dados) {
        verificarMaquina(dados.maquinaId());
        Producao producao = new Producao(registros.incrementAndGet(), dados.maquinaId(), dados.consumoKw(),
            dados.horarioAcionamento(), dados.pecasProduzidas(), LocalDateTime.now());
        historico.add(producao);
        return ResponseEntity.status(HttpStatus.CREATED).body(producao);
    }
    @GetMapping("/producao")
    public List<Producao> consultar(@RequestParam(required = false) Long maquinaId) {
        if (maquinaId != null) verificarMaquina(maquinaId);
        return historico.stream().filter(p -> maquinaId == null || p.maquinaId() == maquinaId).toList();
    }
    private void verificarMaquina(long id) {
        if (!maquinas.containsKey(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Máquina não encontrada");
    }
}
''')
write(projects[1][0], 'src/main/java/br/com/cloudtech/LivroController.java', '''package br.com.cloudtech;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class LivroController {
    public record DadosLivro(@NotBlank String titulo, @NotBlank String autor,
        @NotBlank String editora, @NotNull @Min(1) @Max(9999) Integer ano,
        @NotBlank String categoria, @NotBlank String status) {}
    public record Livro(long id, String titulo, String autor, String editora,
                        int ano, String categoria, String status) {}
    private final Map<Long, Livro> livros = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();
    @GetMapping("/")
    public Map<String, String> inicio() {
        return Map.of("mensagem", "Bem-vindo ao sistema de gerenciamento da Biblioteca CloudTech Solutions");
    }
    @GetMapping("/livros")
    public List<Livro> listar() { return livros.values().stream().sorted(Comparator.comparingLong(Livro::id)).toList(); }
    @GetMapping("/livros/{id}")
    public Livro buscar(@PathVariable long id) {
        Livro livro = livros.get(id);
        if (livro == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado");
        return livro;
    }
    @PostMapping("/livros/cadastrar")
    public ResponseEntity<Livro> cadastrar(@Valid @RequestBody DadosLivro dados) {
        long id = ids.incrementAndGet();
        Livro livro = converter(id, dados);
        livros.put(id, livro);
        return ResponseEntity.status(HttpStatus.CREATED).body(livro);
    }
    @PutMapping("/livros/alterar/{id}")
    public Livro alterar(@PathVariable long id, @Valid @RequestBody DadosLivro dados) {
        Livro atualizado = livros.computeIfPresent(id, (chave, anterior) -> converter(id, dados));
        if (atualizado == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado");
        return atualizado;
    }
    @GetMapping("/documentacao")
    public Map<String, List<String>> documentacao() {
        return Map.of("endpoints", List.of("/", "/livros", "/livros/cadastrar", "/livros/alterar/{id}", "/livros/{id}", "/documentacao"));
    }
    private Livro converter(long id, DadosLivro d) {
        return new Livro(id, d.titulo(), d.autor(), d.editora(), d.ano(), d.categoria(), d.status());
    }
}
''')
common_test = '''package br.com.cloudtech;
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
    BODY
}
'''

machine = {"nome":"Máquina CNC 01","tipo":"CNC","setor":"Usinagem","status":"em operação"}
production = {"maquinaId":1,"consumoKw":12.5,"horarioAcionamento":"08:30","pecasProduzidas":150}
book = {"titulo":"Banco de Dados para Iniciantes","autor":"Maria Souza","editora":"EduTech","ano":2023,"categoria":"Informática","status":"Disponível"}

def literal(value):
    return json.dumps(json.dumps(value, ensure_ascii=False), ensure_ascii=False)

machine_test = '''
    @Test void fluxoCompleto() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/status")).andExpect(jsonPath("$.status").value("online"));
        mvc.perform(get("/versao")).andExpect(jsonPath("$.versao").value("1.0.0"));
        var resposta = mvc.perform(post("/maquinas/cadastro").contentType(MediaType.APPLICATION_JSON)
            .content(MACHINE)).andExpect(status().isCreated()).andReturn();
        long id = json.readTree(resposta.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(get("/maquinas")).andExpect(jsonPath("$[0].tipo").value("CNC"));
        String producao = "{\\"maquinaId\\":" + id + ",\\"consumoKw\\":12.5,\\"horarioAcionamento\\":\\"08:30\\",\\"pecasProduzidas\\":150}";
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
            .content("{\\"maquinaId\\":999999,\\"consumoKw\\":-1,\\"horarioAcionamento\\":\\"08:30\\",\\"pecasProduzidas\\":1}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/producao").contentType(MediaType.APPLICATION_JSON).content(PRODUCTION))
            .andExpect(status().isNotFound());
        mvc.perform(get("/producao?maquinaId=999999")).andExpect(status().isNotFound());
    }
'''.replace('MACHINE', literal(machine)).replace('PRODUCTION', literal(dict(production, maquinaId=999999)))
book_test = '''
    @Test void fluxoCompleto() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/documentacao")).andExpect(jsonPath("$.endpoints.length()").value(6));
        var resposta = mvc.perform(post("/livros/cadastrar").contentType(MediaType.APPLICATION_JSON)
            .content(BOOK)).andExpect(status().isCreated()).andReturn();
        long id = json.readTree(resposta.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(get("/livros")).andExpect(jsonPath("$[0].autor").value("Maria Souza"));
        mvc.perform(get("/livros/" + id)).andExpect(jsonPath("$.ano").value(2023));
        mvc.perform(put("/livros/alterar/" + id).contentType(MediaType.APPLICATION_JSON).content(UPDATED))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("Emprestado"));
        mvc.perform(get("/livros/" + id)).andExpect(jsonPath("$.ano").value(2024));
    }
    @Test void rejeitaDadosInvalidos() throws Exception {
        mvc.perform(get("/livros/999999")).andExpect(status().isNotFound());
        mvc.perform(post("/livros/cadastrar").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(put("/livros/alterar/999999").contentType(MediaType.APPLICATION_JSON).content(BOOK))
            .andExpect(status().isNotFound());
    }
'''.replace('BOOK', literal(book)).replace('UPDATED', literal(dict(book, ano=2024, status='Emprestado')))

for (base, artifact, port), body in zip(projects, [machine_test, book_test]):
    write(base, 'src/test/java/br/com/cloudtech/ApplicationTest.java', common_test.replace('BODY', body))
    samples = {'maquina': machine, 'producao': production} if port == 8080 else {'livro': book, 'livro-alterado': dict(book, ano=2024, status='Emprestado')}
    for name, data in samples.items():
        write(base, f'tests/{name}.json', json.dumps(data, ensure_ascii=False, indent=2) + '\n')
    endpoints = [ ('GET', '/', '200'), ('GET', '/status', '200'), ('GET', '/versao', '200'), ('GET', '/maquinas', '200'), ('POST', '/maquinas/cadastro', '201'), ('POST', '/producao', '201'), ('GET', '/producao', '200'), ('GET', '/producao?maquinaId=1', '200') ] if port == 8080 else [ ('GET', '/', '200'), ('GET', '/livros', '200'), ('POST', '/livros/cadastrar', '201'), ('PUT', '/livros/alterar/1', '200'), ('GET', '/livros/1', '200'), ('GET', '/documentacao', '200') ]
    rows = '\n'.join(f'| {method} | `{path}` | {status} |' for method, path, status in endpoints)
    requests = []
    for method, path, status in endpoints:
        data = None
        if method == 'POST': data = machine if 'cadastro' in path else production if path == '/producao' else book
        if method == 'PUT': data = dict(book, ano=2024, status='Emprestado')
        requests.append(f'### Esperado: HTTP {status}\n{method} http://localhost:{port}{path}' + ('\nContent-Type: application/json\n\n' + json.dumps(data, ensure_ascii=False, indent=2) if data else '') + '\n')
    write(base, 'tests/requisicoes.http', '\n'.join(requests))
    example = 'curl.exe -i -X POST http://localhost:8080/maquinas/cadastro -H "Content-Type: application/json" --data-binary "@tests/maquina.json"\ncurl.exe -i -X POST http://localhost:8080/producao -H "Content-Type: application/json" --data-binary "@tests/producao.json"\ncurl.exe http://localhost:8080/producao?maquinaId=1' if port == 8080 else 'curl.exe -i -X POST http://localhost:8081/livros/cadastrar -H "Content-Type: application/json" --data-binary "@tests/livro.json"\ncurl.exe -i -X PUT http://localhost:8081/livros/alterar/1 -H "Content-Type: application/json" --data-binary "@tests/livro-alterado.json"\ncurl.exe http://localhost:8081/livros/1'
    write(base, 'README.md', f'''# {'Exercício 1 — Monitoramento de máquinas' if port == 8080 else 'Exercício 2 — Biblioteca'}

Aplicação Java 21 e Spring Boot da CloudTech Solutions. Interface JSON conforme o enunciado da Aula 11.

## 1. Executar

Requisitos: JDK 21 e Maven 3.9 (comandos executados nesta pasta).

```powershell
mvn clean verify
mvn spring-boot:run
```

Acesse http://localhost:{port}. Use Ctrl+C para encerrar. Os cadastros e o histórico ficam em memória: ao reiniciar, os dados são apagados. IDs são gerados pelo servidor. Não há autenticação; esta é uma aplicação didática.

## 2. Endpoints

| Método | Caminho | HTTP esperado |
| --- | --- | --- |
{rows}

Cadastro e alteração recebem JSON. Campos obrigatórios vazios, números negativos de produção e horários inválidos retornam 400. IDs inexistentes retornam 404. Alteração de livro usa PUT com todos os campos. A listagem de livros inclui todos os status para gerenciamento do acervo.

## 3. Reproduzir no terminal

Com a aplicação em execução, abra outro terminal nesta pasta:

```powershell
{example}
```

Em uma execução nova, o primeiro cadastro recebe `id: 1`. Cadastro retorna HTTP 201 e os campos enviados junto com o ID. Consulta retorna HTTP 200. {'Cada registro de produção recebe ID e data/hora; GET /producao retorna a lista histórica de consumo e peças, e aceita filtro maquinaId.' if port == 8080 else 'Após o PUT, a consulta retorna ano 2024 e status Emprestado, mantendo o ID.'}

## 4. Testes e Thunder Client

`mvn test` executa testes de integração com MockMvc: endpoints, fluxo de cadastro e consulta, {'histórico e consumo' if port == 8080 else 'alteração de livro'}, entrada inválida e ID inexistente.

Em `tests/requisicoes.http` estão as requisições na ordem de execução. No Thunder Client, crie uma requisição por bloco, copie método e URL e selecione Body > JSON nos POST/PUT. Compare o HTTP esperado e capture as respostas para entrega. Os testes automáticos não substituem as capturas solicitadas pelo professor.

## 5. Erros comuns e soluções

| Erro | Solução |
| --- | --- |
| Porta ocupada | Encerre a outra aplicação ou use `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8090`. |
| 400 | Confira os campos e tipos do JSON; horário deve ser `08:30`. |
| 404 | Cadastre primeiro e use o ID retornado. |
| 405 | Confira o método HTTP na tabela. |
| Dados desapareceram | O armazenamento é em memória; recadastre após reiniciar. |
| Maven falha ao baixar dependências | Verifique conexão e configuração do Maven. |

## 6. GitHub, CI e execução em contêiner

O workflow `.github/workflows/pipeline.yml` executa `mvn verify` em push e pull request. Para ativá-lo, publique o conteúdo desta pasta como raiz de um repositório próprio. O workflow valida o projeto; a publicação web precisa ser configurada em um provedor.

```powershell
git init
git add .
git commit -m "Implementa aplicação CloudTech da aula 11"
git branch -M main
# Depois de criar um repositório vazio no GitHub, substitua a URL:
git remote add origin https://github.com/SEU_USUARIO/SEU_REPOSITORIO.git
git push -u origin main
docker build -t cloudtech-{artifact} .
docker run --rm -p {port}:{port} cloudtech-{artifact}
```

Sugestões de branches para novas alterações: `feature/cadastro`, `feature/testes`, `feature/docker`, `feature/pipeline-cicd`, `feature/monitoramento`. Faça commits reais durante a evolução do trabalho.

## 7. Checklist de entrega

- [ ] Executar `mvn clean verify`.
- [ ] Testar cada endpoint no Thunder Client e guardar capturas.
- [ ] Publicar o repositório com histórico de commits.
- [ ] Conferir a execução do GitHub Actions.
- [ ] Publicar a aplicação em ambiente web quando exigido.
- [ ] Informar links reais e evidências no relatório.
''')
    write(base, 'RELATORIO.md', '''# Relatório de entrega — CloudTech Solutions

Preencha após executar e publicar. Nenhum link ou evidência de publicação foi inventado.

- Nome:
- RA:
- Link do repositório GitHub:
- Link do histórico de commits:
- URL pública da aplicação:
- Link da execução do pipeline:

## Validação

Execute `mvn clean verify` e registre data e resultado. Teste as requisições de `tests/requisicoes.http` no Thunder Client e anexe capturas de cada endpoint com método, URL, status HTTP e resposta. Inclua exemplos de erro 400 e 404.

## Limitação

Armazenamento em memória, apagado ao reiniciar. Sem autenticação. Publicação web e repositório remoto ainda precisam ser configurados.
''')

write('.', 'README.md', '''# Aula 11 — CloudTech Solutions

Dois projetos Spring Boot independentes:

1. [Exercício 1: monitoramento de máquinas](exercicio1-monitoramento-maquinas/README.md), porta 8080.
2. [Exercício 2: biblioteca](exercicio2-biblioteca/README.md), porta 8081.

Abra um terminal na pasta do exercício desejado e execute `mvn clean verify` e depois `mvn spring-boot:run`.
Cada projeto tem seus testes, exemplos JSON, requisições HTTP, Dockerfile e pipeline próprios.
Os dados ficam em memória e são apagados ao reiniciar. Consulte o README de cada exercício para os comandos e a lista de entrega.
''')

