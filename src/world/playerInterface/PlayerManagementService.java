package world.playerInterface;

import client.Client;
import client.commands.PromptCommand;
import network.SimulationManager;
import network.WebServer;
import world.entity.Entity;
import world.meta.World;
import world.playerInterface.commands.LookCommand;
import world.playerInterface.messages.ClientLookMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManagementService {

    //associates internet addresses to entity objects basically
    private Map<String,Entity> accountToEntity = new HashMap<>();
    private Map<Entity,String> entityToAccount = new HashMap<>();


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
                accountToEntity.put(controllerID, e);
                entityToAccount.put(e,controllerID);
                return true;
            }
        }
        return false;
    }

    public void removeControlSource(String controllerID){
        if(accountToEntity.containsKey(controllerID)) {
            entityToAccount.remove(accountToEntity.get(controllerID));
            accountToEntity.remove(controllerID);
        }
    }

    public void removeControlSource(Entity entity){
        if(entityToAccount.containsKey(entity)){
            accountToEntity.remove(entityToAccount.get(entity));
            entityToAccount.remove(entity);
        }
    }

    public Entity getEntityOfAccount(String account){
        if(accountToEntity.containsKey(account))
            return accountToEntity.get(account);
        return null;
    }

    public String getAccountOfEntity(Entity e){
        if(entityToAccount.containsKey(e))
            return entityToAccount.get(e);
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

    public WebServer getAttachedWebServer(){
        return simulationManager.getServer();
    }

    public void sendMessageToEntities(String message, Entity... entities){
        List<String> addresses = new ArrayList<>();
        for(Entity e : entities){
            String clientID = getAccountOfEntity(e);
            addresses.add(clientID);
        }
        simulationManager.scheduleCommand(new PromptCommand(message,simulationManager.getServer(),addresses.toArray(new String[0])));
    }
}
