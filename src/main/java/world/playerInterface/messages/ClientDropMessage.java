package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.GrabDropCommand;

public class ClientDropMessage extends ClientMessage {
    public static final String HEADER = "drop";

    private String targetItemId;

    public ClientDropMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
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
        getWorldModel().getExecutor().scheduleCommand(new GrabDropCommand(targetItemId,"", false,getClient(), getWorldModel()));
    }
}
