package network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

public class ClientForm {
    private JPanel clientView;
    private JTextField inputText;
    private JTextPane outputText;

    private BufferedInputStream in;
    private BufferedOutputStream out;

    private String sessionLog = "";


    public static void main(String... args){
        JFrame frame = new JFrame("Simulacrum");
        ClientForm mForm = new ClientForm();
        frame.setContentPane(mForm.clientView);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        mForm.setupSocket();
        mForm.init();
    }

    private void appendToDisplayText(final String text){
        SwingUtilities.invokeLater(() -> {
            String convertedText =  text.replace("\n","<br>");
            convertedText = convertedText.replace("\t","&emsp;");
            convertedText = convertedText.replace(" ","&nbsp;<wr>");
            sessionLog +=convertedText + "<br>";

            outputText.setText(sessionLog);
        });
    }

    private void init(){
        inputText.setForeground(Color.WHITE);
        inputText.setBackground(Color.BLACK);

        outputText.setForeground(Color.WHITE);
        outputText.setBackground(Color.BLACK);
        outputText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        inputText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);

                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    String entry = inputText.getText();
                    inputText.setText("");
                    String[] words = entry.split(" ");
                    StringBuilder messageToSend = new StringBuilder();

                    boolean firstLine = true;
                    for (String word : words) {
                        if (firstLine)
                            firstLine = false;
                        else
                            messageToSend.append("\n");
                        messageToSend.append(word);
                    }

                    messageToSend.append(WebServer.MESSAGE_DIVIDER);

                    try {
                        out.write(messageToSend.toString().getBytes());
                        out.flush();
                    }catch (Exception ignored){

                    }
                }
            }
        });

        inputText.grabFocus();
    }

    private void setupSocket(){
        try {
            Socket socket = new Socket("localhost", 5555);
            //Socket socket = new Socket(InetAddress.getByName("67.110.213.185"), 25560);
            in = new BufferedInputStream(socket.getInputStream());
            new Thread(() -> {
                boolean quit = false;
                while (!quit) {
                    try {
                        int available = in.available();
                        if (available > 0) {
                            byte[] message = new byte[available];
                            in.read(message, 0, available);
                            String rawMessages = new String(message);
                            rawMessages = rawMessages.replace("SERVER_PROMPT_MESSAGE\n", "");
                            String[] messages = rawMessages.split("<!EOM!>");

                            for (String msg : messages)
                                appendToDisplayText(msg);
                        }
                        Thread.sleep(50);
                    } catch (Exception e) {
                        quit = true;
                    }
                }
            }).start();

            out = new BufferedOutputStream(socket.getOutputStream());
        }catch (Exception ignored){

        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
