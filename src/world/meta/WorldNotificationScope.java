package world.meta;

import world.notification.NotificationScope;
import world.notification.NotificationSubscriber;

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
