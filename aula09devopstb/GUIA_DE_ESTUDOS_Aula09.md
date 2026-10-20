---
title: Guia de estudos da Aula 09
subtitle: Java Maven JUnit e GitHub Actions
toc-title: Sumário
lang: pt-BR
---

Este guia acompanha o projeto `aula09devopstb` e explica como uma alteração no código Java pode ser verificada por testes locais e pelo GitHub Actions. Leia com `Calculadora.java`, `CalculadoraTest.java`, `pom.xml` e o workflow abertos no VS Code. Para visualizar este Markdown formatado, use **Ctrl+Shift+V**.

Nome: ____________________________________  RA: ____________________

# 1 Objetivos da aula

Ao terminar o estudo, você deve conseguir explicar o comportamento da calculadora, escrever um teste unitário, interpretar o resultado do Maven, fazer um commit e localizar uma execução automática no GitHub. A automação configurada é de **integração contínua**, ou CI: ela verifica o código ao receber alterações. O projeto ainda não possui uma etapa de implantação automática.

O caminho de uma alteração é:

```text
Editar Java -> executar testes locais -> revisar o diff
-> criar commit -> fazer push -> GitHub Actions executar testes
-> consultar o resultado e corrigir eventuais falhas
```

Um resultado verde confirma que os testes executados passaram. A qualidade dessa verificação depende dos cenários escritos: atualmente existe apenas um teste de soma.

# 2 Localização dos arquivos

## 2.1 As três pastas importantes

O repositório Git é a pasta `Desenvolvimento_backend_26`. A pasta aberta no VS Code é `Desenvolvimento_backend_26\aula09devopstb`. Dentro dela existe outra pasta chamada `aula09devopstb`, que contém o projeto Maven. Por isso os caminhos aparecem repetidos no workflow.

```text
Desenvolvimento_backend_26/              <- raiz do Git
  .github/workflows/aula09-testes.yml    <- workflow reconhecido pelo GitHub
  aula09devopstb/                       <- pasta aberta no VS Code
    GUIA_DE_ESTUDOS_Aula09.md           <- este guia
    .gitignore
    aula09devopstb/                     <- raiz do Maven
      pom.xml
      src/main/java/com/senai/Calculadora.java
      src/test/java/com/senai/CalculadoraTest.java
      target/                          <- arquivos gerados
```

O workflow deve ficar em `.github/workflows` **na raiz do repositório Git**. Uma pasta `.github` dentro da aula não substitui essa localização.

## 2.2 Arquivos para consultar durante o estudo

- [Calculadora.java](aula09devopstb/src/main/java/com/senai/Calculadora.java): implementação dos métodos.
- [CalculadoraTest.java](aula09devopstb/src/test/java/com/senai/CalculadoraTest.java): teste atual.
- [pom.xml](aula09devopstb/pom.xml): configuração do Maven.
- [Workflow de testes](../.github/workflows/aula09-testes.yml): automação publicada.
- [.gitignore](.gitignore): exclusão de arquivos gerados.

`src/main/java` guarda o código da aplicação. `src/test/java` guarda o código usado para verificá-la. `target` é recriada pelo Maven e não deve ser editada como fonte.

# 3 Entendendo a classe Calculadora

## 3.1 Código atual

```java
package com.senai;

public class Calculadora {
    public int somar(int a, int b) {
        return a + b;
    }

    public int multiplicar(int num1, int num2) {
        return num1 * num2;
    }
}
```

`package com.senai` identifica o pacote da classe. Na estrutura Maven utilizada, o arquivo fica abaixo de `com/senai`. `public class Calculadora` declara uma classe acessível para outras classes. O nome do arquivo é `Calculadora.java`.

Em `public int somar(int a, int b)`, `public` define a visibilidade, `int` antes de `somar` indica o tipo retornado e `a` e `b` são os parâmetros recebidos. `return a + b` calcula a soma e devolve o valor ao código que chamou o método.

O método `multiplicar` segue a mesma estrutura, usando `*`. Os nomes dos parâmetros podem mudar sem alterar a operação, desde que todas as referências sejam ajustadas.

