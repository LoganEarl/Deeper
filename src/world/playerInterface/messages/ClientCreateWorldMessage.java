package world.playerInterface.messages;

import client.Account;
import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.meta.World;

public class ClientCreateWorldMessage extends ClientMessage {
    public static final String HEADER = "conjure";

    private String templateName;

    public ClientCreateWorldMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline messagePipeline) {
        super(HEADER, sourceClient, executor, registry, messagePipeline);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if(args.length == 2 && args[0].equals("new") && !args[1].isEmpty()){
            templateName = args[1];
        }
        return false;
    }

    @Override
    protected void doActions() {
        if(getClient().getAssociatedAccount() != null &&
                getClient().getAssociatedAccount().getAccountType().compareToAcountType(Account.AccountType.ADMIN) >= 0) {
            World newWorld = World.createWorldFromTemplate(templateName);
            if(newWorld != null){
                getClient().sendMessage("Success. Your new world ID is " + newWorld.getWorldID());
            }else
                getClient().sendMessage("Failed to create world from template with name: " + templateName);
        }else
            getClient().sendMessage("You must be an admin to do that");

    }
}
