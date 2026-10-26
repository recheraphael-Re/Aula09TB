# Exercício 1 - CalcIMC

Projeto Maven em Java 21 com JUnit 5 para calcular o IMC:

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

Resultado esperado: 2 testes executados, nenhuma falha e `BUILD SUCCESS`.

Para gerar o JAR:

```powershell
mvn package
```

O workflow fica em `.github/workflows/aula10-imc-testes.yml` na raiz do
repositório Git (pasta acima deste projeto). Ele executa os testes em pushes
e pull requests que alterem este exercício, e permite execução manual.
