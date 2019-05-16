package world.playerInterface;

import world.meta.World;

import java.util.HashMap;
import java.util.Map;

public class PlayerManagementService {
    private Map<String,String> controlledEntities = new HashMap<>();

    public void registerEntityControlSource(String controllerID, String entityID){
        //will register the given client id with the given entity id. Whenever that client talks, we will assume they meant from the associated entity
        World w = World.getWorldOfEntityID(entityID);
        if(w == null){
            //entity does not exist. Must have lost it somehow. oh well
        }else{

        }

    }
}
