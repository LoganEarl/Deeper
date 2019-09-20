package main.java.world.notification;

import main.java.client.ClientRegistry;
import main.java.world.entity.Entity;

public abstract class Notification {
    private ClientRegistry registry;

    public Notification(ClientRegistry registry){
        this.registry = registry;
    }

    public final ClientRegistry getClientRegistry(){
        return registry;
    }

    public abstract String getAsMessage(Entity viewer);
}
