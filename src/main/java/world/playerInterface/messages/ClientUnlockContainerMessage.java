package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.LockContainerCommand;

public class ClientUnlockContainerMessage extends ClientMessage {
    public static final String HEADER = "unlock";

    private String toLock;
    private String key;

    public ClientUnlockContainerMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if(args.length == 3 && "with".equals(args[1]) && !args[0].isEmpty() && !args[2].isEmpty()){
            toLock = args[0];
            key = args[2];
            return true;
        }
        return false;
    }

    @Override
    public String getUsage() {
        return "unlock [container id/container name] with [key item id/name]";
    }

    @Override
    public String getHelpText() {
        return "Trust is great, but so are locks, which is why god made keys. Keys are much easier to steal. This unlocks a container with a key. You must be holding the key item";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new LockContainerCommand(toLock, key, false,getClient(), getWorldModel()));
    }
}
