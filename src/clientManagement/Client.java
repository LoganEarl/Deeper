package clientManagement;

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
