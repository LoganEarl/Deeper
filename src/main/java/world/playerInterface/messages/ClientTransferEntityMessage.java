package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.TransferEntityCommand;

public class ClientTransferEntityMessage extends ClientMessage {
    public static final String HEADER = "transfer";

    private String entityID;
    private String worldID;

    public ClientTransferEntityMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if(args.length == 3 && !args[0].isEmpty() && args[1].equals("to") && !args[2].isEmpty()){
            entityID = args[0];
            worldID = args[2];

            return true;
        }
        return false;
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new TransferEntityCommand(entityID,worldID,getClient(), getWorldModel()));
    }

    @Override
    public String getUsage() {
        return "transfer [player name/entity id] to [main.java.world id/main.java.world name]";
    }

    @Override
    public String getHelpText() {
        return "The portals that connect the universe are at time limiting. You have transcended these limitations, allowing you to transport yourself and others all throughout the cosmos";
    }
}
