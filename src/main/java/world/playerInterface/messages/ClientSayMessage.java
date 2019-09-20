package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.SayCommand;

public class ClientSayMessage extends ClientMessage {
    public static final String HEADER = "say";

    private String message = "";

    public ClientSayMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
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
            getWorldModel().getExecutor().scheduleCommand(new SayCommand(message, getClient(),getWorldModel()));
    }

    @Override
    public String getUsage() {
        return "say [message]";
    }

    @Override
    public String getHelpText() {
        return "Communicate with those around you. Uses include but are not limited to, saying hello, pleading for help, starting flame wars, and getting yourself kicked for spamming";
    }
}
