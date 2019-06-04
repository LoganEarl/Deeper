package world.playerInterface.commands;

import client.Client;
import network.CommandExecutor;
import world.WorldUtils;
import world.entity.Entity;

public abstract class EntityCommand implements CommandExecutor.Command {
    private Client sourceClient;
    private Entity sourceEntity;
    private boolean done = false;

    public EntityCommand(Client sourceClient){
        this.sourceClient = sourceClient;
    }

    public final void execute(){
        if(sourceClient.getStatus() != Client.ClientStatus.ACTIVE) {
            sourceClient.sendMessage("You must be logged in to do that");
            done = true;
        }else if((sourceEntity = WorldUtils.getEntityOfClient(sourceClient)) == null) {
            sourceClient.sendMessage("You must have a character to do that");
            done = true;
        } else {
            executeEntityCommand();
        }
    }

    protected Entity getSourceEntity(){
        return sourceEntity;
    }

    protected Client getSourceClient(){
        return sourceClient;
    }

    public final boolean isComplete(){
        return done || entityCommandIsComplete();
    }

    protected abstract boolean entityCommandIsComplete();

    protected abstract void executeEntityCommand();
}
