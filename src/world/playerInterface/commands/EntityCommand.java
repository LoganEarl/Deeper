package world.playerInterface.commands;

import client.Client;
import network.CommandExecutor;
import world.WorldUtils;
import world.entity.Entity;

public abstract class EntityCommand implements CommandExecutor.Command {
    private Client sourceClient;
    private Entity sourceEntity;

    public EntityCommand(Client sourceClient){
        this.sourceClient = sourceClient;
    }

    public final void execute(){
        if(sourceClient.getStatus() != Client.ClientStatus.ACTIVE)
            sourceClient.sendMessage("You must be logged in to do that");
        else if((sourceEntity = WorldUtils.getEntityOfClient(sourceClient)) == null)
            sourceClient.sendMessage("You must have a character to do that");
        else {
            executeEntityCommand();
        }
    }

    protected Entity getSourceEntity(){
        return sourceEntity;
    }

    protected Client getSourceClient(){
        return sourceClient;
    }

    protected abstract void executeEntityCommand();
}
