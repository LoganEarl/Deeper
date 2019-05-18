package client.messages;

import network.ServerMessageType;
import network.WebServer;

/**
 * Instantiated form of a client's attempt to update account information. Still needs to be verified but contains all the info to do so.<br>
 *     Message format is as follows<br><br>
 *
 *     [ServerMessageType.CLIENT_ACCOUNT_UPDATE_MESSAGE]\n<br>
 *     oldUserName\n<br>
 *     newUserName\n<br>
 *     oldHashedPassword\n<br>
 *     newHashedPassword\n<br>
 *     newEmailAddress[WebServer.MESSAGE_DIVIDER]<br><br>
 *
 *     note* if you do not want to update the user name, make the oldUserName and the newUserName identical
 *     note* if you want to create a new account, leave the odUserName and oldHashedPassword fields blank<br><br>
 *
 *     You can also use the following format for making new accounts<br><br>
 *
 *     [ServerMessageType.CLIENT_ACCOUNT_UPDATE_MESSAGE]\n<br>
 *     newUserName\n<br>
 *     newHashedPassword\n<br>
 *     newEmailAddress[WebServer.MESSAGE_DIVIDER]<br><br>
 * @author Logan Earl
 */
public class ClientAccountUpdateMessage implements WebServer.ClientMessage {
    private String client;
    private String oldUserName;
    private String newUserName;
    private String oldHashedPassword;
    private String newHashedPassword;
    private String newEmailAddress;
    private boolean wasParsedCorrectly = false;

    public ClientAccountUpdateMessage(String client){
        this.client = client;
    }

    @Override
    public ServerMessageType getMessageType() {
        return ServerMessageType.CLIENT_ACCOUNT_UPDATE_MESSAGE;
    }

    @Override
    public String getClient() {
        return client;
    }

    public String getOldUserName() {
        return oldUserName;
    }

    public String getNewUserName() {
        return newUserName;
    }

    public String getOldHashedPassword() {
        return oldHashedPassword;
    }

    public String getNewHashedPassword() {
        return newHashedPassword;
    }

    public String getNewEmailAddress() {
        return newEmailAddress;
    }

    @Override
    public void constructFromString(String rawMessageBody) {
        String[] contents = rawMessageBody.split("\n");
        if(contents.length == 5){
            this.oldUserName = contents[0];
            this.newUserName = contents[1];
            this.oldHashedPassword = contents[2];
            this.newHashedPassword = contents[3];
            this.newEmailAddress = contents[4];
            wasParsedCorrectly = true;
        }
        if(contents.length == 3){
            this.oldUserName = "";
            this.newUserName = contents[0];
            this.oldHashedPassword = "";
            this.newHashedPassword = contents[1];
            this.newEmailAddress = contents[2];
            wasParsedCorrectly = true;
        }
    }

    @Override
    public boolean wasCorrectlyParsed() {
        return wasParsedCorrectly;
    }
}
