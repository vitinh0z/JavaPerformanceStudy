
# Benchmark

> Imagina que no seu código tem um trecho que roda **1 milhão de vezes por dia**.

Você pensa:  
“Como posso transformar esse *hotspot* em algo muito mais eficiente e rápido?”  
“Será que uso **Regex** ou uma **validação manual**?”

Com **Benchmark**, você consegue medir **de verdade** qual é mais rápido.

---

## Exemplo de Código

```java
package benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
public class TestBenchmark {

    String cpfExemplo = "123.456.789-00";

    @Benchmark
    public boolean testarComRegex() {
        return cpfExemplo.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");
    }

    @Benchmark
    public boolean testarNaMao() {
        for (int i = 0; i < 14; i++) {

            if (i == 3 || i == 7 || i == 11) continue;

            char c = cpfExemplo.charAt(i);

            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception {

        Options opt = new OptionsBuilder()
                .include(TestBenchmark.class.getSimpleName())
                .forks(1) // Roda apenas um processo
                .warmupIterations(3) // Aquece o "motor" 3 vezes
                .measurementIterations(5) // Mede valendo 5 vezes
                .build();

        new Runner(opt).run();
    }
}
````

---

## Certo, mas o que tudo isso significa?

---

### Explicação do Código

```java
@State(Scope.Benchmark)
```

> Aqui estamos a dizer que essa classe será responsável por rodar os testes de benchmark.
> Dá pra comparar com anotações do Spring Boot como `@Service`, `@Configuration`
> ou `@Repository`: estamos a definir o papel da classe.

---

```java
@Benchmark
public boolean testarComRegex() {
    return cpfExemplo.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");
}
```

> A anotação `@Benchmark` marca quais métodos o JMH vai executar nos testes.
> Aqui estamos a validar o CPF usando **Regex**.
> Como é um exemplo simples, não tratei todos os casos possíveis.

---

```java
@Benchmark
public boolean testarNaMao() {
    for (int i = 0; i < 14; i++) {

        if (i == 3 || i == 7 || i == 11) continue;

        char c = cpfExemplo.charAt(i);

        if (c < '0' || c > '9') return false;
    }
    return true;
}
```

> Aqui validamos o CPF “na mão”, usando `for`, `if` e comparação de caracteres.
> Ou seja, uma abordagem mais direta e mais “nativa”, sem o custo do motor de regex.

---

```java
Options opt = new OptionsBuilder()
        .include(TestBenchmark.class.getSimpleName())
        .forks(1)
        .warmupIterations(3)
        .measurementIterations(5)
        .build();

new Runner(opt).run();
```

> `.include(TestBenchmark.class.getSimpleName())`
> Diz qual benchmark será executado. Assim o JMH não roda outros testes do projeto.

> `.forks(1)`
> Define quantas JVMs serão criadas para o teste.
> Usei apenas 1 porque não quero explodir meu notebookzinho.

> `.warmupIterations(3)`
> Por incrível que pareça, o Java precisa “aquecer os motores”.
> Nessas execuções o JIT otimiza o código, mas os resultados não contam.

> `.measurementIterations(5)`
> Aqui, sim, são as medições reais. O benchmark roda 5 vezes e coleta os dados.

> `.build()`
> Fecha as configurações.

> `new Runner(opt).run()`
> Diz para o JMH que está tudo pronto e pode rodar.

---

## Resultados

| Benchmark                    | Mode  | Cnt | Score (ops/s)  | Error (±)   | Units |
| ---------------------------- | ----- | --- | -------------- | ----------- | ----- |
| TestBenchmark.testarComRegex | thrpt | 5   | 1.618.592,089  | 52.779,265  | ops/s |
| TestBenchmark.testarNaMao    | thrpt | 5   | 22.001.643,434 | 309.973,277 | ops/s |

---

### Resumindo a tabela

| Benchmark      | Throughput (ops/s) |
| -------------- | ------------------ |
| testarComRegex | ~1,6 milhões       |
| testarNaMao    | ~22 milhões        |

> O benchmark mostrou que o método que valida o CPF na mão
> é muito mais rápido que o Regex.
> Em 1 segundo, ele consegue rodar cerca de **22 milhões de vezes**.

---

## Quando usar Benchmark no meu código?

Pode parecer algo muito foda e você pensar:
“Uau, vou testar tudo no meu projeto!”

Mas sendo sincero: **não é para tudo**.

Benchmark faz sentido em partes do código que:

* rodam muitas vezes
* são gargalos reais
* impactam desempenho de verdade

Você não vai criar um benchmark pra saber a velocidade do login do usuário.
Mas pode (e deve) usar quando o código vira um *hotspot*.


