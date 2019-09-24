package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.DeflectCommand;
import main.java.world.playerInterface.commands.EvadeCommand;

public class ClientDeflectMessage extends ClientMessage {
    public static final String HEADER = "deflect";

    private String rawDeflection;

    public ClientDeflectMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        rawDeflection = rawMessage;
        return true;
    }

    @Override
    public String getUsage() {
        return "deflect [number 0-10 inclusive]";
    }

    @Override
    public String getHelpText() {
        return "Used to mitigate damage from attacks. Deflecting at 1 means you will deflect 10% of incoming damage, taking a portion of it to your stamina instead. How much damage is transferred to stamina depends on the amount of damage deflected";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new DeflectCommand(rawDeflection,getClient(),getWorldModel()));
    }
}
