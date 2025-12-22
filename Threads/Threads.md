# Threads

> "Quantas threads eu devo criar?"  
> "Quanto mais threads, melhor?"

A resposta curta é: **depende do que sua aplicação está fazendo**.

Vamos **ver na prática** como o número de threads impacta o desempenho.

---

## Sumário

- [A Ideia Principal](#a-ideia-principal)
- [Exemplo de Código](#exemplo-de-código)
- [Resultados Esperados](#resultados-esperados)
- [Por que isso acontece?](#por-que-isso-acontece)
- [Aplicando a fórmula na prática](#aplicando-a-fórmula-na-prática)
- [E as Virtual Threads?](#e-as-virtual-threads)
- [Quando NÃO Usar Threads](#quando-não-usar-threads)
- [Resumo Final](#resumo-final)
- [Dica Prática (Vida Real)](#dica-prática-vida-real)
- [Dicas Finais](#dicas-finais)

---

Vamos separar o problema em dois mundos bem diferentes:

- **IO-Bound** → tarefas que passam a maior parte do tempo esperando (rede, banco, disco) → ou seja, coisas leves
- **CPU-Bound** → tarefas que ficam fritando o processador sem parar → Criptografia, Processamento de Imagens e coisas pesadas

Misturar os dois sem entender o impacto costuma gerar códigos lentos.

---

## A Ideia Principal

> Threads não deixam o código mais rápido magicamente.  
> Elas só ajudam quando existe tempo ocioso para ser aproveitado.

Se não existe tempo ocioso, mais threads só atrapalham.

---

## Exemplo de Código

### 1. Tarefa Leve (IO-Bound)

Imagine algo como:
- chamada HTTP  
- consulta no banco  
- leitura de arquivo  
- qualquer coisa que "espera resposta"

```java
public class TarefaLeve implements Runnable {

    @Override
    public void run() {
        try {
            // Simula espera de rede ou banco
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

> Aqui a thread não está trabalhando de verdade.  
> Ela entra em espera e **libera a CPU**.

---

### 2. Tarefa Pesada (CPU-Bound)

Agora o oposto: faz cálculo gigante, sem descanso.

```java
public class TarefaPesada implements Runnable {

    @Override
    public void run() {
        double resultado = 0;

        // Muitos cálculos matemáticos
        for (int i = 0; i < 500_000; i++) {
            resultado += Math.sqrt(i * Math.PI);
        }
    }
}
```

> Aqui a thread abraça a CPU e não solta em nenhum momento.  
> Enquanto ela roda, o núcleo está 100% ocupado.

---

### 3. O Main (Testando na Prática)

Agora a parte importante: rodar os testes e ver o que acontece.

```java
public class TesteThreads {

    public static void main(String[] args) {
        int nucleos = Runtime.getRuntime().availableProcessors();
        System.out.println("=== SEU PC TEM " + nucleos + " NÚCLEOS (LÓGICOS) ===\n");

        // Teste 1: IO-Bound
        System.out.println(">>> TESTE 1: Tarefas Leves (IO-Bound) <<<");
        testar(new TarefaLeve(), nucleos, "Poucas Threads");
        testar(new TarefaLeve(), nucleos * 4, "Muitas Threads");

        System.out.println();

        // Teste 2: CPU-Bound
        System.out.println(">>> TESTE 2: Tarefas Pesadas (CPU-Bound) <<<");
        testar(new TarefaPesada(), nucleos, "Threads Ideais");
        testar(new TarefaPesada(), nucleos * 4, "Threads em Excesso");
    }

    private static void testar(Runnable tarefa, int qtdThreads, String label) {
        ExecutorService executor = Executors.newFixedThreadPool(qtdThreads);
        
        long inicio = System.currentTimeMillis();

        // Submete 100 tarefas
        for (int i = 0; i < 100; i++) {
            executor.submit(tarefa);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long tempo = System.currentTimeMillis() - inicio;
        System.out.println(label + " (" + qtdThreads + "): " + tempo + "ms");
    }
}
```

---

### Explicação do Código

```java
int nucleos = Runtime.getRuntime().availableProcessors();
```

> Pega quantos núcleos (lógicos) seu processador tem.  
> Esse número é importante pra entender CPU-Bound.

---

```java
testar(new TarefaLeve(), nucleos, "Poucas Threads");
testar(new TarefaLeve(), nucleos * 4, "Muitas Threads");
```

> Roda o mesmo tipo de tarefa com **poucas threads** e depois com **muitas threads**.  
> Assim dá pra comparar qual é mais rápido.

---

```java
ExecutorService executor = Executors.newFixedThreadPool(qtdThreads);
```

> Cria um "pool"(grupo) com um número fixo de threads.  
> Tipo: "vou ter 8 pessoas trabalhando ao mesmo tempo".

---

```java
for (int i = 0; i < 100; i++) {
    executor.submit(tarefa);
}
```

> Submete 100 tarefas para as threads executarem.  
> As threads vão pegando as tarefas conforme ficam livres.

---

```java
executor.shutdown();
executor.awaitTermination(1, TimeUnit.MINUTES);
```

> `shutdown()` → Diz pro executor não aceitar mais tarefas novas.  
> `awaitTermination()` → Espera todas as tarefas terminarem (ou até 1 minuto).

---

```java
long tempo = System.currentTimeMillis() - inicio;
System.out.println(label + " (" + qtdThreads + "): " + tempo + "ms");
```

> Calcula quanto tempo levou do início ao fim.  
> Imprime o resultado na tela.

---

## Resultados Esperados

Ao rodar os testes, algo parecido com isso deve aparecer:

```text
=== SEU PC TEM 8 NÚCLEOS (LÓGICOS) ===

>>> TESTE 1: Tarefas Leves (IO-Bound) <<<
Poucas Threads (8): 1800ms
Muitas Threads (32): 400ms   <-- MUITO MAIS RÁPIDO

>>> TESTE 2: Tarefas Pesadas (CPU-Bound) <<<
Threads Ideais (8): 95ms      <-- MELHOR CENÁRIO
Threads em Excesso (32): 110ms <-- MAIS LENTO
```

Os números variam por máquina, mas o padrão é basicamente sempre esse.

Dá pra visualizar assim:

```
IO-Bound (quanto menos threads, pior):
8 threads  → ████████████████████ (1800ms)
32 threads → ████ (400ms) ✓

CPU-Bound (quanto mais threads, pior):
8 threads  → ███ (95ms) ✓
32 threads → ████ (110ms)
```

---

## Por que isso acontece?

### Analogia das Bancadas (analogia que eu inventei)

Antes de tudo, quem é quem:

* **Core do processador** → **Bancada / Mesa**
* **Thread** → **Pessoa trabalhando**
* **Context Switching** → Trocar a pessoa de bancada no meio do trabalho

---

### CPU-Bound: Por que usar threads = núcleos

Tarefas pesadas:

* fazem muito cálculo
* não esperam nada
* usam a CPU o tempo todo

Imagine:

* Você tem **8 bancadas (8 cores)**
* Cada pessoa chega com **muita coisa pra fazer**
* Enquanto a pessoa está na bancada, ela não sai da mesa

Se você cria:

* **8 threads** → perfeito  
  Cada pessoa pega uma bancada e trabalha até terminar.

Se você cria:

* **20 threads para 8 bancadas**

O processador precisa:

* tirar uma pessoa da bancada
* salvar o estado
* colocar outra
* depois voltar pra anterior

Esse vai-e-volta constante é o **Context Switching**.

> Enquanto o core fica trocando de "mesa",  
> o trabalho real anda mais devagar.

**Conclusão:**  
Para tarefas pesadas, o ideal é:

```
threads = número de núcleos do processador
```

Uma thread por core.  
Uma pessoa por bancada.

---

### IO-Bound: Por que mais threads ajudam

Agora o cenário muda.

Tarefas IO-Bound:

* fazem pouco cálculo
* entram rapidamente em espera
* aguardam rede, banco ou disco

Ou seja:

* a pessoa usa a bancada por pouco tempo
* sai para esperar
* a bancada fica vazia

Com poucas threads:

* várias ficam esperando ao mesmo tempo
* as bancadas ficam ociosas
* a CPU fica parada

Com mais threads:

* enquanto umas esperam
* outras aproveitam a bancada livre
* o core quase nunca fica ocioso

> O core troca de "mesa" **quando alguém está esperando**,  
> não no meio de um cálculo pesado.

**Conclusão:**  
Para IO-Bound, faz sentido aumentar o número de threads.

---

> **Nota de Engenharia (o "cálculo mágico")**  
>  
> Existe uma fórmula clássica para estimar o número ideal de threads em cargas de trabalho IO-Bound:  
> (e disseram que em programação não precisa de matemática)
>  
> ```
> Threads = Nº de Núcleos × (1 + Tempo de Espera / Tempo de Processamento)
> ```
>  
> Ela não é uma lei da física, mas ajuda a decidir com base em **medição**, não em: "ah acho que é isso".

---

## Aplicando a fórmula na prática

### 1. Tarefa Leve (IO-Bound)

Na `TarefaLeve`, quase tudo é espera.

```java
public void run() {
    long inicio = System.currentTimeMillis();

    // --- ESPERA (IO) ---
    try {
        Thread.sleep(200); // Simula rede/banco
    } catch (InterruptedException e) {}

    long fimEspera = System.currentTimeMillis();

    // --- PROCESSAMENTO (CPU) ---
    // Quase nenhum, só coisas internas do Java

    long fimTotal = System.currentTimeMillis();

    long tempoEspera = fimEspera - inicio; // ~200ms
    long tempoCPU = fimTotal - fimEspera;  // ~1ms (aproximação)
}
```

Aplicando a fórmula:

```
Threads = Núcleos × (1 + 200 / 1)
Threads = Núcleos × 201
```

**Conclusão:**  
Podemos criar **muitas threads** sem medo, porque a maior parte do tempo elas estão esperando.

---

### 2. Tarefa Pesada (CPU-Bound)

Na `TarefaPesada`, não existe espera.

```java
public void run() {
    long inicio = System.currentTimeMillis();

    // --- ESPERA (IO) ---
    // Nenhuma

    long fimEspera = System.currentTimeMillis();

    // --- PROCESSAMENTO (CPU) ---
    double resultado = 0;
    for (int i = 0; i < 500_000; i++) {
        resultado += Math.sqrt(i * Math.PI);
    }

    long fimTotal = System.currentTimeMillis();

    long tempoEspera = fimEspera - inicio; // ~0ms
    long tempoCPU = fimTotal - fimEspera;  // ~50ms
}
```

Aplicando a fórmula:

```
Threads = Núcleos × (1 + 0 / 50)
Threads = Núcleos × 1
```

**Conclusão:**  
O número ideal de threads é **igual ao número de núcleos**.

---

## E as Virtual Threads?

Se você tá no Java 21+, tem uma carta na manga: **Virtual Threads**.

A ideia é simples:

* são threads super leves
* a JVM gerencia milhões delas
* você não precisa se preocupar com pools (grupos)

Elas foram feitas especialmente para IO-Bound (tarefas leves)

```java
// Antes (Platform Threads)
ExecutorService executor = Executors.newFixedThreadPool(200);

// Agora (Virtual Threads)
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
```

Com Virtual Threads, você pode criar **milhares** delas sem medo.  
Quando uma espera (IO), a JVM automaticamente aproveita o core pra outra.

> Vale alertar: para CPU-Bound, Virtual Threads **não ajudam**.  
> Se a tarefa está calculando, ela precisa de um core de verdade.

---

## Quando NÃO Usar Threads

Nem tudo precisa de concorrência.

Evite threads quando:

* a tarefa é simples e rápida
* você não tem IO nem múltiplos cores
* a complexidade não vale a pena

**Vantagens de código single-threaded:**

* mais simples de entender
* menos bugs bizarros
* mais fácil de debugar
* não precisa sincronizar nada

> Se não tá lento, não tem motivo pra otimização.

---

## Resumo Final

| Tipo de Tarefa | Exemplos                     | Estratégia de Threads            |
| -------------- | ---------------------------- | -------------------------------- |
| **IO-Bound**   | API, Banco, Arquivo, Rede    | Muitas Threads / Virtual Threads |
| **CPU-Bound**  | Cálculo, Criptografia, Vídeo | Threads = nº de núcleos          |

---

## Dica Prática (Vida Real)

No Spring Boot, o Tomcat vem com ~200 threads por padrão.

Isso é ótimo para:

* requisições HTTP
* IO
* aplicações web tradicionais

Mas se você colocar **processamento pesado** dentro de um endpoint:

* uma thread fica presa
* outras requisições começam a travar
* o servidor "morre" aos poucos

Para tarefas pesadas:

* jogue para fila (Kafka, RabbitMQ, SQS)
* processe com workers dedicados
* limite bem o número de threads (igual explicação la em cima)

Threads são ferramentas.  
Usadas sem enteder, só acaba com performance do seu codigo

---

## Dicas finais

* Threads não fazem mágica
* Entenda o que sua tarefa faz (fica parada ou trabalha muito?)
* Faça cálculo antes de otimizar
* Simples funciona na maioria dos casos

E: código que funciona > código "otimizado" que ninguém entende e consiga das manutenção. Segredo é o meio termo.
