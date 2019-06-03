package world.playerInterface.messages;

import client.Client;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.WebServer;

/**
 * Message from a client representing its intention to create a new character
 * @author Logan Earl
 */
public class ClientCreateCharacterMessage extends ClientMessage {
    private boolean parsedCorrectly = false;

    public ClientCreateCharacterMessage(Client sourceClient, CommandExecutor executor){
        super("create", sourceClient,executor);
    }

    @Override
    public boolean constructFromString(String rawMessageBody) {
        String[] args = rawMessageBody.toLowerCase().split("\n");
        if(args.length == 2 && args[0].equals("new") && args[1].equals("character")){
            parsedCorrectly = true;
        }
    }

    @Override
    public boolean wasCorrectlyParsed() {
        return parsedCorrectly;
    }
}
