public class TarefaLeve  implements Runnable{

    @Override
    public void run() {

        try {
            Thread.sleep(200);
        } catch (InterruptedException e){
            e.printStackTrace();
        }

    }
}