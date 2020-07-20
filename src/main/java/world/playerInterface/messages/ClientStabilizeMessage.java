package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.StabilizeCommand;

public class ClientStabilizeMessage extends ClientMessage {
    public static final String HEADER = "stabilize";

    private String target;

    public ClientStabilizeMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        if(rawMessage == null || rawMessage.isEmpty())
            return false;
        target = rawMessage;
        return true;
    }

    @Override
    public String getUsage() {
        return "stabilize [target name/id]";
    }

    @Override
    public String getHelpText() {
        return "Used to help a friend (or foe) in need. Death can be a slow process and can be stopped with proper care. Use to prevent death in a dying entity.";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new StabilizeCommand(getClient(), target, getWorldModel()));
    }
}
