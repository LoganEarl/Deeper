package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.MoveCommand;

public class ClientMoveMessage extends ClientMessage {
    public static final String HEADER = "go";
    private String rawConnectionIndex;

    public ClientMoveMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if (args.length == 1) {
            rawConnectionIndex = args[0];
            return true;
        }
        return false;
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new MoveCommand(rawConnectionIndex, getClient(), getWorldModel()));
    }

    @Override
    public String getUsage() {
        return "go (index of way to take)";
    }

    @Override
    public String getHelpText() {
        return "Used to move about. have caution when walking paths in exotic locations. The way foreword may be clear, but some thresholds once crossed cannot be returned from.";
    }
}
