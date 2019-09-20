package main.java.world.meta;

import main.java.world.notification.NotificationScope;
import main.java.world.notification.NotificationSubscriber;

import java.util.Collection;

public class WorldNotificationScope implements NotificationScope {
    private String worldDatabaseName;

    public WorldNotificationScope(String worldDatabaseName){
        this.worldDatabaseName = worldDatabaseName;
    }

    @Override
    public Collection<NotificationSubscriber> filterSubscribers(Collection<NotificationSubscriber> allSubscribers) {
        return null;
    }
}
