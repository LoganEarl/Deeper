package main.java.world.playerInterface.messages;

import main.java.client.Account;
import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.meta.World;

public class ClientCreateWorldMessage extends ClientMessage {
    public static final String HEADER = "conjure";

    private String templateName;

    public ClientCreateWorldMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if(args.length == 2 && args[0].equals("new") && !args[1].isEmpty()){
            templateName = args[1];
            return true;
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

    @Override
    public String getUsage() {
        return "conjure new [template name]";
    }

    @Override
    public String getHelpText() {
        return "Great is your power and infinite your wisdom. While the semblance of civilization scrounges through the shattered pieces of the universe, you create shards as you would. The power of creation is yours to do with as you please.";
    }
}
