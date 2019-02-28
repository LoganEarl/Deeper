package clientManagement;

import baseNetwork.MessageType;
import baseNetwork.WebServer;

/**
 * Class that provides the link between an account and a client connection
 * @author Logan Earl
 */
public class Client {
    private ClientCollectionManager provider;
    private ClientStatus status;
    private Account associatedAccount;

    public Client(ClientCollectionManager provider){
        this.provider = provider;
        this.status = ClientStatus.UNAUTHENTICATED;
    }

    public void setStatus(ClientStatus newStatus){
        this.status = newStatus;
    }

    public void registerMessage(WebServer.ClientMessage message){
        if(message.getMessageType() == MessageType.LOGIN_MESSAGE && status == ClientStatus.UNAUTHENTICATED){
            //TODO create a command to log in this client
        }
        if(message.getMessageType() == MessageType.ACCOUNT_UPDATE_MESSAGE &&
                (status == ClientStatus.ACCOUNT_CREATION || status == ClientStatus.ACTIVE)){
            //TODO authenticate login info if ACTIVE, else check if account exists already. If ACTIVE and account exists, update info, if ACCOUNT_CREATION and doesn't exist create new account. All else, send failure message
        }
    }

    /**Enumeration of possible client states*/
    public enum ClientStatus{
        /**client has either logged out or was kicked at some point. Has no open connections associated with client account*/
        INACTIVE,
        /**client has just connected. We have an internet address but client has not yet provided any login info so we don't know who it is yet*/
        UNAUTHENTICATED,
        /**turns out the client is new, there is not an account on record for the client yet and the client is currently making one*/
        ACCOUNT_CREATION,
        /**client is both logged in and active, free to interact with the system normally*/
        ACTIVE
    }
}