## 3.2 Exemplos de comportamento

| Chamada | Retorno esperado | Motivo |
| --- | --- | --- |
| `somar(3, 2)` | `5` | Soma de dois positivos |
| `somar(0, 7)` | `7` | Zero é elemento neutro da soma |
| `somar(-3, 2)` | `-1` | Soma com número negativo |
| `multiplicar(3, 2)` | `6` | Produto de dois positivos |
| `multiplicar(0, 9)` | `0` | Produto por zero |
| `multiplicar(-3, 2)` | `-6` | Produto com sinais diferentes |

Esses métodos retornam valores; não usam `System.out.println`. Também não existe método `main`. Assim, não espere uma mensagem de calculadora no terminal ao rodar os testes, nem tente executar essa classe com `java com.senai.Calculadora` como se fosse um programa com entrada própria.

`int` possui faixa limitada. Operações que ultrapassam essa faixa podem produzir overflow sem lançar uma exceção aritmética. Os pequenos valores desta aula estão dentro da faixa; validar limites seria outro conjunto de cenários.

# 4 Entendendo o teste atual

## 4.1 Código essencial

```java
package com.senai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class CalculadoraTest {
    @Test
    public void testeSomar() {
        Calculadora calculadora = new Calculadora();
        int resultado = calculadora.somar(3, 2);
        assertEquals(5, resultado);
    }
}
```

O import de `Test` permite usar `@Test`, que marca o método para execução pelo JUnit. O import estático de `assertEquals` permite chamar a asserção diretamente, sem escrever `Assertions.assertEquals`.

`Calculadora calculadora = new Calculadora()` cria uma instância da classe. A variável `calculadora` guarda a referência ao objeto. Em seguida, o teste chama `somar(3, 2)` e guarda o retorno em `resultado`.

`assertEquals(5, resultado)` compara o valor **esperado**, no primeiro argumento, com o **obtido**, no segundo. Se forem iguais, o teste passa. Se forem diferentes, o JUnit registra uma falha. O método do teste retorna `void`, pois comunica o resultado por asserções e possíveis exceções.

## 4.2 Organizando o raciocínio do teste

O padrão Arrange, Act, Assert ajuda a separar três momentos:

1. **Preparar:** criar a calculadora e escolher as entradas.
2. **Executar:** chamar o método que está sendo estudado.
3. **Verificar:** comparar o resultado com uma resposta conhecida.

O esperado deve vir do requisito ou do cálculo que você conhece. Evite calcular o esperado chamando novamente o próprio método: isso pode repetir o mesmo defeito e deixar o teste verde.

## 4.3 O que está e o que ainda não está testado

O teste atual confirma somente `somar(3, 2) == 5`. Ele não verifica `multiplicar`, soma com negativos, zero ou limites de `int`. Se `multiplicar` estiver errado, o teste atual ainda pode passar.

O nome `CalculadoraTest` segue uma convenção reconhecida pelo Surefire. A anotação `@Test` identifica o método de teste. Um nome descritivo como `deveSomarDoisNumerosPositivos` pode deixar o objetivo mais claro.

# 5 Entendendo o pom xml

## 5.1 Identificação e propriedades

O `pom.xml` é o arquivo que o Maven lê para construir o projeto. A configuração atual contém:

| Elemento | Valor atual | Função no projeto |
| --- | --- | --- |
| `groupId` | `com.senai` | Identifica o grupo do artefato |
| `artifactId` | `aula09devopstb` | Identifica o projeto |
| `version` | `1.0-SNAPSHOT` | Indica uma versão em desenvolvimento |
| `project.build.sourceEncoding` | `UTF-8` | Define a codificação de fontes para o build |
| `maven.compiler.source` | `21` | Define o nível da linguagem para compilação |
| `maven.compiler.target` | `21` | Define a versão do bytecode gerado |

O JDK usado para compilar deve suportar Java 21. `JAVA_HOME` e o `PATH` precisam apontar para uma instalação adequada. Use `mvn -version` para descobrir qual Java o Maven realmente está usando.

