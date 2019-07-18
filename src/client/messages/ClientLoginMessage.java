package client.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.entity.Entity;
import world.notification.NotificationService;

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

    public ClientLoginMessage(Client client, CommandExecutor executor, ClientRegistry registry, MessagePipeline pipeline, NotificationService notificationService){
        super(HEADER,client,executor, registry, pipeline, notificationService);
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
        getClient().tryLogIn(getClient(), userName, hashedPassword);
        if(getClient().getStatus() == Client.ClientStatus.ACTIVE){
            Entity loggedEntity = Entity.getPlayableEntityByID(getClient().getUserName());
            if(loggedEntity != null)
                getNotificationService().subscribe(loggedEntity);
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
