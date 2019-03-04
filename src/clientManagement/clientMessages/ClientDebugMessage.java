package clientManagement.clientMessages;

import baseNetwork.MessageType;
import baseNetwork.WebServer;

public class ClientDebugMessage implements WebServer.ClientMessage {
    private String client;
    private String message;
    boolean parsed = false;

    public ClientDebugMessage(String sourceClient){
        this.client = sourceClient;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CLIENT_DEBUG_MESSAGE;
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public void constructFromString(String rawMessageBody) {
        this.message = rawMessageBody;
        parsed = true;
    }

    @Override
    public boolean wasCorrectlyParsed() {
        return parsed;
    }
}
