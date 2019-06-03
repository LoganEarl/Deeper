package client.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;

public class ClientDebugMessage extends ClientMessage {
    private String message;

    public static final String HEADER = "debug";

    public ClientDebugMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline pipeline){
        super(HEADER,sourceClient,executor, registry, pipeline);
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
