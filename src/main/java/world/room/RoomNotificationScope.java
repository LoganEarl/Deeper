package main.java.world.room;

import main.java.world.notification.NotificationScope;
import main.java.world.notification.NotificationSubscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RoomNotificationScope implements NotificationScope {
    private String targetRoomName;
    private String targetDatabaseName;
    private List<String> excludedIDs;

    public RoomNotificationScope(Room room, String... excludedSubscribers){
        this(room.getRoomName(),room.getDatabaseName(), excludedSubscribers);
    }

    public RoomNotificationScope(String targetRoomName, String targetDatabaseName, String... excludedSubscribers){
        this.targetRoomName = targetRoomName;
        if(this.targetRoomName == null)
            this.targetRoomName = "";
        this.targetDatabaseName = targetDatabaseName;
        this.excludedIDs = Arrays.asList(excludedSubscribers);
    }

    @Override
    public Collection<NotificationSubscriber> filterSubscribers(Collection<NotificationSubscriber> allSubscribers) {
        List<NotificationSubscriber> validSubscribers = new ArrayList<>();
        for(NotificationSubscriber subscriber: allSubscribers)
            if(!excludedIDs.contains(subscriber.getID()) &&
                    targetRoomName.equals(subscriber.getRoomName()) &&
                    targetDatabaseName.equals(subscriber.getDatabaseName()))
                validSubscribers.add(subscriber);

        return validSubscribers;
    }
}
