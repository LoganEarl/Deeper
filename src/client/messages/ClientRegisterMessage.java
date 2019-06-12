package client.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;

public class ClientRegisterMessage extends ClientMessage {
    public static final String HEADER = "register";

    private String userName;
    private String hPass;
    private String email = "";

    public ClientRegisterMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline pipeline){
        super(HEADER,sourceClient, executor, registry, pipeline);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if(args.length == 2){
            userName = args[0];
            hPass = args[1];
            return true;
        }else if(args.length == 3){
            userName = args[0];
            hPass = args[1];
            email = args[2];
            return true;
        }
        return false;
    }

    @Override
    protected void doActions() {
        getClient().tryUpdateInfo(getClient(),"",userName,"",hPass,email);
    }

    @Override
    public String getUsage() {
        return "register [username] [password] (email)";
    }

    @Override
    public String getHelpText() {
        return "Used to create a new account. It is always nice to see a new face.";
    }
}
