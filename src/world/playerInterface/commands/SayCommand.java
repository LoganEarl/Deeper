package world.playerInterface.commands;

import client.Client;
import client.ClientRegistry;
import world.entity.Entity;

import java.util.List;

public class SayCommand extends EntityCommand {
    boolean complete = false;
    private ClientRegistry registry;
    private String message;

    public SayCommand(String message, Client sourceClient, ClientRegistry registry){
        super(sourceClient);
        this.registry = registry;
        this.message = message;
    }

    @Override
    protected void executeEntityCommand() {
        List<Entity> nearby = Entity.getEntitiesInRoom(
                getSourceEntity().getRoomName(),
                getSourceEntity().getDatabaseName(),
                getSourceEntity().getID());

        for(Entity near: nearby){
            Client connected;
            if((connected = registry.getClientWithUsername(near.getID())) != null)
                connected.sendMessage(getSourceEntity().getDisplayName() + " says " + message);
        }
        getSourceClient().sendMessage("You say " + message);

        complete = true;
    }

    @Override
    protected boolean requiresBalance() {
        return false;
    }

    @Override
    public boolean entityCommandIsComplete() {
        return complete;
    }
}
