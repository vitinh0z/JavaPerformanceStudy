import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Principal {

    public static void main(String[] args) {
        int nucleos = Runtime.getRuntime().availableProcessors();
        int totalTarefas = 100;

        System.out.println("=== SEU PC TEM " + nucleos + " NÚCLEOS (Lógicos) ===");

        System.out.println("\n>>> TESTE 1: Tarefas Leves (Espera/Rede) <<<");

        long tempoPoucasThreads = rodarTeste(nucleos, totalTarefas, true);
        System.out.println("Poucas Threads (" + nucleos + "): " + tempoPoucasThreads + "ms");

        long tempoMuitasThreads = rodarTeste(50, totalTarefas, true);
        System.out.println("Muitas Threads (50): " + tempoMuitasThreads + "ms");

        System.out.println("\n>>> TESTE 2: Tarefas Pesadas (Cálculo Puro) <<<");

        tempoPoucasThreads = rodarTeste(nucleos, totalTarefas, false);
        System.out.println("Threads Ideais (" + nucleos + "): " + tempoPoucasThreads + "ms");

        tempoMuitasThreads = rodarTeste(50, totalTarefas, false);
        System.out.println("Threads em Excesso (50): " + tempoMuitasThreads + "ms");
    }

    private static long rodarTeste(int qtdThreads, int qtdTarefas, boolean ehTarefaLeve) {
        long inicio = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(qtdThreads);

        for (int i = 0; i < qtdTarefas; i++) {
            if (ehTarefaLeve) {
                executor.submit(new TarefaLeve());
            } else {
                executor.submit(new TarefaPesada());
            }
        }

        // Encerra e espera todo mundo terminar
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) { e.printStackTrace(); }

        return System.currentTimeMillis() - inicio;
    }
}