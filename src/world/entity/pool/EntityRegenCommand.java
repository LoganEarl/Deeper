package world.entity.pool;

import client.ClientRegistry;
import network.CommandExecutor;
import world.entity.Entity;

public class EntityRegenCommand implements CommandExecutor.Command {
    private ClientRegistry registry;

    public EntityRegenCommand(ClientRegistry registry){
        this.registry = registry;
    }

                              @Override
    public void execute() {
        long curTime = System.currentTimeMillis();
        for(Entity loadedEntity: Entity.getAllLoadedEntities()){





            loadedEntity.getPools().regenPools(curTime,loadedEntity.getStats());
            loadedEntity.updateInDatabase(loadedEntity.getDatabaseName());
        }
    }



    @Override
    public boolean isComplete() {
        return false;
    }
}
