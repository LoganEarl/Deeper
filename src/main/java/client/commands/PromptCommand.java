package main.java.client.commands;

import main.java.client.Client;
import main.java.network.CommandExecutor;
import main.java.network.WebServer;

/**
 * A general purpose class used to notify the user of something. Does not have any special formatting or coloring
 * @author Logan Earl
 */
public class PromptCommand implements CommandExecutor.Command, WebServer.ServerMessage {
    private boolean complete = false;
    private String toSend;
    private Client[] clients;
    private WebServer server;
    private long messageTimestamp;

    /**The header of this message. That way the client can tell what kind of message was sent*/
    public static final String HEADER = "SERVER_PROMPT_MESSAGE";

    /**
     * Constructor to send command immediately
     * @param message the message to deliver
     * @param server the server used to send the message
     * @param clients the client(s) to receive the message
     */
    public PromptCommand(String message, WebServer server, Client... clients){
        this(message, 0, server, clients);
    }

    /**
     * Constructor to send a message at the given timestamp
     * @param message the message to deliver
     * @param messageTimestamp the unix timestamp to send the message after
     * @param server the server used to send the message
     * @param clients the client(s) to receive the message
     */
    public PromptCommand(String message, long messageTimestamp, WebServer server, Client... clients){
        toSend = message;
        this.server = server;
        this.clients = clients;
        this.messageTimestamp = messageTimestamp;
    }

    @Override
    public void execute() {
        server.notifyClients(this, clients);
        complete = true;
    }

    @Override
    public long getStartTimestamp() {
        return messageTimestamp;
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public byte[] getBytes() {
        return (toSend + WebServer.MESSAGE_DIVIDER).getBytes();
    }
}
