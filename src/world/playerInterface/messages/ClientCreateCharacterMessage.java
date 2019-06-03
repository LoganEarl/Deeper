package world.playerInterface.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.WebServer;
import network.messaging.MessagePipeline;
import world.entity.Entity;
import world.meta.World;
import world.playerInterface.commands.CreateCharCommand;

/**
 * Message from a client representing its intention to create a new character
 * @author Logan Earl
 */
public class ClientCreateCharacterMessage extends ClientMessage {
    public static final String HEADER = "create";

    public ClientCreateCharacterMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline pipeline){
        super(HEADER, sourceClient,executor,registry,pipeline);
    }

    @Override
    public boolean constructFromString(String rawMessageBody) {
        String[] args = rawMessageBody.toLowerCase().split("\n");
        if(args.length == 2 && args[0].equals("new") && args[1].equals("character")){
            return true;
        }
        return false;
    }

    @Override
    protected void doActions() {
        //this is already guaranteed to be in the world thread so we can just execute it now to save a server tick
        new CreateCharCommand(getClient(),getExecutor(),getClientRegistry(),getMessagePipeline()).execute();
    }
}
