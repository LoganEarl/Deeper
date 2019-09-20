package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.GrabDropCommand;

public class ClientPutMessage extends ClientMessage {
    public static final String HEADER = "put";

    private String toStore;
    private String toStoreIn;

    public ClientPutMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if(args.length == 3 && !args[0].isEmpty() && "in".equals(args[1]) && !args[2].isEmpty()){
            toStore = args[0];
            toStoreIn = args[2];
            return true;
        }
        return false;
    }

    @Override
    public String getUsage() {
        return "put [item name/id] in [container name/id]";
    }

    @Override
    public String getHelpText() {
        return "The scattered shards are full of danger. Should you be ripped from reality, be thrust into limbo and survive to tell the tale, " +
                "it would be good to know that your belongings waited for you in a secure location. Store your valuables";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new GrabDropCommand(toStore,toStoreIn,false,getClient(), getWorldModel()));
    }
}
