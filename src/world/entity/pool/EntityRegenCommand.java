package world.entity.pool;

import network.CommandExecutor;
import world.entity.Entity;

public class EntityRegenCommand implements CommandExecutor.Command {
    @Override
    public void execute() {
        long curTime = System.currentTimeMillis();
        for(Entity loadedEntity: Entity.getAllLoadedEnties()){
            loadedEntity.getPools().regenPools(curTime,loadedEntity.getStats());
            loadedEntity.updateInDatabase(loadedEntity.getDatabaseName());
        }
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
