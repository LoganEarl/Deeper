package clientManagement.clientMessages;

import baseNetwork.MessageType;
import baseNetwork.WebServer;

public class ClientLogoutCommand implements WebServer.ClientMessage{
    private String client;
    private boolean wasParsedCorrectly = false;

    public ClientLogoutCommand(String sourceClient){
        this.client = sourceClient;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CLIENT_LOGOUT_MESSAGE;
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public void constructFromString(String rawMessageBody) {
        String[] contents = rawMessageBody.split("\n");
    }

    @Override
    public boolean wasCorrectlyParsed() {
        return wasParsedCorrectly;
    }
}
