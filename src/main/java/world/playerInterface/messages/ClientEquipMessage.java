package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.EquipCommand;

public class ClientEquipMessage extends ClientMessage {
    public static final String HEADER = "equip";

    private String armorName;

    public ClientEquipMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        armorName = rawMessage;
        return true;
    }

    @Override
    public String getUsage() {
        return "equip [armor name/id]";
    }

    @Override
    public String getHelpText() {
        return "Used to put a piece of armor in your hand onto your body. The more steel between you and whatever you face, the better";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new EquipCommand(armorName,true,getClient(),getWorldModel()));
    }
}
