package client.commands;

import network.CommandExecutor;
import network.WebServer;

public class PromptCommand implements CommandExecutor.Command, WebServer.ServerMessage {
    private boolean complete = false;
    private String toSend;
    private String[] clients;
    private WebServer server;

    public static final String HEADER = "SERVER_PROMPT_MESSAGE";

    public PromptCommand(String message, WebServer server, String... addresses){
        toSend = message;
        this.server = server;
        this.clients = addresses;
    }

    @Override
    public void execute() {
        server.notifyClients(this,clients);
        complete = true;
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
