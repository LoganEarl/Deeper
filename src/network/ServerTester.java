package network;

public class ServerTester {
    public static void main(String[] args) {
        CommandExecutor executor = new CommandExecutor();
        SimulationManager simManager = new SimulationManager(5555, executor);
        simManager.init();

        while(true){
            executor.step();
            try{
                Thread.sleep(500);
            }catch (InterruptedException ignored){}
        }
    }
}
