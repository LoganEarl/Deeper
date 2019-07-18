package world.notification;

import client.Client;
import client.ClientRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NotificationService {
    private Collection<NotificationSubscriber> subscribers = new ArrayList<>();
    private Map<NotificationSubscriber, Long> lastTimestamps = new HashMap<>();

    private static final long TIMEOUT_INTERVAL = 1000 * 60 * 15; //15 minutes
    private ClientRegistry registry;

    public NotificationService(ClientRegistry registry){
        this.registry = registry;
    }

    public void checkTimeouts(){
        long curTime = System.currentTimeMillis();

        for(NotificationSubscriber subscriber : subscribers){
            long lastPing = 0;
            if(lastTimestamps.containsKey(subscriber))
                lastPing = lastTimestamps.get(subscriber);
            if(lastPing + TIMEOUT_INTERVAL < curTime){
                subscriber.notify(new TimeoutNotification(registry));
                Client connectedClient = registry.getClientWithUsername(subscriber.getID());
                if(connectedClient != null){
                    connectedClient.tryLogOut(connectedClient, "");
                }
            }
        }
    }

    public void notify(Notification notification, NotificationScope scope){
        Collection<NotificationSubscriber> toNotify = scope.filterSubscribers(subscribers);
        for(NotificationSubscriber subscriber : toNotify) {
            subscriber.notify(notification);
            lastTimestamps.put(subscriber,System.currentTimeMillis());
        }
    }

    public void subscribe(NotificationSubscriber subscriber){
        subscribers.add(subscriber);
        lastTimestamps.put(subscriber, System.currentTimeMillis());
    }

    public void unsubscribe(NotificationSubscriber subscriber){
        subscribers.remove(subscriber);
        lastTimestamps.remove(subscriber);
    }

    public class TimeoutNotification extends Notification{
        public TimeoutNotification(ClientRegistry registry) {
            super(registry);
        }

        @Override
        public String getAsMessage(NotificationSubscriber viewer) {
            return "You have been inactive for " + TIMEOUT_INTERVAL/1000/60 + " minutes and have been disconnected";
        }
    }
}
