package client.messages;

import client.Account;
import client.Client;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.WorldModel;

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

public class ClientElevateUserMessage extends ClientMessage {
    private Account.AccountType newAccountType;
    private String targetUserName;

    public static final String HEADER = "elevate";

    public ClientElevateUserMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessageBody) {
        String[] contents = rawMessageBody.split("\n");
        if(contents.length == 2){
            try {
                this.targetUserName = contents[0];
                this.newAccountType = Account.AccountType.fromInt(Integer.parseInt(contents[1]));
                return true;
            }catch (Exception e){
                return false;
            }
        }
        return false;
    }

    @Override
    public void doActions() {
        if(getClient().getStatus() == Client.ClientStatus.ACTIVE && getClient().getAssociatedAccount() != null)
            getClient().tryElevateUser(getClient(),targetUserName,newAccountType);
        else
            getClient().sendMessage("You must be logged in to do that");
    }

    @Override
    public String getUsage() {
        return "elevate [username] {1/2/3/4/5}";
    }

    @Override
    public String getHelpText() {
        return "You have been bestowed with limited power over others. You may use this to give and take away the powers of creation from your underlings.";
    }
}
