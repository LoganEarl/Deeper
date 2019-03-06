package baseNetwork;

public class ServerTester {
    public static void main(String[] args) {
        SimulationManager simManager = new SimulationManager(5555);
        simManager.init();

        while(true){
            simManager.step();
            try{
                Thread.sleep(500);
            }catch (InterruptedException ignored){}
        }
    }
}