## 5.2 Dependência do JUnit

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.11.4</version>
    <scope>test</scope>
</dependency>
```

Essa é a biblioteca utilizada pelos imports dos testes. O escopo `test` disponibiliza a dependência para compilação e execução dos testes, sem torná-la uma dependência normal da aplicação.

Por isso compilar o teste com `javac` sozinho, sem configurar o classpath do JUnit, costuma gerar erros de pacote ausente. Nesta aula, use Maven para preparar as dependências e executar os testes.

## 5.3 Plugins configurados

O `maven-compiler-plugin` está na versão `3.13.0` e compila os arquivos Java. O `maven-surefire-plugin` está na versão `3.5.4` e executa os testes unitários durante a fase `test`.

Dependências fornecem bibliotecas para o código. Plugins realizam tarefas de construção. Aqui, JUnit define e executa a lógica dos testes em conjunto com o mecanismo de testes, e Surefire conecta a execução ao Maven.

# 6 Comandos para reproduzir os resultados

## 6.1 Verificar o ambiente

No terminal PowerShell do VS Code:

```powershell
Get-Location
java -version
javac -version
mvn -version
```

Confira Java 21 e a presença do Maven. A versão de manutenção e o fornecedor do JDK podem variar. O Java indicado pelo Maven é a informação mais relevante para o build.

## 6.2 Rodar a partir da pasta aberta no VS Code

Na pasta `Desenvolvimento_backend_26\aula09devopstb`, execute:

```powershell
mvn --batch-mode --no-transfer-progress -f aula09devopstb/pom.xml clean test
```

`-f` aponta para o POM que está na subpasta. `clean` remove os resultados anteriores. `test` compila o necessário e executa os testes. `--batch-mode` evita interação e `--no-transfer-progress` reduz mensagens de progresso de downloads.

Na primeira execução podem ocorrer downloads. O tempo total varia com a rede e com o cache local.

## 6.3 Rodar dentro da pasta do Maven

```powershell
Set-Location aula09devopstb
mvn clean test
```

Depois de entrar nessa pasta, o `pom.xml` fica no diretório atual. Para voltar à pasta do guia:

```powershell
Set-Location ..
```

Não execute esse `Set-Location aula09devopstb` repetidamente sem conferir onde está. O comando `Get-Location` ajuda a evitar confusão.

## 6.4 Executar somente o teste escolhido

Da pasta aberta no VS Code:

```powershell
mvn -f aula09devopstb/pom.xml "-Dtest=CalculadoraTest" test
mvn -f aula09devopstb/pom.xml "-Dtest=CalculadoraTest#testeSomar" test
```

O primeiro seleciona a classe. O segundo seleciona o método atual. Se o método for renomeado, ajuste o comando.

## 6.5 Saída esperada

Com o código atual, os trechos relevantes são:

```text
[INFO] Running com.senai.CalculadoraTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

`Tests run` informa quantos testes foram executados. `Failures` conta falhas de verificação. `Errors` conta erros de execução, como exceções inesperadas. `Skipped` conta testes ignorados. O tempo e as demais mensagens do log podem variar.

Os relatórios ficam em `aula09devopstb/target/surefire-reports`, considerando a pasta do guia como ponto de partida:

```powershell
Get-Content aula09devopstb/target/surefire-reports/com.senai.CalculadoraTest.txt
```

## 6.6 Fases do Maven

No ciclo padrão, pedir uma fase posterior também executa as fases anteriores necessárias. `test` inclui a compilação; `package` inclui os testes e o empacotamento. `clean` pertence a outro ciclo e pode ser combinado no mesmo comando. Consulte a [documentação do ciclo de vida do Maven](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html).

```powershell
mvn -f aula09devopstb/pom.xml compile
mvn -f aula09devopstb/pom.xml test
mvn -f aula09devopstb/pom.xml package
```

Use esses comandos como alternativas conforme o objetivo. O workflow atual usa `clean test` e não produz um JAR como etapa de entrega. Mesmo ao gerar um JAR com `package`, o projeto não passa a ter automaticamente um `main` executável.

