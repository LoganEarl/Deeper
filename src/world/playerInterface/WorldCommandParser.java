package world.playerInterface;

import network.MessageType;
import network.WebServer;

public class WorldCommandParser implements WebServer.ClientMessageParser {
    private WebServer.ClientMessageParser wrappedParser;

    public WorldCommandParser(WebServer.ClientMessageParser wrappedParser){
        this.wrappedParser = wrappedParser;
    }

    @Override
    public WebServer.ClientMessage parseFromString(String toParse, String sourceClient) {
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

        MessageType messageType = MessageType.parseFromString(rawMessageType);
        WebServer.ClientMessage message = null;

        switch (messageType){
            
        }



        if(message == null)
            return wrappedParser.parseFromString(toParse, sourceClient);
    }
}
