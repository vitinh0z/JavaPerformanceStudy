import http from 'k6/http';
import { check, sleep } from 'k6';

// Configuração do teste
export let options = {
    stages: [
        { duration: '30s', target: 10 },  // Sobe para 10 usuários em 30s
        { duration: '1m', target: 50 },   // Sobe para 50 usuários em 1min
        { duration: '30s', target: 100 }, // Sobe para 100 usuários em 30s
        { duration: '1m', target: 100 },  // Mantém 100 por 1min
        { duration: '30s', target: 0 },   // Desce para 0 em 30s
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% das requisições < 500ms
        http_req_failed: ['rate<0.01'],   // Menos de 1% de erro
    },
};


export default function() {

    // Teste endpoint rapido
    let res1 = http.get('http://localhost:8080/api/rapida');
    check(res1, {
        'rapido status 200': (r) => r.status === 200,
        'rapido tempo < 200ms': (r) => r.timings.duration < 200,
    });

    sleep(1); // Espera 1 segundo

    // Testa endpoint lento
    let res2 = http.get('http://localhost:8080/api/lenta');
    check(res2, {
        'lento status 200': (r) => r.status === 200,
        'lento tempo < 1000ms': (r) => r.timings.duration < 1000,
    });

    sleep(1);


    // Teste Endpoint Pesado
    let res3 = http.get('http://localhost:8080/api/pesada');
    check(res3, {
        'lento status 200': (r) => r.status === 200,
        'lento tempo < 1000ms': (r) => r.timings.duration < 1000,
    });

    sleep(1);
}