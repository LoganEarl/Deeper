package client.messages;

import network.ServerMessageType;
import network.WebServer;

/**
 * Instantiated form of a clients logout attempt. Can also be used to log out other players<br>
 *     Message format for logging self out is as follows<br><br>
 *
 *     [ServerMessageType.CLIENT_LOGOUT_MESSAGE][WebServer.MESSAGE_DIVIDER]<br><br>
 *
 *     Message format for logging out other people is as follows<br><br>
 *
 *     [ServerMessageType.CLIENT_LOGOUT_MESSAGE]\n<br>
 *     targetUserName[WebServer.MESSAGE_DIVIDER]<br><br>
 * @author Logan Earl
 */
public class ClientLogoutMessage implements WebServer.ClientMessage{
    private String client;
    private boolean wasParsedCorrectly = false;

    private String targetUserName = "";

    public ClientLogoutMessage(String sourceClient){
        this.client = sourceClient;
    }

    @Override
    public ServerMessageType getMessageType() {
        return ServerMessageType.CLIENT_LOGOUT_MESSAGE;
    }

    @Override
    public String getClient() {
        return client;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    @Override
    public void constructFromString(String rawMessageBody) {
        String[] contents = rawMessageBody.split("\n");
        if(contents.length == 0)
            wasParsedCorrectly = true;
        if(contents.length == 1){
            targetUserName = contents[0];
            wasParsedCorrectly = true;
        }
    }

    @Override
    public boolean wasCorrectlyParsed() {
        return wasParsedCorrectly;
    }
}
