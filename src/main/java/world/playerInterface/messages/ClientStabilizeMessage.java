package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;

public class ClientStabilizeMessage extends ClientMessage {
    public static final String HEADER = "stabilize";

    public ClientStabilizeMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        return false;
    }

    @Override
    public String getUsage() {
        return "stabilize [target name/id]";
    }

    @Override
    public String getHelpText() {
        return "Used to help a friend (or foe) in need. Death can be a slow process and can be stopped with proper care";
    }

    @Override
    protected void doActions() {

    }
}
