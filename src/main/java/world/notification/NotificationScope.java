package main.java.world.notification;

import java.util.Collection;

public interface NotificationScope {
    Collection<NotificationSubscriber> filterSubscribers(Collection<NotificationSubscriber> allSubscribers);
}
