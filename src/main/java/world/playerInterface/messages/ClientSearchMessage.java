package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.SearchCommand;

public class ClientSearchMessage extends ClientMessage {
    public static final String HEADER = "search";

    public ClientSearchMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        return "room".equals(rawMessage);
    }

    @Override
    public String getUsage() {
        return "search room";
    }

    @Override
    public String getHelpText() {
        return "Only a god notices all that is at a glance. Only a fool assumes they are a god. You are neither god nor fool, search your surroundings carefully. You never know what lies waiting to be found.";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new SearchCommand(getClient(), getWorldModel()));
    }
}
