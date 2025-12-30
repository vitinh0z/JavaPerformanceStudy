package benchmarks.stringVSConcatenacao;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
public class StringVSConcatenacao {


    @Benchmark
    public Boolean concaternarComString() {
       
        String resultado = "";
        for (int i = 0; i < 100; i++) {
            resultado += i;
        }
        return resultado.length() > 0;
    }

    @Benchmark
    public Boolean concatenarComString() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append(i);
        }
        return sb.length() > 0;  
    }


    public static void main(String[] args) throws RunnerException {
        
        Options opt = new OptionsBuilder()
                .include(StringVSConcatenacao.class.getSimpleName())
                .forks(1) // Roda apenas um processo
                .warmupIterations(3) // Aquece 'motor' 3 vezes
                .measurementIterations(5) // Mede valendo 5 vezes
                .build();
        new Runner(opt).run();
    }
}
