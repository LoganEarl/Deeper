package world.playerInterface;

import network.WebServer;
import world.playerInterface.messages.ClientCreateCharacterMessage;
import world.playerInterface.messages.ClientLookMessage;

public class WorldMessageParser implements WebServer.ClientMessageParser {
    private WebServer.ClientMessageParser wrappedParser;

    public WorldMessageParser(WebServer.ClientMessageParser wrappedParser){
        this.wrappedParser = wrappedParser;
    }

    @Override
    public WebServer.ClientMessage parseFromString(String toParse, String sourceClient) {
        WebServer.ClientMessage message = wrappedParser.parseFromString(toParse, sourceClient);
        if(message != null)
            return message;

        String rawMessageType;
        String rawMessageBody;

        int headerLastIndex = toParse.indexOf('\n');
        if (headerLastIndex == -1 || headerLastIndex == toParse.length() - 1) {
            rawMessageType = toParse;
            rawMessageBody = "";
        }else{
            rawMessageType = toParse.substring(0, headerLastIndex);
            rawMessageBody = toParse.substring(headerLastIndex + 1);
        }

        WorldMessageType messageType = WorldMessageType.parseFromString(rawMessageType);
        message = null;

        switch (messageType){
            case CLIENT_HOLD_MESSAGE:
                break;
            case CLIENT_LOOK_MESSAGE:
                message = new ClientLookMessage(sourceClient);
                break;
            case CLIENT_STATS_MESSAGE:
                break;
            case CLIENT_ATTACK_MESSAGE:
                break;
            case CLIENT_INVENTORY_MESSAGE:
                break;
            case UNKNOWN_MESSAGE_FORMAT:
                break;
            case CLIENT_CREATE_CHAR_MESSAGE:
                message = new ClientCreateCharacterMessage(sourceClient);
                break;
        }

        if(message != null){
            message.constructFromString(rawMessageBody);
        }
        return message;
    }
}
