# Exercício 1 - CalcIMC

API Spring Boot com Maven, Java 21 e JUnit 5 para calcular o IMC:

```text
IMC = peso / (altura * altura)
```

A classe `src/main/java/com/CalcIMC/CalcIMC.java` segue a estrutura padrão
do Maven e recebe o peso em quilogramas e a altura em metros.

Exemplo de uso:

```java
CalcIMC calculadora = new CalcIMC();
double imc = calculadora.calcularIMC(80.0, 2.0); // 20.0
```

Execute na pasta `aula10devops_IMC`:

```powershell
mvn clean test
```

Resultado esperado: 12 testes executados, nenhuma falha e `BUILD SUCCESS`.

Para gerar o JAR:

```powershell
mvn package
```

O workflow fica em `.github/workflows/aula10-imc-testes.yml` neste projeto.
Ele executa `mvn -B clean verify` em pushes e pull requests e permite
execução manual. O JAR gerado é disponibilizado como artefato da build.

## Testar com Thunder Client

1. Na raiz deste projeto, inicie o servidor no terminal do VS Code:

   ```powershell
   mvn spring-boot:run
   ```

2. Aguarde a mensagem `Started ImcApplication`. Mantenha o terminal aberto.
3. Abra o Thunder Client no VS Code e clique em **New Request**.
4. Selecione **GET** e informe a URL:

   ```text
   http://localhost:8080/imc?peso=70&altura=1.75
   ```

5. Clique em **Send**. Não precisa preencher Body nem autenticação.

Resposta esperada: **200 OK**, com JSON:

```json
{
  "peso": 70.0,
  "altura": 1.75,
  "imc": 22.857142857142858
}
```

Use peso em quilogramas e altura em metros, com ponto nos decimais.
Para testar outro cálculo, envie `http://localhost:8080/imc?peso=80&altura=2`:
o campo `imc` deve ser `20.0`.

Entradas ausentes, texto em lugar de números, valores não finitos, peso ou
altura menores ou iguais a zero e cálculos fora do intervalo numérico
suportado retornam **400 Bad Request**. Exemplo:
`http://localhost:8080/imc?peso=70&altura=0`.

Para encerrar o servidor, pressione **Ctrl+C** no terminal.
Se houver erro de conexão, confirme se a aplicação iniciou. Se a porta 8080
estiver ocupada, execute:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Nesse caso, use `localhost:8081` nas requisições.

Também é possível executar o JAR após `mvn clean verify`:

```powershell
java -jar target/calc-imc-1.0-SNAPSHOT.jar
```
