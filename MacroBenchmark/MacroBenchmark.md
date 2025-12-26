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

> **Por que não contam requisições que falharam?**  
> Se em 1 segundo você mandou 500 requisições e 480 voltaram com sucesso (HTTP 200), o RPS efetivo é 480.  
> As 20 que deram erro (timeout, 500, etc) não entram na conta porque RPS mede requisições **bem-sucedidas**.

---

## O Conceito de Gargalo (Bottleneck)

O seu sistema funciona como uma **corrente**. A capacidade final (RPS) será determinada pela parte mais fraca.

Imagine o fluxo:

1. **Código Java:** Aguenta 1.000 RPS.
2. **Banco de Dados:** Aguenta apenas 100 RPS.

**Resultado:** O seu sistema só entrega **100 RPS**. independente se o Java tá extremamente otimizado

> **Regra importante:** Não adianta otimizar o código se o gargalo é a infraestrutura (Banco ou Rede).  
> Antes de tunar threads ou algoritmos, descubra quem está atrasando mais.

---

## Vamos Testar

Para esse teste, fiz uma aplicação Spring Boot e criei 3 endpoints:

```java
@GetMapping("/rapida")
public String endpointRapido(){
    return "Resposta Rapida";
}
```

> Ela apenas retorna 'Resposta Rapida'. Algo extremamente simples.

---

```java
@GetMapping("/lenta")
public String endpointLento() {
    try {
        Thread.sleep(200);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    return "Resposta lenta";
}
```

> Aqui a nossa ideia é simular como se fosse algum gargalo (tipo consulta no banco que demora).

---

```java
@GetMapping("/pesada")
public String endpointPesado(){
    double resultado = 0;

    for (int i = 0; i < 1_000_000; i++){
        resultado += Math.sqrt(i * Math.PI);
    }
    return "Processamento concluido " + resultado;
}
```

> Aqui vamos simular algo pesado de processar.  
> Sei lá, tipo baixar Shrek 3 em HD.

---

## Criando o Script de Teste

Como o nosso foco é Java e entender Performance, não vou entrar muito detalhadamente em como baixar o K6. Mas é bem simples (MUITO simples).

Vamos criar o arquivo `teste-basico.js`:

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
    },
};

export default function() {
    let res1 = http.get('http://localhost:8080/api/rapida');
    check(res1, {
        'rapido status 200': (r) => r.status === 200,
        'rapido tempo < 200ms': (r) => r.timings.duration < 200,
    });

    sleep(1);

    let res2 = http.get('http://localhost:8080/api/lenta');
    check(res2, {
        'lento status 200': (r) => r.status === 200,
        'lento tempo < 1000ms': (r) => r.timings.duration < 1000,
    });

    sleep(1);

    let res3 = http.get('http://localhost:8080/api/pesada');
    check(res3, {
        'pesado status 200': (r) => r.status === 200,
        'pesado tempo < 1000ms': (r) => r.timings.duration < 1000,
    });

    sleep(1);
}
```

---

### Explicando o Script

```javascript
stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 100 },
    { duration: '1m', target: 100 },
    { duration: '30s', target: 0 },
],
```

> **Stages (Estágios):** Define como o número de usuários vai aumentar ao longo do tempo.
>
> - **30s → 10 users:** Começa suave com 10 usuários
> - **1min → 50 users:** Aumenta pra 50
> - **30s → 100 users:** Sobe pro máximo (100)
> - **1min → 100 users:** Mantém 100 usuários batendo no sistema
> - **30s → 0 users:** Diminui até parar
>
> Tipo quando você vai numa academia: aquece, treina pesado, e depois desacelera.

---

```javascript
thresholds: {
    http_req_duration: ['p(95)<500']
    http_req_failed: ['rate<0.01']
}
```

> **Thresholds (Limites):** São as "regras de aprovação" do teste.
>
> - `p(95)<500`: 95% das requisições precisam ser mais rápidas que 500ms
> 
> 
> - `rate<0.01`: Menos de 1% de erro é aceitável
>
> Se qualquer uma dessas regras falhar, o teste reprova (mas ainda mostra os resultados).

---

```javascript
let res1 = http.get('http://localhost:8080/api/rapida');
check(res1, {
    'rapido status 200': (r) => r.status === 200,
    'rapido tempo < 200ms': (r) => r.timings.duration < 200,
});
```

> **http.get:** Faz uma requisição GET pro endpoint.
>
> **check:** Verifica se a resposta é boa:
> - Status 200? (sucesso)
> - Tempo menor que 200ms? (rápido)
>
> É tipo fazer `assert` em teste unitário, mas pra API.

---

```javascript
sleep(1);
```

> **sleep:** Espera 1 segundo antes da próxima requisição.
>
> Por quê? Pra simular um usuário real.  
> Ninguém fica clicando 1000 vezes por segundo igual maluco.

---

## Rodando o Teste

```bash
k6 run teste-basico.js
```

---

## Resultado

```
█ THRESHOLDS

