package main.java.network;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ServerForm {
    private JTextPane mainTextArea;
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

        CommandExecutor executor = new CommandExecutor();
        SimulationManager simManager = new SimulationManager(5555, executor);
        simManager.init();

        String lastText = "";

        //noinspection InfiniteLoopStatement
        while(true){
            executor.step();
            try{
                Thread.sleep(100);
                System.out.flush();
                if(out != null) {
                    String newText = out.toString("UTF8");
                    if(!newText.equals(lastText)) {
                        mForm.mainTextArea.setText(newText);
                        lastText = newText;
                    }
                }
            }catch (Exception ignored){}
        }
    }
}
