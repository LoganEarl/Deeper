package main.java.client.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.entity.Entity;

/**
 * Instantiated form of a client's attempt to login. Still needs to be verified but contains all the info to do so.<br>
 *     Message format is as follows<br><br>
 *
 *     [ServerMessageType.CLIENT_LOGIN_MESSAGE]\n<br>
 *     userName\n<br>
 *     hashedPassword[WebServer.MESSAGE_DIVIDER]
 * @author Logan Earl
 */

public class ClientLoginMessage extends ClientMessage {
    /**the userName entered by the client trying to log in*/
    private String userName;
    /**the hashed form of the password entered by the client trying to log in*/
    private String hashedPassword;

    public static final String HEADER = "login";

    public ClientLoginMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessageBody) {
        String[] contents = rawMessageBody.split("\n");
        if(contents.length == 2){
            userName = contents[0];
            hashedPassword = contents[1];
            return true;
        }
        return false;
    }

    @Override
    public void doActions() {
        if(getClient().getStatus() == Client.ClientStatus.ACTIVE){
            Entity loggedEntity = getWorldModel().getEntityCollection().getPlayableEntityByID(getClient().getUserName());
            if(loggedEntity != null)
                getWorldModel().getNotificationService().unsubscribe(loggedEntity);
        }

        getClient().tryLogIn(getClient(), userName, hashedPassword);
        if(getClient().getStatus() == Client.ClientStatus.ACTIVE){
            Entity loggedEntity = getWorldModel().getEntityCollection().getPlayableEntityByID(getClient().getUserName());
            if(loggedEntity != null)
                getWorldModel().getNotificationService().subscribe(loggedEntity);
        }
    }

    @Override
    public String getUsage() {
        return "login [username] [password]";
    }

    @Override
    public String getHelpText() {
        return "Used to login to a prior created account. Note: each account can have a single playable character associated with it. Note, your account name and your character name are different";
    }
}