http_req_duration
✗ 'p(95)<500' p(95)=517.74ms  <-- REPROVADO

http_req_failed
✓ 'rate<0.01' rate=0.00%      <-- APROVADO


█ HTTP METRICS

http_req_duration......: avg=187ms  max=6.2s  p(95)=517ms
http_reqs..............: 9621       45.32/s   <-- RPS FINAL
```

---

### Traduzindo os Resultados

```
http_req_duration
✗ 'p(95)<500' p(95)=517.74ms
```

> Reprovou
>
> A regra era: "95% das requisições precisam ser < 500ms"  
> Mas o p95 ficou em 517ms (passou 17ms do limite).
>
> **Por quê?** Provavelmente por causa do endpoint `/lenta` que dorme 200ms e sob carga pode demorar mais.

---

```
http_req_failed
✓ 'rate<0.01' rate=0.00%
```

> Passou de ano
>
> Taxa de erro foi 0% (nenhuma requisição falhou).  
> Sistema aguentou a pressão sem cair!

---

```
http_req_duration......: avg=187ms  max=6.2s  p(95)=517ms
```

> **Tempo de Resposta:**
>
> - **avg=187ms:** Tempo médio foi 187ms (bem rápido)
> - **max=6.2s:** A requisição mais lenta demorou 6.2 segundos (provavelmente sobrecarga)
> - **p(95)=517ms:** 95% das requisições foram mais rápidas que 517ms

---

```
http_reqs..............: 9621       45.32/s
```

> essa parte é importante
>
> - **9621 requisições** no total
> - **45.32 RPS** (Requisições Por Segundo)
>
> Significa que o nosso sistema consegue aguentar **~45 requisições por segundo** com 100 usuários simultâneos.

---

## Interpretando o RPS

**45 RPS é bom ou ruim?**

Depende do contexto:

| Cenário | RPS Esperado |
|---------|--------------|
| API interna (poucos usuários) | 10-50 RPS |
| Site pequeno | 50-200 RPS |
| E-commerce médio | 200-1000 RPS |
| Rede social | 1000+ RPS |

No nosso caso, com endpoints simples, **45 RPS tá ok** para começar.

Mas dá para melhorar:
- Usar cache
- Otimizar queries
- Aumentar threads do Tomcat
- Usar Virtual Threads (Java 21+)

---

## Dicas Práticas

A maioria dos bancos de dados (MySQL, Postgres) possui comandos internos para mostrar o RPS atual (`QPS - Queries Per Second`).

Se o RPS do seu Java estiver alto, mas o do Banco estiver no limite, **a culpa é do Banco**, não do seu código.

---

## Pra Fixar

- **RPS** mede quantas requisições o seu sistema aguenta
- **Teste de carga** simula usuários reais
- **Gargalo** é sempre na parte mais fraca (código, banco, rede)
- **Meça antes de otimizar** (não chute achando que sabe)