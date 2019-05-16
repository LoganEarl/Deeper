package world.playerInterface;

import world.entity.Entity;
import world.meta.World;

import java.util.HashMap;
import java.util.Map;

public class PlayerManagementService {
    private Map<String,Entity> controlledEntities = new HashMap<>();

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

    public Entity getEntityOfAccount(String account){
        if(controlledEntities.containsKey(account))
            return controlledEntities.get(account);
        return null;
    }
}