# 7 Experimentos para aprender com os testes

## 7.1 Observar uma falha de asserção

Experimento local: altere temporariamente `assertEquals(5, resultado)` para `assertEquals(6, resultado)`. Execute o comando da seção 6.2. O método continua retornando 5, mas o teste passa a exigir 6.

Você deverá encontrar uma mensagem equivalente a:

```text
expected: <6> but was: <5>
Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
BUILD FAILURE
```

Restaure o esperado para 5 e rode novamente. O objetivo é aprender a identificar uma falha e sua causa; não envie essa alteração experimental para `master`.

## 7.2 Observar uma regressão no código

Outro experimento é trocar temporariamente `return a + b` por `return a - b`. Para as entradas 3 e 2, o retorno será 1. O teste correto deve falhar com esperado 5 e obtido 1. Restaure a soma e confirme `BUILD SUCCESS`.

Esse exercício demonstra por que um teste protege um comportamento durante alterações futuras.

## 7.3 Escrever novos cenários

Os exemplos abaixo são exercícios para adicionar **dentro da classe `CalculadoraTest`**, antes da última chave. Eles não fazem parte do teste atual; use os imports já existentes.

```java
@Test
public void testeMultiplicar() {
    Calculadora calculadora = new Calculadora();
    int resultado = calculadora.multiplicar(3, 2);
    assertEquals(6, resultado);
}

@Test
public void testeSomarComZero() {
    Calculadora calculadora = new Calculadora();
    assertEquals(7, calculadora.somar(0, 7));
}

@Test
public void testeSomarComNegativo() {
    Calculadora calculadora = new Calculadora();
    assertEquals(-1, calculadora.somar(-3, 2));
}
```

Se acrescentar os três métodos mantendo o teste original, a saída esperada passa a ser `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0`. Antes de editar, explique para si mesmo qual erro cada cenário conseguiria detectar.

# 8 Entendendo o workflow publicado

## 8.1 Configuração atual

```yaml
name: Testes da Aula 09

on:
  push:
    paths:
      - 'aula09devopstb/**'
      - '.github/workflows/aula09-testes.yml'
  pull_request:
    paths:
      - 'aula09devopstb/**'
      - '.github/workflows/aula09-testes.yml'
  workflow_dispatch:

permissions:
  contents: read

jobs:
  testes:
    name: Java 21 e JUnit
    runs-on: ubuntu-latest
    timeout-minutes: 10
    steps:
      - name: Obter o codigo
        uses: actions/checkout@v6

      - name: Configurar Java 21
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
          cache-dependency-path: aula09devopstb/aula09devopstb/pom.xml

      - name: Executar os testes
        working-directory: aula09devopstb/aula09devopstb
        run: mvn --batch-mode --no-transfer-progress clean test
```

## 8.2 Eventos e filtros

`name` é o título mostrado na aba Actions. `on` define os eventos que iniciam a automação. `push` verifica envios de commits; `pull_request` verifica eventos de uma proposta de alteração; `workflow_dispatch` permite iniciar uma execução manual.

Os filtros `paths` selecionam alterações na pasta da aula ou no workflow. Uma alteração somente em outra aula não inicia esta verificação. Alterar este guia também corresponde ao filtro da aula. Não foi definido filtro de branches; o workflow não está restrito a `master`.

## 8.3 Job e ambiente

`jobs` agrupa as tarefas. Existe um job com identificador `testes`, mostrado como `Java 21 e JUnit`. `runs-on: ubuntu-latest` solicita uma máquina Linux hospedada pelo GitHub. `timeout-minutes: 10` limita a duração do job. `contents: read` concede permissão de leitura do conteúdo do repositório.

O ambiente é preparado na execução; ele não usa diretamente a instalação Java do seu Windows. Por isso a versão do Java precisa estar declarada.

## 8.4 Etapas executadas

`actions/checkout@v6` obtém o código do repositório. `actions/setup-java@v5` configura o JDK 21 da distribuição Temurin. O cache Maven reaproveita dependências e o caminho do POM participa da identificação desse cache. Cache não substitui a execução dos testes.

