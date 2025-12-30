package benchmarks.loops;

import java.util.Arrays;


import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
public class loops {

    private static final int TAMANHO = 1000;
    private int[] dados;

    @Setup
    public void setup() {
        dados = new int[TAMANHO];
    }


    @Benchmark
    public int LoopComFor(){
        int soma = 0;
        for (int i = 0; i< TAMANHO; i++){
            soma += dados[i];
        }
        return soma;
    }

    @Benchmark
    public int LoopComWhile(){
        int soma = 0;
        int i = 0;
        while (i < TAMANHO){
            soma += dados[i];
            i++;
        }
        return soma;
    }


    @Benchmark
    public int loopComForEach(){

        int soma = 0;
        for (int numero : dados){
            soma += numero;
        }
        return soma;
    }

    @Benchmark
    public int loopComStream(){
        return Arrays.stream(dados).sum();
    }


    public static void main(String[] args) throws Exception{

        Options opt = new OptionsBuilder()
                .include(loops.class.getSimpleName())
                .forks(1) // Roda apenas um processo
                .warmupIterations(3) // Aquece 'motor' 3 vezes
                .measurementIterations(6) // Mede valendo 6 vezes
                .build();
        new Runner(opt).run();
    }

}

