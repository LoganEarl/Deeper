package world.playerInterface.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.notification.NotificationService;
import world.playerInterface.commands.GrabDropCommand;

public class ClientGrabMessage extends ClientMessage {
    public static final String HEADER = "take";

    private String itemID = "";
    private String fromContainer = "";

    public ClientGrabMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline messagePipeline, NotificationService notificationService) {
        super(HEADER, sourceClient, executor, registry, messagePipeline, notificationService);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if(args.length == 3 && !args[0].isEmpty() && !args[2].isEmpty() && "from".equals(args[1]) ){
            itemID = args[0];
            fromContainer = args[2];
            return true;
        }
        if(args.length == 1 && !args[0].isEmpty()){
            itemID = args[0];
            return true;
        }

        return false;
    }

    @Override
    public String getUsage() {
        return "grab [item name or id] (from [container name])";
    }

    @Override
    public String getHelpText() {
        return "As a certain reptile famously didn't say, 'Greed is Good'. Take what you can carry, the rest belongs to someone else";
    }

    @Override
    protected void doActions() {
        getExecutor().scheduleCommand(new GrabDropCommand(itemID, fromContainer, true, getClient()));
    }
}
