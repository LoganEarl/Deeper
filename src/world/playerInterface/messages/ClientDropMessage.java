package world.playerInterface.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.playerInterface.commands.GrabDropCommand;

public class ClientDropMessage extends ClientMessage {
    public static final String HEADER = "drop";

    private String targetItemId;

    public ClientDropMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline messagePipeline) {
        super(HEADER, sourceClient, executor, registry, messagePipeline);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if(args.length == 1 && !args[0].isEmpty()){
            targetItemId = args[0];
            return true;
        }
        return false;
    }

    @Override
    public String getUsage() {
        return "drop [item name/id]";
    }

    @Override
    public String getHelpText() {
        return "The only people people unhappy with a mountain of loot, are those who died under a mountain of loot. You can only hold so much";
    }

    @Override
    protected void doActions() {
        getExecutor().scheduleCommand(new GrabDropCommand(targetItemId,"", false,getClient()));
    }
}
