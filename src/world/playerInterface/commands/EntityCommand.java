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
        } else if(requiresBalance() && !sourceEntity.isBalanced()) {
            sourceClient.sendMessage("You must regain your balance first!");
            done = true;
        }else{
            executeEntityCommand();
            if(requiresBalance())
                setBalance();
        }
    }

    protected final Entity getSourceEntity(){
        return sourceEntity;
    }

    protected final Client getSourceClient(){
        return sourceClient;
    }

    public final boolean isComplete(){
        return done || entityCommandIsComplete();
    }

    /**
     * An overridable method that determines how unbalanced the entity is after the command completes. By default, cooldown ranges from 500 to 1000 ms depending on the dex of the entity. Only called if requiresBalance() returns true. If you want a command to require balance but not create a cooldown then simple override this and don't call the setBalanceTime() of the source entity
     */
    protected void setBalance(){
        int dex = getSourceEntity().getStats().getDexterity();
        //500 to 1000 ms depending on dex
        long cooldown = (int)(dex/100.0 * 500 + 500);
        getSourceEntity().setBalanceTime(cooldown,getSourceClient());
    }

    /**
     * Get if this command requires the usage of the balance system. Namely, is there a global cooldown in effect that prevents quick successive actions
     * @return true if cooldown is required
     */
    protected abstract boolean requiresBalance();

    protected abstract boolean entityCommandIsComplete();

    protected abstract void executeEntityCommand();
}
