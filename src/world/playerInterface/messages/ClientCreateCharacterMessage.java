package world.playerInterface.messages;

import network.ClientMessage;
import network.WebServer;
import world.playerInterface.WorldMessageType;

/**
 * Message from a client representing its intention to create a new character
 * @author Logan Earl
 */
public class ClientCreateCharacterMessage implements ClientMessage {
    private String sourceClient;
    private boolean parsedCorrectly = false;

    public ClientCreateCharacterMessage(String sourceClient){
        this.sourceClient = sourceClient;
    }

    @Override
    public WebServer.MessageType getMessageType() {
        return WorldMessageType.CLIENT_CREATE_CHAR_MESSAGE;
    }

    @Override
    public String getClient() {
        return sourceClient;
    }

    @Override
    public void constructFromString(String rawMessageBody) {
        String[] args = rawMessageBody.toLowerCase().split("\n");
        if(args.length == 2 && args[0].equals("new") && args[1].equals("character")){
            parsedCorrectly = true;
        }
    }

    @Override
    public boolean wasCorrectlyParsed() {
        return parsedCorrectly;
    }
}
