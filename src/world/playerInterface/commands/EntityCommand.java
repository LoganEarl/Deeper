package world.playerInterface.commands;

import client.Client;
import network.CommandExecutor;
import world.WorldUtils;
import world.entity.Entity;
import world.entity.skill.Skill;
import world.entity.skill.SkillTable;
import static world.playerInterface.ColorTheme.*;

public abstract class EntityCommand implements CommandExecutor.Command {
    private Client sourceClient;
    private Entity sourceEntity;
    private boolean done = false;

    public EntityCommand(Client sourceClient){
        this.sourceClient = sourceClient;
        sourceEntity = WorldUtils.getEntityOfClient(sourceClient);
    }

    public final void execute(){
        Skill requiredSkill;
        if(sourceClient.getStatus() != Client.ClientStatus.ACTIVE) {
            sourceClient.sendMessage("You must be " + getMessageInColor("logged in to do that",FAILURE));
            done = true;
        }else if(sourceEntity == null) {
            sourceClient.sendMessage("You must "+ getMessageInColor("have a character",FAILURE) + " to do that");
            done = true;
        } else if(sourceEntity.getPools().isDying() && !canDoWhenDying()){
            sourceClient.sendMessage(getMessageInColor("You are dying.",FAILURE) + " You cannot do that right now. Seek help quickly before you pass on");
            done = true;
        }else if(sourceEntity.getPools().isDead() && !canDoWhenDead()){
            sourceClient.sendMessage(getMessageInColor("You are dead",FAILURE));
            done = true;
        } else if(requiresBalance() && !sourceEntity.isBalanced()) {
            sourceClient.sendMessage("You must regain your balance first!");
            done = true;
        } else if((requiredSkill = getRequiredSkill()) != null && !SkillTable.entityHasSkill(sourceEntity,requiredSkill)) {
            sourceClient.sendMessage("You must " + getMessageInColor("learn " + requiredSkill.getDisplayName(),FAILURE) + " before you can do that");
            done = true;
        } else if(getRequiredStamina() > sourceEntity.getPools().getStamina()){
            sourceClient.sendMessage("You are " + getMessageInColor("exhausted", STAMINA_COLOR));
            done = true;
        }else{
            executeEntityCommand();
            if(requiresBalance())
                setBalance();
            if(getSourceEntity().getEquipment().isEncumbered()) {
                getSourceEntity().getPools().expendStamina(getStaminaUsed() * 2);
            }else{
                getSourceEntity().getPools().expendStamina(getStaminaUsed());
            }
        }
    }

    /**
     * override to gate the command behind a skill. Default behavior is for there to be no requirement
     * @return the required skill for the command or null if no requirement is needed
     */
    protected Skill getRequiredSkill(){
        return null;
    }

    protected int getRequiredStamina(){
        return 0;
    }

    protected int getStaminaUsed(){
        return 0;
    }

    /**
     * override to set if entities can do this action while dying. Default state is for the action to be impossible while dying
     * @return true if the action can be done while dying.
     */
    protected boolean canDoWhenDying(){
        return false;
    }

    /**
     * override to set if entities can do this action while dead. Default state is for the action to be impossible while dead
     * @return true if the action can be done while dead.
     */
    protected boolean canDoWhenDead() {
        return false;
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
