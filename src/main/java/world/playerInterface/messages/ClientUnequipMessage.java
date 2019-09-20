package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.EquipCommand;

public class ClientUnequipMessage extends ClientMessage {
    public static final String HEADER = "unequip";

    private String armorName;

    public ClientUnequipMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        armorName = rawMessage;
        return true;
    }

    @Override
    public String getUsage() {
        return "unequip [armor name/id]";
    }

    @Override
    public String getHelpText() {
        return "Used to remove a piece of armor from your body, keeping it in a free hand instead. ";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new EquipCommand(armorName,false,getClient(),getWorldModel()));
    }
}
