package main.java.world.entity;

import main.java.world.WorldModel;

import java.util.List;

public class EntityCollection {
    private WorldModel model;

    public EntityCollection(WorldModel worldModel){
        this.model = worldModel;
    }

    public List<Entity> getEntitiesInRoom(String roomName, String databaseName, String... excludedEntityIDs){
        return Entity.getEntitiesInRoom(roomName, databaseName, model, excludedEntityIDs);
    }

    public Entity getEntityByEntityID(String entityID, String databaseName){
        return Entity.getEntityByEntityID(entityID,model,databaseName);
    }

    public Entity getEntityByDisplayName(String displayName, String roomName, String databaseName){
        return Entity.getEntityByDisplayName(displayName,roomName,model,databaseName);
    }

    public Entity getPlayableEntityByID(String entityID){
        return Entity.getPlayableEntityByID(entityID,model);
    }
}
