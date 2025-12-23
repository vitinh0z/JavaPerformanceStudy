# Macrobenchmark

Vimos que podemos medir o desempenho de um método isolado usando o **JMH** (Microbenchmark).

Mas e se quisermos medir a saúde do software completo, com banco de dados, rede e servidor web? Para isso, olhamos para o **RPS**.

---

## O que é RPS?

RPS significa **Requests Per Second** (Requisições Por Segundo).

Ele mede a **Vazão (Throughput)** do sistema. Ou seja: "Quantos pedidos o seu sistema consegue aguentar e entregar ao mesmo tempo?"

> **Analogia:** Pense numa estrada.
> * **Latência (Tempo):** É a velocidade do carro (km/h).
> * **RPS (Vazão):** É quantas faixas a estrada tem e quantos carros passam por ali em 1 segundo.
>
> 

---

## Como Descobrir o RPS?

Existem duas formas principais:

### 1) Monitoramento (Vida Real)

Observar o sistema rodando em produção.

* **Ferramentas:** Datadog, New Relic, Prometheus + Grafana.
* **Modo Raiz:** `grep` nos arquivos de log contando linhas por segundo.

### 2) Teste de Carga (Simulação)

Usar ferramentas para simular milhares de usuários acessando ao mesmo tempo.

* **Ferramentas:** K6 (Moderno/JS), JMeter (Clássico/Java), Gatling.
* **Como funciona:** Você configura o "ataque" e vê até onde o sistema aguenta antes de começar a dar erro ou ficar lento demais.
* *Exemplo:* Se em 1 segundo você mandou 500 requisições e 480 voltaram com sucesso ("HTTP 200 OK"), o seu RPS efetivo é 480.



---

## O Conceito de Gargalo (Bottleneck)

O seu sistema funciona como uma **corrente**. A capacidade final (RPS) será determinada pelo parte mais fraca.

Imagine o fluxo:

1. **Código Java:** Aguenta 1.000 RPS.
2. **Banco de Dados:** Aguenta apenas 100 RPS.

**Resultado Final:** Seu sistema só entrega **100 RPS**.

> **Regra de importante:** Não adianta otimizar o código se o gargalo é a infraestrutura (Banco ou a Rede).
> Antes de tunar threads ou algoritmos, descubra quem está atrasando mais.

---

## Dica Prática

A maioria dos bancos de dados (MySQL, Postgres) possui comandos internos para mostrar o RPS atual (`QPS - Queries Per Second`).

Se o RPS do seu Java estiver alto, mas o do Banco estiver no limite, **a culpa é do Banco**, não do seu código.