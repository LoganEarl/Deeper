package client.messages;

import network.MessageType;
import network.WebServer;

/**
 * Instantiated form of a client's attempt to login. Still needs to be verified but contains all the info to do so.<br>
 *     Message format is as follows<br><br>
 *
 *     [MessageType.CLIENT_LOGIN_MESSAGE]\n<br>
 *     userName\n<br>
 *     hashedPassword[WebServer.MESSAGE_DIVIDER]
 * @author Logan Earl
 */

public class ClientLoginMessage implements WebServer.ClientMessage {
    /**the internet address of the client trying to log in*/
    private String client;
    /**the userName entered by the client trying to log in*/
    private String userName;
    /**the hashed form of the password entered by the client trying to log in*/
    private String hashedPassword;

    private boolean wasParsedCorrectly = false;

    public ClientLoginMessage(String client){
        this.client = client;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CLIENT_LOGIN_MESSAGE;
    }

    @Override
    public String getClient() {
        return client;
    }

    public String getUserName(){
        return userName;
    }

    public String getHashedPassword(){
        return hashedPassword;
    }

    @Override
    public void constructFromString(String rawMessageBody) {
        String[] contents = rawMessageBody.split("\n");
        if(contents.length == 2){
            userName = contents[0];
            hashedPassword = contents[1];
            wasParsedCorrectly = true;
        }
    }

    @Override
    public boolean wasCorrectlyParsed() {
        return wasParsedCorrectly;
    }
}
