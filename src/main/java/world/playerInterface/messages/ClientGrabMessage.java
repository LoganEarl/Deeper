package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.GrabDropCommand;

public class ClientGrabMessage extends ClientMessage {
    public static final String HEADER = "take";

    private String itemID = "";
    private String fromContainer = "";

    public ClientGrabMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if(args.length == 3 && !args[0].isEmpty() && !args[2].isEmpty() && "from".equals(args[1]) ){
            itemID = args[0];
            fromContainer = args[2];
            return true;
        }
        if(args.length == 1 && !args[0].isEmpty()){
            itemID = args[0];
            return true;
        }

        return false;
    }

    @Override
    public String getUsage() {
        return "grab [item name or id] (from [container name])";
    }

    @Override
    public String getHelpText() {
        return "As a certain reptile famously didn't say, 'Greed is Good'. Take what you can carry, the rest belongs to someone else";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new GrabDropCommand(itemID, fromContainer, true, getClient(), getWorldModel()));
    }
}
