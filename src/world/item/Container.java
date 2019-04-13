package world.item;

import database.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static world.item.ContainerInstanceTable.*;

/**
 * Holds the data for a container. Containers can hold any item as long as the item does not
 * exceed the storage capabilities of the container. Containers are constrained by a maximum volume,
 * a maximum weight, and a maximum number of items. A given container can have any/all of these
 * constraints. Or none if you want to make a bag of holding or something
 * @author Logan Earl
 */
public class Container implements DatabaseManager.DatabaseEntry {
    private int containerID;
    private String containerName;
    private String entityID;
    private String roomName;
    private String containerState;
    private int lockNumber;
    private String databaseName;

    private Map<String,String> containerStats;

    private Container(ResultSet entry, String databaseName) throws SQLException {
        containerID = entry.getInt(CONTAINER_ID);
        containerName = entry.getString(CONTAINER_NAME);
        entityID = entry.getString(ENTITY_ID);
        roomName = entry.getString(ROOM_NAME);
        containerState = entry.getString(CONTAINER_STATE);
        lockNumber = entry.getInt(LOCK_NUMBER);
        this.databaseName = databaseName;
    }

    @Override
    public boolean saveToDatabase(String databaseName) {
        return false;
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        return false;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        return false;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return false;
    }

    public int getContainerID() {
        return containerID;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getEntityID() {
        return entityID;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getLockNumber() {
        return lockNumber;
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
