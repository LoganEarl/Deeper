package clientManagement;

import baseNetwork.MessageType;
import baseNetwork.WebServer;

/**
 * Instantiated form of a client's attempt to login. Still needs to be verified but contains all the info to do so.
 * @author Logan Earl
 */
public class ClientLoginMessage implements WebServer.ClientMessage {
    /**the internet address of the client trying to log in*/
    public final String client;
    /**the userName entered by the client trying to log in*/
    public final String userName;
    /**the hashed form of the password entered by the client trying to log in*/
    public final String hashedPassword;

    public ClientLoginMessage(String client, String userName, String hashedPassword){
        this.client = client;
        this.userName = userName;
        this.hashedPassword = hashedPassword;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.LOGIN_MESSAGE;
    }

    @Override
    public String getClient() {
        return client;
    }
}
