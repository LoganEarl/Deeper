package client.commands;

import network.MessageType;
import network.SimulationManager;
import network.WebServer;

public class PromptCommand implements SimulationManager.Command, WebServer.ServerMessage {
    private boolean complete = false;
    private String toSend;
    private String[] clients;
    private WebServer server;


    public PromptCommand(String message, WebServer server, String... clients){
        toSend = message;
        this.server = server;
        this.clients = clients;
    }

    @Override
    public void execute() {
        server.notifyClients(this,clients);
        complete = true;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public byte[] getBytes() {
        return (MessageType.SERVER_PROMPT_MESSAGE + "\n" + toSend + WebServer.MESSAGE_DIVIDER).getBytes();
    }
}
