package world.playerInterface.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.playerInterface.commands.MoveCommand;

public class ClientMoveMessage extends ClientMessage {
    public static final String HEADER = "go";
    private String direction;

    public ClientMoveMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline pipeline){
        super(HEADER,sourceClient,executor,registry,pipeline);

    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if(args.length == 1){
            direction = args[0];
            return true;
        }
        return false;
    }

    @Override
    protected void doActions() {
        getExecutor().scheduleCommand(new MoveCommand(direction,getClient()));
    }
}
