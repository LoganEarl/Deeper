package world.playerInterface.commands;

import client.Client;
import network.CommandExecutor;
import world.WorldUtils;

public abstract class EntityCommand implements CommandExecutor.Command {
    private Client sourceClient;

    public EntityCommand(Client sourceClient){
        this.sourceClient = sourceClient;
    }

    public final void execute(){
        if(sourceClient.getStatus() != Client.ClientStatus.ACTIVE)
            sourceClient.sendMessage("You must be logged in to do that");
        else if(WorldUtils.getEntityOfClient(sourceClient) == null)
            sourceClient.sendMessage("You must have a character to do that");
        else
            executeEntityCommand();
    }

    protected abstract void executeEntityCommand();
}
