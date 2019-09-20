package main.java.world.entity.pool;

import main.java.client.ClientRegistry;
import main.java.network.CommandExecutor;
import main.java.world.entity.Entity;
import main.java.world.entity.stance.DyingStance;
import main.java.world.entity.stance.StabilizedStance;
import main.java.world.entity.stance.Stance;
import main.java.world.notification.NotificationService;
import main.java.world.room.RoomNotificationScope;

public class EntityRegenCommand implements CommandExecutor.Command {
    private NotificationService service;
    private ClientRegistry registry;

    public EntityRegenCommand(ClientRegistry registry, NotificationService service) {
        this.service = service;
        this.registry = registry;
    }

    @Override
    public void execute() {
        long curTime = System.currentTimeMillis();
        for (Entity loadedEntity : Entity.getAllLoadedEntities()) {
            Stance curStance = loadedEntity.getStance();
            if(loadedEntity.getPools().isDying() &&
                    !(curStance.equals(new DyingStance()) || curStance.equals(new StabilizedStance()))){
                loadedEntity.setStance(new DyingStance());
                service.notify(new DyingNotification(loadedEntity, registry),new RoomNotificationScope(loadedEntity.getRoomName(),loadedEntity.getDatabaseName()));
            }

            Stance.RegenPacket packet = loadedEntity.getStance().receiveNextRegenPacket(loadedEntity.getStats(),curTime);
            loadedEntity.getPools().regenPools(packet);
            loadedEntity.updateInDatabase(loadedEntity.getDatabaseName());
        }
    }


    @Override
    public boolean isComplete() {
        return false;
    }
}
