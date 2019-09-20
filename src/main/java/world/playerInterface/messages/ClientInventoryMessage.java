package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.InventoryCommand;

public class ClientInventoryMessage extends ClientMessage {
    public static final String HEADER = "inventory";

    public ClientInventoryMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        return true;
    }

    @Override
    public String getUsage() {
        return "inventory";
    }

    @Override
    public String getHelpText() {
        return "When walking through a crowded thoroughfare, best to check your pockets often";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new InventoryCommand(getClient(), getWorldModel()));
    }
}
