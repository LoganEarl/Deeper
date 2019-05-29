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

    private Map<Entity, List<MessageContext>> messageContexts = new HashMap<>();

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

    public boolean registerMessage(WebServer.ClientMessage message){
        if(!(message.getMessageType() instanceof WorldMessageType)){
            return false;
        }

        //special case where they want to make a new character. Can be done without logging in
        if(message.getMessageType() == WorldMessageType.CLIENT_CREATE_CHAR_MESSAGE){

            return true;
        }

        Client associatedClient = simulationManager.getClientWithAddress(message.getClient());
        if(associatedClient.getStatus() != Client.ClientStatus.ACTIVE){
            simulationManager.scheduleCommand(new PromptCommand("You are unable to act in the world as you are not logged in", simulationManager.getServer(), message.getClient()));
            return true;
        }

        switch ((WorldMessageType)message.getMessageType()){
            case CLIENT_LOOK_MESSAGE:
                Entity controlledEntity = getEntityOfAccount(message.getClient());
                if(controlledEntity != null)
                    simulationManager.scheduleCommand(new LookCommand((ClientLookMessage) message,controlledEntity,this));

                break;
        }
        return true;
    }

    public WebServer getAttachedWebServer(){
        return simulationManager.getServer();
    }

    /**
     * sends the given string message to all the given entities in the form of a {@link PromptCommand}
     * @param message the message string to send
     * @param entities the entities to receive the message
     */
    public void sendMessageToEntities(String message, Entity... entities){
        List<String> addresses = new ArrayList<>();
        for(Entity e : entities){
            String clientID = getAccountOfEntity(e);
            addresses.add(clientID);
        }
        simulationManager.scheduleCommand(new PromptCommand(message,simulationManager.getServer(),addresses.toArray(new String[0])));
    }

    /**
     * associate the given context to the entity so that incoming messages from the entity
     * are passed to the given context before going to the default one.
     * @param entity the entity to get the new context
     * @param context the context
     */
    public void addEntityMessageContext(Entity entity, MessageContext context){
        if(!messageContexts.containsKey(entity))
            messageContexts.put(entity,new ArrayList<>(4));
        messageContexts.get(entity).add(context);
    }

    /**
     * removes the given context from the entity
     * @param entity the entity with the context to remove
     * @param toRemove the context to remove
     */
    public void removeMessageContextOfEntity(Entity entity, MessageContext toRemove){
        if(messageContexts.containsKey(entity)){
             messageContexts.get(entity).remove(toRemove);
        }
    }

    /**
     * a custom context in which commands can be executed. Messages that arrive from the
     * associated entity will first be routed to the context. The context can then choose
     * to consume the message or ignore it. If it is ignored, the message will go to the
     * default context for all world messages.
     */
    public interface MessageContext {
        /**
         * @return the time in milliseconds until the context should self-invalidate to prevent memory leaks
         */
        long getTimeToExpire();

        /**
         * called when a message is received from the associated entity
         * @param fromEntity the entity that sent the message
         * @param message the message that was sent
         * @return true to consume the message, false if the message should continue on to other contexts
         */
        boolean registerMessage(Entity fromEntity, boolean isLoggedIn, WebServer.ClientMessage message);
    }
}
