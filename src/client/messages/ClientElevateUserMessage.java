package client.messages;

import client.Account;
import network.ServerMessageType;
import network.WebServer;

/**
 * Instantiated form of a client's attempt to update another account's permission level. Client must already be logged in and have a permission
 * level greater than that being dispensed, and greater than the current level of the target.<br>
 *     Message format as follows<br><br>
 *
 *      [ServerMessageType.CLIENT_ELEVATE_USER_MESSAGE]\n<br>
 *      targetName\n<br>
 *      newPermissionLevel[WebServer.MESSAGE_DIVIDER]<br><br>
 *
 *      Note, the newPermissionLevel should be an integer, and should be the value of Account.AccountType.getSavableForm()
 * @author Logan Earl
 */

public class ClientElevateUserMessage implements WebServer.ClientMessage{
    private String client;
    private Account.AccountType newAccountType;
    private String targetUserName;

    private boolean wasParsedCorrectly;

    public ClientElevateUserMessage(String sourceClient){
        this.client = sourceClient;
    }

    @Override
    public ServerMessageType getMessageType() {
        return ServerMessageType.CLIENT_ELEVATE_USER_MESSAGE;
    }

    @Override
    public String getClient() {
        return client;
    }

    public Account.AccountType getNewAccountType(){
        return newAccountType;
    }

    public String getTargetUserName(){
        return targetUserName;
    }

    @Override
    public void constructFromString(String rawMessageBody) {
        String[] contents = rawMessageBody.split("\n");
        if(contents.length == 2){
            try {
                this.targetUserName = contents[0];
                this.newAccountType = Account.AccountType.fromInt(Integer.parseInt(contents[1]));
                wasParsedCorrectly = true;
            }catch (Exception e){
                wasParsedCorrectly = false;
            }
        }
    }

    @Override
    public boolean wasCorrectlyParsed() {
        return wasParsedCorrectly;
    }
}
