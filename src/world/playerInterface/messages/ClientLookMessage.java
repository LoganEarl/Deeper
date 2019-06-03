package world.playerInterface.messages;

import network.WebServer;

/**
 * Message from a client expressing a desire to get a room's description, examine items, and to look into containers
 * @author Logan Earl
 */
public class ClientLookMessage implements WebServer.ClientMessage {
    private String client;
    private boolean wasParsed = false;

    private String examineTarget;
    private boolean isLookingInto;

    public ClientLookMessage(String sourceClient){
        client = sourceClient;
    }

    @Override
    public WebServer.MessageType getMessageType() {
        return WorldMessageType.CLIENT_LOOK_MESSAGE;
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public void constructFromString(String rawMessageBody) {
        String[] args = rawMessageBody.toLowerCase().split("\n");
        if(args.length == 0) {
            isLookingInto = false;
            examineTarget = "";
            wasParsed = true;
        }else if(args.length == 1){
            isLookingInto = false;
            examineTarget = args[0];
            wasParsed = true;
        }else if(args.length == 2 && args[0].equals("in")){
            isLookingInto = true;
            examineTarget = args[1];
            wasParsed = true;
        }else
            wasParsed = false;
    }

    @Override
    public boolean wasCorrectlyParsed() {
        return wasParsed;
    }

    /**
     * get if the client is trying to look inside the target. Assumes target is a container of some sort
     * @return true if looking inside
     */
    public boolean getIsLookingInsideTarget(){
        return isLookingInto;
    }

    /**
     * get the target of the client's scrutiny. If blank, assume it to be the room name of the entity that is doing the looking
     * @return a string that is a Room name, an Item name, an Entity Display name, or a container name
     */
    public String getTarget(){
        return examineTarget;
    }
}