`working-directory` indica onde executar o comando. Os caminhos do workflow são relativos à raiz do checkout, por isso há duas ocorrências de `aula09devopstb`. O comando final limpa, compila e testa. Uma falha do Maven gera uma etapa com falha e faz este job falhar.

YAML depende de indentação: use espaços e mantenha a hierarquia. `uses` seleciona uma action reutilizável; `run` executa um comando de terminal. A [documentação de Java com Maven no GitHub Actions](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven) apresenta o mesmo modelo geral.

## 8.5 Consultar a execução

1. Abra o [repositório no GitHub](https://github.com/recheraphael-Re/Desenvolvimento_backend_26).
2. Clique em **Actions** e escolha **Testes da Aula 09**.
3. Abra a execução correspondente ao seu commit.
4. Abra o job **Java 21 e JUnit** e a etapa **Executar os testes**.
5. Confira a quantidade de testes e `BUILD SUCCESS` ou a mensagem de falha.

Para execução manual, use **Run workflow** na página desse workflow, selecione a branch e inicie. O último workflow verificado durante a configuração passou: [execução 37554417617](https://github.com/recheraphael-Re/Desenvolvimento_backend_26/actions/runs/37554417617). Esse é um registro histórico, não uma garantia para commits futuros.

## 8.6 Limites da configuração

Não há deploy, medição de cobertura ou publicação de relatórios como artefatos nesse YAML. Os relatórios locais ficam em `target`; no GitHub, o resultado pode ser consultado no log. O workflow também não configura por si só regras que impeçam um merge: exigir testes aprovados depende da configuração de proteção da branch.

# 9 Git e publicação de alterações

## 9.1 Conceitos essenciais

Git controla o histórico local. GitHub hospeda o repositório remoto e executa o Actions. `origin` é o nome dado ao remoto configurado. `master` é a branch usada neste projeto.

Uma edição modifica a cópia de trabalho. `git add` escolhe o conteúdo que entrará no próximo commit. `git commit` registra esse conjunto no histórico local. `git push` envia commits ao remoto. Um arquivo salvo no editor ainda não foi automaticamente publicado.

## 9.2 Publicar uma alteração no teste

Na pasta do guia, depois de editar e rodar os testes:

```powershell
git status --short
git diff -- aula09devopstb/src/test/java/com/senai/CalculadoraTest.java
git add -- aula09devopstb/src/test/java/com/senai/CalculadoraTest.java
git diff --cached
git commit -m "test: adiciona cenarios da calculadora"
git push origin master
```

Confira todos os arquivos mostrados em `git diff --cached` antes do commit. Como várias aulas compartilham o repositório, escolher o caminho explicitamente ajuda a publicar apenas o trabalho desejado.

## 9.3 Versionar este guia

```powershell
git add -- GUIA_DE_ESTUDOS_Aula09.md
git diff --cached
git commit -m "docs: adiciona guia de estudos da aula 09"
git push origin master
```

Esses comandos são um roteiro de consulta. A criação do guia não exige automaticamente executá-los. A cópia Word em Downloads fica fora desse repositório.

## 9.4 O papel do gitignore

O `.gitignore` da aula contém `**/target/`. Isso ignora diretórios `target` abaixo dessa pasta. Fontes, testes, POM, guia e workflow são materiais de desenvolvimento; classes compiladas e relatórios podem ser gerados novamente.

Adicionar um padrão ao `.gitignore` não retira sozinho arquivos já rastreados. Na configuração realizada, os arquivos gerados foram removidos do índice com `git rm --cached`, preservando os resultados locais. Esse ajuste já foi feito; não é necessário repetir para estudar.

# 10 Erros comuns e soluções

| Erro ou sintoma | Causa provável | Como resolver |
| --- | --- | --- |
| `mvn` não reconhecido | Maven fora do PATH | Confira a instalação e reabra o terminal |
| Não encontra `pom.xml` | Terminal na pasta errada | Use `Get-Location` e o comando com `-f` |
| `invalid target release: 21` | JDK antigo usado pelo Maven | Confira `mvn -version` e ajuste `JAVA_HOME` |
| Pacote JUnit não encontrado | Dependência ou classpath ausente | Use o Maven e confira o POM |
| `expected` diferente de `was` | Resultado ou expectativa incorreto | Recalcule o cenário e revise a implementação |
| Nenhum teste executado | Classe ou anotação não descoberta | Confira `src/test/java`, nome e `@Test` |
| Erro de compilação no teste | Sintaxe ou imports incorretos | Leia a linha indicada no log e corrija |
| Workflow não aparece | YAML fora da raiz do Git | Confira `.github/workflows` no repositório |
| Push não inicia os testes | Alteração fora do filtro de caminhos | Confira os arquivos alterados ou rode manualmente |
| Maven falha no Actions por caminho | Diretório relativo incorreto | Confira a pasta repetida no `working-directory` |
| Download de dependência falha | Rede ou repositório indisponível | Confira a mensagem e tente após corrigir a conexão |
| Push rejeitado | Remoto tem commits novos ou falta autenticação | Leia o erro, sincronize com cuidado e autentique |
| `target` aparece no Git | Arquivo já rastreado ou padrão ausente | Confira `.gitignore` e o índice do Git |
| Word mostra sumário vazio | Campo ainda não atualizado | Clique no sumário e escolha atualizar o índice |

Não confunda uma falha de compilação com uma asserção que falhou: na primeira, os testes podem nem ter começado. Procure a primeira mensagem relevante de erro e use o resumo final para confirmar o resultado.

# 11 Perguntas para revisar

1. Qual é a diferença entre `src/main/java` e `src/test/java`?
2. Qual valor aparece primeiro em `assertEquals`?
3. Por que o Maven precisa usar um JDK que suporte Java 21?
4. Qual é a diferença entre JUnit e Surefire neste projeto?
5. Por que o caminho do workflow contém duas pastas com o mesmo nome?
6. O teste atual detecta um defeito em `multiplicar`?
7. O que acontece quando você altera apenas outra aula?
8. Um teste verde garante que todos os cenários possíveis estão corretos?
9. Qual a diferença entre salvar, fazer commit e fazer push?
10. O que esta configuração automatiza e qual etapa de entrega ainda não existe?

Respostas para conferir: fontes e testes têm funções diferentes; esperado vem primeiro; o compilador precisa aceitar o nível 21; JUnit fornece os testes e Surefire integra a execução ao Maven; o projeto Maven está dentro de duas pastas da aula a partir da raiz do Git; multiplicação ainda não está testada; o filtro não inicia este workflow para outra aula; aprovação cobre apenas os cenários executados; salvar altera o arquivo, commit registra e push envia; há CI de testes, sem deploy.

# 12 Roteiro de estudo e checklist de entrega

## 12.1 Sequência sugerida

Leia a classe e calcule os exemplos da seção 3. Depois acompanhe cada linha do teste e execute `clean test`. Faça um dos experimentos de falha, restaure o código e confirme o sucesso. Em seguida, leia o POM e compare o comando local com o workflow. Por último, pratique a revisão do diff e consulte os logs do GitHub.

## 12.2 Checklist final

- [ ] Localizei a raiz do Git e a pasta do POM.
- [ ] Confirmei Java 21 em `mvn -version`.
- [ ] Sei explicar os métodos `somar` e `multiplicar`.
- [ ] Sei explicar `@Test`, criação do objeto e `assertEquals`.
- [ ] Executei `clean test` e conferi a quantidade de testes.
- [ ] Restaurei todas as alterações experimentais de falha.
- [ ] Sei diferenciar falha de asserção, erro e falha de compilação.
- [ ] Revisei os arquivos escolhidos para eventual commit.
- [ ] Confirmei que `target` não entra em novos commits.
- [ ] Localizei o workflow na raiz do repositório.
- [ ] Conferi uma execução aprovada no GitHub Actions.
- [ ] Guardei este Markdown no projeto para consulta.
- [ ] Preenchi nome e RA na cópia Word antes da entrega.
