package main.java.world.entity.pool;

import main.java.network.CommandExecutor;
import main.java.world.entity.Entity;

public class EntityPoolRecalcCommand implements CommandExecutor.Command {
    private long nextExecutionTime = 0;
    @Override
    public void execute() {
        nextExecutionTime = System.currentTimeMillis() + 1000;
        for (Entity loadedEntity : Entity.getAllLoadedEntities()) {
            loadedEntity.getPools().calculatePoolMaxes(loadedEntity.getStats());
        }
    }

    @Override
    public long getStartTimestamp() {
        return nextExecutionTime;
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
