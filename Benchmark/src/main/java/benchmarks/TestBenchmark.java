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
    public boolean testarComRegex(){
        return cpfExemplo.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");
    }

    @Benchmark
    public boolean testarNaMao(){
        if (cpfExemplo == null){
            return false;
        }

        if (cpfExemplo.length() != 14) return false;

        if (cpfExemplo.charAt(3) != '.') return false;
        if (cpfExemplo.charAt(7) != '.') return false;
        if (cpfExemplo.charAt(11) != '-') return false;

        for (int i = 0; i < 14; i++){

            if (i == 3 || i == 7 || i == 11) continue;

            char c = cpfExemplo.charAt(i);

            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception{

        Options opt = new OptionsBuilder()
                .include(TestBenchmark.class.getSimpleName())
                .forks(1) // Roda apenas um processo
                .warmupIterations(3) // Aquece 'motor' 3 vezes
                .measurementIterations(5) // Mede valendo 5 vezes
                .build();
        new Runner(opt).run();
    }
}