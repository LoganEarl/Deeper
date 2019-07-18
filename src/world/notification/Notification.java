package world.notification;

import client.ClientRegistry;

public abstract class Notification {
    private ClientRegistry registry;

    public Notification(ClientRegistry registry){
        this.registry = registry;
    }

    public final ClientRegistry getClientRegistry(){
        return registry;
    }

    public abstract String getAsMessage(NotificationSubscriber viewer);
}
