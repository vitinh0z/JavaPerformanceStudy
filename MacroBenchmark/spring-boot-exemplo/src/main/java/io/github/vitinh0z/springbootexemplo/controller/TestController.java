package io.github.vitinh0z.springbootexemplo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/rapida")
    public String endpoitRapido(){
        return "Resposta Rapida";
    }

    @GetMapping("/lenta")
    public String endpoinLento() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "Resposta lenta";
    }

    @GetMapping("/pesada")
    public String endpointPesado(){
        double resultado = 0;

        for (int i = 0; i < 1_000_000; i++){
            resultado += Math.sqrt(i * Math.PI);
        }
        return "Processamento concluido " + resultado;
    }
}
