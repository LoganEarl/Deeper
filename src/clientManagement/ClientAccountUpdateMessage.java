package clientManagement;

import baseNetwork.MessageType;
import baseNetwork.WebServer;
import com.sun.istack.internal.NotNull;

/**
 * Instantiated form of a client's attempt to update account information. Still needs to be verified but contains all the info to do so.
 * @author Logan Earl
 */
public class ClientAccountUpdateMessage implements WebServer.ClientMessage {
    /**the internet address of the client trying to change info*/
    public final String client;
    /**the old username associated with the account being changed. Will be blank in the event of a new account creation*/
    public final String oldUserName;
    /**the new user name to be associated with the account. Will be identical to oldUserName if no userName update is desired. If looking to create a new account, use this slot for the account user name*/
    public final String newUserName;
    /**the hashed form of the existing accounts password. Used to authenticate the changes*/
    public final String oldHashedPassword;
    /**the hashed form of the new password to be used by the account*/
    public final String newHashedPassword;
    /**the new email address to be used by the client. Must be in the form www.*@*<br>Value will overwrite any existing emails*/
    public final String newEmailAddress;

    /**
     * Sole constructor
     * @param clientAddress the internet address of the client that sent the message
     * @param oldClientUserName the old username associated with the account being changed.Leave blank in the event of a new account creation
     * @param newClientUserName the new user name to be associated with the account. Will be identical to oldUserName if no userName update is desired. If looking to create a new account, use this slot for the account user name
     * @param oldClientHashedPass the hashed form of the existing accounts password. Used to authenticate the changes
     * @param newClientHashedPass the hashed form of the new password to be used by the account
     * @param newClientEmail the new email address to be used by the client. Must be in the form www.*@*<br>Value will overwrite any existing emails
     */
    public ClientAccountUpdateMessage(
            @NotNull String clientAddress,
            @NotNull String oldClientUserName,
            @NotNull String newClientUserName,
            @NotNull String oldClientHashedPass,
            @NotNull String newClientHashedPass,
            @NotNull String newClientEmail){
        client = clientAddress;
        this.oldUserName = oldClientUserName;
        this.newUserName = newClientUserName;
        this.oldHashedPassword = oldClientHashedPass;
        this.newHashedPassword = newClientHashedPass;
        this.newEmailAddress = newClientEmail;

    }

    @Override
    public MessageType getMessageType() {
        return MessageType.ACCOUNT_UPDATE_MESSAGE;
    }

    @Override
    public String getClient() {
        return client;
    }
}
