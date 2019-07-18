package world.playerInterface.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.notification.NotificationService;
import world.playerInterface.commands.CreateCharCommand;

/**
 * Message from a client representing its intention to create a new character
 * @author Logan Earl
 */
public class ClientCreateCharacterMessage extends ClientMessage {
    public static final String HEADER = "create";

    public ClientCreateCharacterMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline pipeline, NotificationService notificationService){
        super(HEADER, sourceClient,executor,registry,pipeline, notificationService);
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
        getExecutor().scheduleCommand(new CreateCharCommand(getClient(),getExecutor(),getClientRegistry(),getMessagePipeline(), getNotificationService()));
    }

    @Override
    public String getUsage() {
        return "create new character";
    }

    @Override
    public String getHelpText() {
        return "Used to start fresh on a bran new Journey. Note, you can only have one character per account. In order to create a new one, the old must be annihilated. ";
    }
}
