package world.playerInterface.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.playerInterface.commands.LockContainerCommand;

public class ClientUnlockContainerMessage extends ClientMessage {
    public static final String HEADER = "unlock";

    private CommandExecutor executor;

    private String toLock;
    private String key;

    public ClientUnlockContainerMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline messagePipeline) {
        super(HEADER, sourceClient, executor, registry, messagePipeline);
        this.executor = executor;
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
        executor.scheduleCommand(new LockContainerCommand(toLock, key, false,getClient()));
    }
}
