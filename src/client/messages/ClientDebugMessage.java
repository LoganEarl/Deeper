package client.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;

/**
 * A debugging tool the client can use to cause printouts on the server's System.out. Just initiate with debug [any text]
 * @author Logan Earl
 */
public class ClientDebugMessage extends ClientMessage {
    private String message;

    public static final String HEADER = "debug";

    public ClientDebugMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline pipeline){
        super(HEADER,sourceClient,executor, registry, pipeline);
    }

    @Override
    public boolean constructFromString(String rawMessageBody) {
        this.message = rawMessageBody;
        return true;
    }

    @Override
    public void doActions() {
        System.out.println(message);
    }

    @Override
    public String getUsage() {
        return "debug [custom message]";
    }

    @Override
    public String getHelpText() {
        return "This is a utility command that can be used on the client to print out messages in the server's System.out: quite useless in most cases.";
    }
}
