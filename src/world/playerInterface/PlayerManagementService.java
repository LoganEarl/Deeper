package world.playerInterface;

import client.Client;
import client.commands.PromptCommand;
import network.SimulationManager;
import network.WebServer;
import world.entity.Entity;
import world.meta.World;
import world.playerInterface.commands.LookCommand;
import world.playerInterface.messages.ClientLookMessage;

import java.util.HashMap;
import java.util.Map;

public class PlayerManagementService {
    private Map<String,Entity> controlledEntities = new HashMap<>();

    private SimulationManager simulationManager;

    public PlayerManagementService(SimulationManager simulationManager){
        this.simulationManager = simulationManager;
    }

    public boolean registerEntityControlSource(String controllerID, String entityID){
        //will register the given client id with the given entity id. Whenever that client talks, we will assume they meant from the associated entity
        World w = World.getWorldOfEntityID(entityID);
        if(w != null){
            Entity e;
            if((e = Entity.getEntityByEntityID(entityID, w.getDatabaseName())) != null) {
                controlledEntities.put(controllerID, e);
                return true;
            }
        }
        return false;
    }

    public void removeControlSource(String controllerID){
        controlledEntities.remove(controllerID);
    }

    public Entity getEntityOfAccount(String account){
        if(controlledEntities.containsKey(account))
            return controlledEntities.get(account);
        return null;
    }

    public void registerMessage(WebServer.ClientMessage message){
        if(!(message.getMessageType() instanceof WorldMessageType)){
            return;
        }

        Client associatedClient = simulationManager.getClientWithAddress(message.getClient());
        if(associatedClient.getStatus() != Client.ClientStatus.ACTIVE){
            simulationManager.scheduleCommand(new PromptCommand("You are unable to act in the world as you are not logged in", simulationManager.getServer(), message.getClient()));
            return;
        }

        switch ((WorldMessageType)message.getMessageType()){
            case CLIENT_LOOK_MESSAGE:
                Entity controlledEntity = getEntityOfAccount(message.getClient());
                if(controlledEntity != null)
                    simulationManager.scheduleCommand(new LookCommand((ClientLookMessage) message,controlledEntity,this));

                break;
        }
    }
}
