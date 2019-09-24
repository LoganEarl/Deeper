package main.java.world.notification;

import main.java.client.ClientRegistry;
import main.java.world.entity.Entity;

public interface Notification {
    String getAsMessage(Entity viewer);
    ClientRegistry getClientRegistry();
}
