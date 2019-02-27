package baseNetwork;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Responsible for creating and maintaining a single server thread for sending messages and any number of client threads used
 * to communicate with connected clients
 *
 * @author Logan Earl
 */
public class WebServer {
    private Thread serverThread;
    private boolean serverRunning = false;

    private OnMessageReceivedListener clientListener;
    private ClientMessageParser clientParser;

    private static final String MESSAGE_DIVIDER = "<!EOM!>";

    private List<ClientConnection> connectedClients = new ArrayList<>();

    /**
     * Creates the server thread, but does not start it yet. To do that, use the startServer() method
     * @param port the port to start the server on
     * @param clientListener listener used to receive messages from connected clients
     * @param parser parser used to instantiate and populate newly received client messages
     */
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

    /**
     * Starts the server thread, allowing clients to connect.
     */
    public void startServer() {
        if (serverThread != null && !serverRunning)
            serverThread.start();
    }

    /**
     * sends the given message to the given clients.
     * @param message the message to send to the given clients
     * @param toNotify the identifiers of all clients to notify
     */
    public void notifyClients(ServerMessage message, String... toNotify) {
        for (ClientConnection conn : connectedClients)
            if (toNotify.length == 0 || Arrays.asList(toNotify).contains(conn.identifier))
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
                        byte[] allRawMessages = new byte[in.available()];
                        in.read(allRawMessages);
                        String allMessages = new String(allRawMessages);
                        String[] messages = allMessages.split(MESSAGE_DIVIDER);

                        if(clientListener != null)
                            for(String message: messages)
                                if(message.isEmpty())
                                    clientListener.onClientMessage(identifier,clientParser.parseFromString(message, identifier));
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

    /**
     * Interface for a message being sent from the server to clients. All messages must be able to be converted int a
     * standard byte[] for transfer to the client. All messages must terminate with the following sequence of characters
     * {@value MESSAGE_DIVIDER}
     */
    public interface ServerMessage {
        byte[] getBytes();
    }

    /**
     * Interface for a message that was sent by the client to the server. The message is converted into this format by a
     * {@link ClientMessageParser} provided to the server during its construction. The client message will have a type associated with it
     * as well as information on which client sent the message.
     */
    public interface ClientMessage {
        /**
         * gets what kind of message it is.
         * @return the type message that was received
         */
        MessageType getMessageType();

        /**
         * gets the internet address of the client that sent the message
         * @return the String value of the client that sent the message
         */
        String getClient();
    }

    /**
     * Parser provided to the WebServer during construction responsible for converting the raw string value of a received message into
     * the appropriate ClientMessage object
     */
    public interface ClientMessageParser{
        ClientMessage parseFromString(String toParse, String sourceClient);
    }

    /**
     * Listener used to alert the program at large of a newly received message from a client.
     */
    public interface OnMessageReceivedListener{
        /**
         * method called when a new message is received and has been parsed
         * @param client the client's internet address
         * @param message the message the client sent
         */
        void onClientMessage(String client, ClientMessage message);
    }
}
