package client.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;

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
public class ClientLogoutMessage extends ClientMessage {
    private String targetUserName = "";

    public static final String HEADER = "logout";

    public ClientLogoutMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline pipeline){
        super(HEADER,sourceClient,executor, registry, pipeline);
    }

    @Override
    public boolean constructFromString(String rawMessageBody) {
        String[] contents = rawMessageBody.split("\n");
        if(contents.length == 0)
            return true;
        if(contents.length == 1){
            targetUserName = contents[0];
            return true;
        }
        return false;
    }

    @Override
    public void doActions() {
        getClient().tryLogOut(getClient(),targetUserName);
    }
}
