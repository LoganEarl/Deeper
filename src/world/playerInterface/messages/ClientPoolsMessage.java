package world.playerInterface.messages;

import client.Client;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.WorldModel;
import world.playerInterface.commands.PoolsCommand;

public class ClientPoolsMessage extends ClientMessage {
    public static final String HEADER = "pools";

    private double updateInterval = -1;
    private boolean killExisting = false;

    public ClientPoolsMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if(args.length == 0 || (args.length == 1 && args[0].isEmpty()))
            return true;
        else if(args.length == 1 && args[0].toLowerCase().equals("stop")) {
            killExisting = true;
            return true;
        }else if((args.length == 3 || (args.length == 4 && args[3].toLowerCase().equals("seconds"))) && args[0].toLowerCase().equals("update") && args[1].toLowerCase().equals("every")){
            try{
                updateInterval = Double.parseDouble(args[2]);
                return updateInterval >= .5;
            }catch (NumberFormatException e){
                return false;
            }
        }
        return false;
    }

    @Override
    public String getUsage() {
        return "pools ({stop/update every [time interval] (seconds)})";
    }

    @Override
    public String getHelpText() {
        return "Reports on your pools as they stand. Can be used to get continuous updates. Only updates you if your pools have changed from the last time you were updated. Cannot update faster than twice a second";
    }

    @Override
    protected void doActions() {
        if(killExisting) {
            getClient().sendMessage("No longer updating pools");
            PoolsCommand.killUpdaterOfClient(getClient());
        }else
            getWorldModel().getExecutor().scheduleCommand(new PoolsCommand(true, updateInterval, getClient(), getWorldModel()));
    }
}
