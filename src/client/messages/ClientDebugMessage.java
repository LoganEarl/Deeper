package client.messages;

import client.Client;
import network.CommandExecutor;
import network.messaging.ClientMessage;

public class ClientDebugMessage extends ClientMessage {
    private String message;

    public ClientDebugMessage(Client sourceClient, CommandExecutor executor){
        super("debug",sourceClient,executor);
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean constructFromString(String rawMessageBody) {
        this.message = rawMessageBody;
        return true;
    }

    @Override
    public void doActions() {
        System.out.println(message);
    }
}
