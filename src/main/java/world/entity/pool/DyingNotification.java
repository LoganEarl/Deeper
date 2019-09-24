package main.java.world.entity.pool;

import main.java.client.ClientRegistry;
import main.java.world.entity.Entity;
import main.java.world.notification.ConcreteNotification;
import main.java.world.playerInterface.ColorTheme;

public class DyingNotification extends ConcreteNotification {
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
