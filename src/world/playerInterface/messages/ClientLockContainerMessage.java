package world.playerInterface.messages;

import client.Client;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.WorldModel;
import world.playerInterface.commands.LockContainerCommand;

public class ClientLockContainerMessage extends ClientMessage {
    public static final String HEADER = "lock";

    private String toLock;
    private String key;

    public ClientLockContainerMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
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
        return "lock [container id/container name] with [key item id/name]";
    }

    @Override
    public String getHelpText() {
        return "Trust is great, but so are locks. This locks a container with a key. You must be holding the key item";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new LockContainerCommand(toLock, key, true,getClient(), getWorldModel()));
    }
}
