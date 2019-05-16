package world.room;

import network.MessageType;
import network.WebServer;

public class LookCommand implements WebServer.ClientMessage {

    @Override
    public MessageType getMessageType() {
        return null;
    }

    @Override
    public String getClient() {
        return null;
    }

    @Override
    public void constructFromString(String rawMessageBody) {

    }

    @Override
    public boolean wasCorrectlyParsed() {
        return false;
    }
}
