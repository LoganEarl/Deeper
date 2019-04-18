package world.item;

import database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
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
    private static final String CREATE_SQL = String.format(Locale.US,"INSERT INTO %s (%s %s %s %s %s %s) VALUES (? ? ? ? ? ?)",
            TABLE_NAME, CONTAINER_ID, CONTAINER_NAME, ENTITY_ID, ROOM_NAME, CONTAINER_STATE, LOCK_NUMBER);
    private static final String DELETE_SQL = String.format(Locale.US,"DELETE FROM %s WHERE %s=?",
            TABLE_NAME, CONTAINER_ID);
    private static final String UPDATE_SQL = String.format(Locale.US,"UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=? WHERE %s=?",
            TABLE_NAME, CONTAINER_NAME, ENTITY_ID, ROOM_NAME, CONTAINER_STATE, LOCK_NUMBER, CONTAINER_ID);
    private static final String GET_ID_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?",
            TABLE_NAME, CONTAINER_ID);

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

    /**
     * gets the container with the specified unique identifier
     * @param containerID the unique identifier of the container
     * @param databaseName the name of the database the item is stored in
     * @return the instantiated container or null if it was not found
     */
    public static Container getContainerByContainerID(int containerID, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        Container toReturn;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_ID_SQL);
                getSQL.setInt(1,containerID);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next())
                    toReturn = new Container(accountSet,databaseName);
                else
                    toReturn = null;
                getSQL.close();
                c.close();
            }catch (SQLException e){
                toReturn = null;
            }
        }
        return toReturn;
    }

    @Override
    public boolean saveToDatabase(String databaseName) {
        Container container = getContainerByContainerID(containerID,databaseName);
        if(container == null)
            return DatabaseManager.executeStatement(CREATE_SQL,databaseName,
                    containerID, containerName, entityID, roomName, containerState, lockNumber) > 0;
        else
            return updateInDatabase(databaseName);
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        return DatabaseManager.executeStatement(DELETE_SQL,databaseName,containerID) > 0;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        return DatabaseManager.executeStatement(UPDATE_SQL, databaseName,
                containerName, entityID, roomName, containerState, lockNumber, containerID) > 0;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return getContainerByContainerID(containerID,databaseName) != null;
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
