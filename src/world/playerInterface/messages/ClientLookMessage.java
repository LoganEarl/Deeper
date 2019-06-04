package world.playerInterface.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.playerInterface.commands.LookCommand;

/**
 * Message from a client expressing a desire to get a room's description, examine items, and to look into containers. Possible formats include<br><br>
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

    public ClientLookMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline pipeline){
        super(HEADER,sourceClient, executor, registry, pipeline);
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
        getExecutor().scheduleCommand(new LookCommand(examineTarget,isLookingInto,getClient()));
    }
}
