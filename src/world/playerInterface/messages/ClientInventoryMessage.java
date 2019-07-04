package world.playerInterface.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.playerInterface.commands.InventoryCommand;

public class ClientInventoryMessage extends ClientMessage {
    public static final String HEADER = "inventory";

    public ClientInventoryMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline messagePipeline) {
        super(HEADER, sourceClient, executor, registry, messagePipeline);
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
        getExecutor().scheduleCommand(new InventoryCommand(getClient()));
    }
}
