import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class WebServer {
    private Thread serverThread;
    private boolean serverRunning = false;

    private OnMessageReceivedListener clientListener;
    private ClientMessageParser clientParser;

    private List<ClientConnection> connectedClients = new ArrayList<>();

    public WebServer(final int port, OnMessageReceivedListener clientListener, ClientMessageParser parser) {
        this.clientListener = clientListener;
        this.clientParser = parser;

        serverThread = new Thread(() -> {
            try {
                serverRunning = true;
                ServerSocket serverSocket = new ServerSocket(port);
                while (true) {
                    ClientConnection clientConnection = new ClientConnection(serverSocket.accept());
                    connectedClients.add(clientConnection);
                    clientConnection.start();
                }

            } catch (Exception e) {
                serverRunning = false;
                System.out.println("Failed to start server thread\n" +
                        Arrays.toString(e.getStackTrace()));
            }
        });
    }

    public void startServer() {
        if (serverThread != null && !serverRunning)
            serverThread.start();
    }

    public void notifyClients(List<String> toNotify, ServerMessage message) {
        for (ClientConnection conn : connectedClients)
            if (toNotify.isEmpty() || toNotify.contains(conn.identifier))
                conn.sendMessage(message);
    }

    private class ClientConnection extends Thread{
        private Socket clientSocket;
        private String identifier;

        private BufferedOutputStream out;
        private BufferedInputStream in;

        private Queue<ServerMessage> messageQueue = new LinkedList<>();

        private boolean alive = true;

        ClientConnection(Socket clientSocket) {
            this.clientSocket = clientSocket;
            identifier = clientSocket.getInetAddress().toString();

            try{
                out = new BufferedOutputStream(clientSocket.getOutputStream());
                in = new BufferedInputStream(clientSocket.getInputStream());
                clientSocket.setKeepAlive(true);
            }catch (Exception ignored){}
        }

        private void sendMessage(ServerMessage toSend) {
            messageQueue.add(toSend);
        }

        @Override
        public void run(){
            while(alive){
                try {
                    if (!messageQueue.isEmpty()) {
                        out.write(messageQueue.remove().getBytes());
                        out.flush();
                    }
                    if(in.available() > 0){
                        StringBuilder message = new StringBuilder();
                        char last = (char)in.read();
                        while(last != '\n' && in.available() > 0){
                            message.append(last);
                            last = (char)in.read();
                        }
                        if(last != '\n')
                            message.append(last);
                        if(clientListener != null)
                            clientListener.onClientMessage(identifier,clientParser.parseFromString(message.toString()));
                    }
                }catch (Exception e){

                }
            }
            try {
                in.close();
                out.close();
                clientSocket.close();
            }catch (Exception ignored){}
        }

        private void kill(){
            alive = false;
        }
    }

    public interface ServerMessage {
        byte[] getBytes();
    }

    public interface ClientMessage {
        byte[] getBytes();
    }

    public interface ClientMessageParser{
        ClientMessage parseFromString(String toParse);
    }

    public interface OnMessageReceivedListener{
        void onClientMessage(String client, ClientMessage message);
    }
}
