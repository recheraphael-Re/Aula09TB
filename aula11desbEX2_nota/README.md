# Exercício 2 — Biblioteca

Aplicação Java 21 e Spring Boot da CloudTech Solutions. Interface JSON conforme o enunciado da Aula 11.

## 1. Executar

Requisitos: JDK 21 e Maven 3.9 (comandos executados nesta pasta).

```powershell
mvn clean verify
mvn spring-boot:run
```

Acesse http://localhost:8081. Use Ctrl+C para encerrar. Os cadastros e o histórico ficam em memória: ao reiniciar, os dados são apagados. IDs são gerados pelo servidor. Não há autenticação; esta é uma aplicação didática.

## 2. Endpoints

| Método | Caminho | HTTP esperado |
| --- | --- | --- |
| GET | `/` | 200 |
| GET | `/livros` | 200 |
| POST | `/livros/cadastrar` | 201 |
| PUT | `/livros/alterar/1` | 200 |
| GET | `/livros/1` | 200 |
| GET | `/documentacao` | 200 |

Cadastro e alteração recebem JSON. Campos obrigatórios vazios, números negativos de produção e horários inválidos retornam 400. IDs inexistentes retornam 404. Alteração de livro usa PUT com todos os campos. A listagem de livros inclui todos os status para gerenciamento do acervo.

## 3. Reproduzir no terminal

Com a aplicação em execução, abra outro terminal nesta pasta:

```powershell
curl.exe -i -X POST http://localhost:8081/livros/cadastrar -H "Content-Type: application/json" --data-binary "@tests/livro.json"
curl.exe -i -X PUT http://localhost:8081/livros/alterar/1 -H "Content-Type: application/json" --data-binary "@tests/livro-alterado.json"
curl.exe http://localhost:8081/livros/1
```

Em uma execução nova, o primeiro cadastro recebe `id: 1`. Cadastro retorna HTTP 201 e os campos enviados junto com o ID. Consulta retorna HTTP 200. Após o PUT, a consulta retorna ano 2024 e status Emprestado, mantendo o ID.

## 4. Testes e Thunder Client

`mvn test` executa testes de integração com MockMvc: endpoints, fluxo de cadastro e consulta, alteração de livro, entrada inválida e ID inexistente.

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
docker build -t cloudtech-biblioteca .
docker run --rm -p 8081:8081 cloudtech-biblioteca
```

Sugestões de branches para novas alterações: `feature/cadastro`, `feature/testes`, `feature/docker`, `feature/pipeline-cicd`, `feature/monitoramento`. Faça commits reais durante a evolução do trabalho.

## 7. Checklist de entrega

- [ ] Executar `mvn clean verify`.
- [ ] Testar cada endpoint no Thunder Client e guardar capturas.
- [ ] Publicar o repositório com histórico de commits.
- [ ] Conferir a execução do GitHub Actions.
- [ ] Publicar a aplicação em ambiente web quando exigido.
- [ ] Informar links reais e evidências no relatório.
