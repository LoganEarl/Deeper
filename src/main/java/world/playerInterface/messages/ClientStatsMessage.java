package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.StatsCommand;

public class ClientStatsMessage extends ClientMessage {
    public static final String HEADER = "stats";

    public ClientStatsMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        return true;
    }

    @Override
    public String getUsage() {
        return "stats";
    }

    @Override
    public String getHelpText() {
        return "Used to obtain information about oneself. Used far and wide for self-reflection and narcissism alike";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new StatsCommand(getClient(),getWorldModel()));
    }
}
