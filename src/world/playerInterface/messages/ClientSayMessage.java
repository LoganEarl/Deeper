package world.playerInterface.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.playerInterface.commands.SayCommand;

public class ClientSayMessage extends ClientMessage {
    public static final String HEADER = "say";

    private String message = "";

    public ClientSayMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline pipeline){
        super(HEADER,sourceClient, executor, registry, pipeline);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        if(rawMessage != null){
            message = rawMessage.replace("\n"," ");
        }
        return true;
    }

    @Override
    protected void doActions() {
        if(message.isEmpty())
            getClient().sendMessage("What do you want to say?");
        else
            getExecutor().scheduleCommand(new SayCommand(message, getClient(),getClientRegistry()));
    }
}
