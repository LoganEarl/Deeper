package main.java.network;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ServerForm {
    private JTextArea mainTextArea;
    private JPanel panel1;

    public static void main(String[] args) {
        JFrame frame = new JFrame("SimulacrumServer");
        ServerForm mForm = new ServerForm();
        frame.setContentPane(mForm.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        ByteArrayOutputStream out = null;

        try {
            out = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(out, true, "UTF-8");
            System.setOut(ps);
        }catch (Exception ignored){

        }

        System.out.println("Hello world");
        System.out.flush();

        CommandExecutor executor = new CommandExecutor();
        SimulationManager simManager = new SimulationManager(5555, executor);
        simManager.init();

        //noinspection InfiniteLoopStatement
        while(true){
            executor.step();
            try{
                Thread.sleep(100);
                System.out.flush();
                if(out != null)
                    mForm.mainTextArea.setText(out.toString("UTF8"));
            }catch (Exception ignored){}
        }
    }
}
