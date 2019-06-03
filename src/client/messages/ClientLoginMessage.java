package client.messages;

import client.Client;
import network.CommandExecutor;
import network.messaging.ClientMessage;

/**
 * Instantiated form of a client's attempt to login. Still needs to be verified but contains all the info to do so.<br>
 *     Message format is as follows<br><br>
 *
 *     [ServerMessageType.CLIENT_LOGIN_MESSAGE]\n<br>
 *     userName\n<br>
 *     hashedPassword[WebServer.MESSAGE_DIVIDER]
 * @author Logan Earl
 */

public class ClientLoginMessage extends ClientMessage {
    /**the userName entered by the client trying to log in*/
    private String userName;
    /**the hashed form of the password entered by the client trying to log in*/
    private String hashedPassword;

    public ClientLoginMessage(Client client, CommandExecutor executor){
        super("login",client,executor);
    }

    @Override
    public boolean constructFromString(String rawMessageBody) {
        String[] contents = rawMessageBody.split("\n");
        if(contents.length == 2){
            userName = contents[0];
            hashedPassword = contents[1];
            return true;
        }
        return false;
    }

    @Override
    public void doActions() {
        getClient().tryLogIn(getClient(), userName, hashedPassword);
    }
}
