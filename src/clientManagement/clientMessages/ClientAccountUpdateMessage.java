package clientManagement.clientMessages;

import baseNetwork.MessageType;
import baseNetwork.WebServer;

/**
 * Instantiated form of a client's attempt to update account information. Still needs to be verified but contains all the info to do so.<br>
 *     Message format is as follows<br><br>
 *
 *     [MessageType.CLIENT_ACCOUNT_UPDATE_MESSAGE]\n<br>
 *     oldUserName\n<br>
 *     newUserName\n<br>
 *     oldHashedPassword\n<br>
 *     newHashedPassword\n<br>
 *     newEmailAddress[WebServer.MESSAGE_DIVIDER]<br><br>
 *
 *     note* if you do not want to update the user name, make the oldUserName and the newUserName identical
 *     note* if you want to create a new account, leave the odUserName and oldHashedPassword fields blank
 *
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
    public MessageType getMessageType() {
        return MessageType.CLIENT_ACCOUNT_UPDATE_MESSAGE;
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
    }

    @Override
    public boolean wasCorrectlyParsed() {
        return wasParsedCorrectly;
    }
}
