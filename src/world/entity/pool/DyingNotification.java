package world.entity.pool;

import client.ClientRegistry;
import world.entity.Entity;
import world.notification.Notification;
import world.notification.NotificationSubscriber;
import world.playerInterface.ColorTheme;

public class DyingNotification extends Notification {
    private Entity dyingEntity;

    public DyingNotification(Entity dyingEntity, ClientRegistry registry) {
        super(registry);
        this.dyingEntity = dyingEntity;
    }

    @Override
    public String getAsMessage(Entity viewer) {
        String response;
        if (viewer.equals(dyingEntity) && viewer.getDatabaseName().equals(dyingEntity.getDatabaseName()))
            response = ColorTheme.getMessageInColor( "You are now dying.", ColorTheme.FAILURE) + " Stabilize yourself or seek aid, for you will soon pass on";

        else
            response = ColorTheme.getMessageInColor(dyingEntity.getDisplayName() + " is dying",ColorTheme.INFORMATIVE);

        return response;
    }
}
