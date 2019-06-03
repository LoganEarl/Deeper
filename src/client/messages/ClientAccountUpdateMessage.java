package client.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;

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
public class ClientAccountUpdateMessage extends ClientMessage {
    private String oldUserName;
    private String newUserName;
    private String oldHashedPassword;
    private String newHashedPassword;
    private String newEmailAddress;

    public static final String HEADER = "update";

    public ClientAccountUpdateMessage(Client client, CommandExecutor executor, ClientRegistry registry, MessagePipeline pipeline){
        super(HEADER,client,executor, registry, pipeline);
    }

    @Override
    public boolean constructFromString(String rawMessageBody) {
        String[] contents = rawMessageBody.split("\n");
        if(contents.length == 5){
            this.oldUserName = contents[0];
            this.newUserName = contents[1];
            this.oldHashedPassword = contents[2];
            this.newHashedPassword = contents[3];
            this.newEmailAddress = contents[4];
            return true;
        }
        if(contents.length == 3){
            this.oldUserName = "";
            this.newUserName = contents[0];
            this.oldHashedPassword = "";
            this.newHashedPassword = contents[1];
            this.newEmailAddress = contents[2];
            return true;
        }
        return false;
    }

    @Override
    public void doActions() {
        getClient().tryUpdateInfo(getClient(),oldUserName,newUserName,oldHashedPassword,newHashedPassword,newEmailAddress);
    }
}
