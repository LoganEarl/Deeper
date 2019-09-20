package main.java.world.playerInterface.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.LookCommand;

/**
 * Message from a main.java.client expressing a desire to get a room's description, examine items, and to look into containers. Possible formats include<br><br>
 *
 * look<br>
 * look at [target item, container, or entity]<br>
 * look into [target entity or container]<br>
 * @author Logan Earl
 */
public class ClientLookMessage extends ClientMessage {
    private String examineTarget;
    private boolean isLookingInto;

    public static final String HEADER = "look";

    public ClientLookMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessageBody) {
        String[] args = rawMessageBody.toLowerCase().split("\n");
        if(args.length == 0 || args.length == 1 && args[0].isEmpty()) {
            isLookingInto = false;
            examineTarget = "";
            return true;
        }else if(args.length == 2 && args[0].equals("in")){
            isLookingInto = true;
            examineTarget = args[1];
            return true;
        }else if(args.length == 2 && args[0].equals("at")){
            isLookingInto = false;
            examineTarget = args[1];
        }
        return false;
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new LookCommand(examineTarget,isLookingInto,getClient(), getWorldModel()));
    }

    @Override
    public String getUsage() {
        return "look ({in/at} [item/container close by])";
    }

    @Override
    public String getHelpText() {
        return "Used to observe your surroundings, this can be used to peer closely at items, survey a new location, or even peep inside of open containers";
    }
}
