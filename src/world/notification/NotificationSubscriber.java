package world.notification;

public interface NotificationSubscriber {
    void notify(Notification notification);
    String getID();
    String getRoomName();
    String getDatabaseName();
}
