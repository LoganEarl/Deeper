package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.EvadeCommand;

public class ClientEvadeMessage extends ClientMessage {
    public static final String HEADER = "evade";

    private String rawEvasion;

    public ClientEvadeMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        rawEvasion = rawMessage;
        return true;
    }

    @Override
    public String getUsage() {
        return "evade [number 0-10 inclusive]";
    }

    @Override
    public String getHelpText() {
        return "Used to dodge attacks. Evading at 1 means you will attempt to dodge 1 in 10 attacks. At 10, every attack will result in an attempted dodge. Beware, dodging tires you out quickly.";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new EvadeCommand(rawEvasion,getClient(),getWorldModel()));
    }
}
