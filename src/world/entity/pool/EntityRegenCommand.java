package world.entity.pool;

import client.ClientRegistry;
import network.CommandExecutor;
import world.entity.Entity;
import world.entity.stance.DyingStance;
import world.entity.stance.StabilizedStance;
import world.entity.stance.Stance;
import world.notification.NotificationService;
import world.room.RoomNotificationScope;

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
