package main.java.network;

public class ServerTester {
    public static void main(String[] args) {
        CommandExecutor executor = new CommandExecutor();
        SimulationManager simManager = new SimulationManager(5555, executor);
        simManager.init();

        //noinspection InfiniteLoopStatement
        while(true){
            executor.step();
            try{
                Thread.sleep(100);
            }catch (InterruptedException ignored){}
        }
    }
}
