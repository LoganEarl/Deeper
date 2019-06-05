package world.playerInterface.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;

public class ClientCreateWorldMessage extends ClientMessage {
    public static final String HEADER = "conjure";

    private String templateName;

    public ClientCreateWorldMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline messagePipeline) {
        super(HEADER, sourceClient, executor, registry, messagePipeline);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if(args.length == 2 && args[0].equals("new") && !args[1].isEmpty()){
            templateName = args[1];
        }
        return false;
    }

    @Override
    protected void doActions() {

    }
}
