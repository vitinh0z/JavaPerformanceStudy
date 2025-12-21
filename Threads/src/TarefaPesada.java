public class TarefaPesada implements Runnable{
    @Override
    public void run() {
        double resultado = 0;


        for (int i = 0; i<500_000; i++){
            resultado += Math.sqrt(i* Math.PI);
        }
    }
}
