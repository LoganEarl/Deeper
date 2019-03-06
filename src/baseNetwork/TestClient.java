package baseNetwork;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) throws Exception {
        //TODO redo this to use new message formatting
        Socket socket = new Socket("localhost", 5555);
        final BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
        new Thread(() -> {
            boolean quit = false;
            while (!quit) {
                try {
                    int available = in.available();
                    if (available > 0) {
                        byte[] message = new byte[available];
                        in.read(message, 0, available);
                        System.out.println(new String(message));
                    }
                    Thread.sleep(100);
                } catch (Exception e) {
                    quit = true;
                }
            }
        }).start();

        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

        Scanner keyBoard = new Scanner(System.in);
        String entry = "";
        while (!entry.toLowerCase().equals("q") && !entry.toLowerCase().equals("quit")) {
            entry = keyBoard.nextLine();
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
            out.write(messageToSend.toString().getBytes());
            out.flush();
        }
        out.close();
    }
}
