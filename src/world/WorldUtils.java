package world;

import client.Client;
import world.entity.Entity;
import world.meta.World;

public class WorldUtils {
    public static Entity getEntityOfClient(Client c){
        if(c.getStatus() != Client.ClientStatus.ACTIVE)
            return null;
        String entityID = c.getUserName();
        World w = World.getWorldOfEntityID(entityID);
        if(w != null)
            return Entity.getEntityByEntityID(entityID,w.getDatabaseName());
        return null;
    }

}
