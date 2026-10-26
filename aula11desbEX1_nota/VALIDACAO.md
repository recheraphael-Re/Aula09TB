# Validação local — 26/10/2026

Ambiente: Java 21.0.11, Maven e Spring Boot 3.4.2.

| Projeto | Comando | Resultado |
| --- | --- | --- |
| Exercício 1 — monitoramento | `mvn -B verify` | Compilação e empacotamento concluídos; 2 testes, zero falhas e zero erros. |

Os testes de integração executam múltiplas requisições com MockMvc para verificar os endpoints, os fluxos completos e os erros 400/404. Os relatórios detalhados ficam em `target/surefire-reports` de cada projeto após a execução.

Não foram realizados: testes manuais no Thunder Client, build Docker, execução do GitHub Actions, criação de repositório remoto e publicação web. As instruções e os arquivos necessários estão nos projetos; esses itens permanecem no checklist de entrega.
