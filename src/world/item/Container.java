package world.item;

import database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
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

    private Container(ResultSet entry, String databaseName) throws Exception {
        containerID = entry.getInt(CONTAINER_ID);
        containerName = entry.getString(CONTAINER_NAME);
        if(containerName == null || containerName.isEmpty() || ContainerStatTable.getStatsForContainer(containerName,databaseName) == null)
            throw new IllegalArgumentException("Passed in an entry without a container name");

        entityID = entry.getString(ENTITY_ID);
        if(entityID == null || entityID.isEmpty()) {
            entityID = "";
            roomName = entry.getString(ROOM_NAME);
        }else
            roomName = "";
        containerState = entry.getString(CONTAINER_STATE);
        if (containerState == null || containerState.isEmpty())
            containerState = STATE_UNLOCKED;
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
            }catch (Exception e){
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

    /**
     * shortcut to Item.getItemsOfContainerID().
     * @return all items stored in this container
     * @see Item
     */
    public List<Item> getStoredItems(){
        return Item.getItemsOfContainerID(containerID,databaseName);
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

    public String getContainerDescription(){
        initStats();
        String s =  containerStats.get(ContainerStatTable.CONTAINER_DESCRIPTION);
        return s == null? "":s;
    }

    /**
     * gets if this container can store the given item without exceeding its storage constraints
     * @param i the item to check against the storage constraints
     * @return true if the item fits in the container
     */
    public boolean canHoldItem(Item i){
        double totalKgs = 0, totalLiters = 0;
        List<Item> heldItems = getStoredItems();
        for(Item stored : heldItems){
            totalKgs += stored.getWeight();
            totalLiters += stored.getVolume();
        }
        return (getMaxItems() != ContainerStatTable.CODE_NOT_USED && getMaxItems() < heldItems.size()+1) ||
                (getMaxKilograms() != ContainerStatTable.CODE_NOT_USED && getMaxKilograms() < totalKgs + i.getWeight()) ||
                (getMaxLiters() != ContainerStatTable.CODE_NOT_USED && getMaxLiters() < totalLiters + i.getVolume());
    }

    /**
     * attempts to set the lock state of this container with the given item. The lockNumbers of the items are compared. If they match and the container is lockable, the container lock state will be set successfully. Otherwise not. If the state changed, the database is updated with the new lock state.
     * @param i the item used to
     * @param wantToBeLocked true if the container should be locked
     * @return true if the container could be updated. false if not a lockable container, the item was not a key, or the lock numbers did not match
     */
    public boolean setLockedWithItem(Item i, boolean wantToBeLocked){
        if(!getIsLockable() || i.getLockNumber() == 0)
            return false;
        if(getLockNumber() == i.getLockNumber()){
            if(wantToBeLocked)
                containerState = STATE_LOCKED;
            else
                containerState = STATE_UNLOCKED;
            updateInDatabase(databaseName);
            return true;
        }
        return false;
    }

    /**
     * will attempt to store the given item in this container. Will fail to do id this container is locked or cannot hold the item due to container constraints.
     * @param toStore the item to store in teh container
     * @return true if the item was stored successfully
     */
    public boolean tryStoreItem(Item toStore){
        if(containerState.equals(STATE_LOCKED) || !canHoldItem(toStore))
            return false;
        toStore.setContainerID(containerID);
        return true;
    }

    public double getMaxKilograms(){
        initStats();
        return getCastDouble(ContainerStatTable.MAX_KGS,containerStats);
    }

    public double getMaxLiters(){
        initStats();
        return getCastDouble(ContainerStatTable.MAX_LITERS,containerStats);
    }

    public int getMaxItems(){
        initStats();
        return getCastInt(ContainerStatTable.MAX_NUMBER,containerStats);
    }

    public int getLockDifficulty(){
        initStats();
        return getCastInt(ContainerStatTable.LOCK_DIFFICULTY, containerStats);
    }

    public boolean getIsLockable(){
        initStats();
        return getLockDifficulty() != ContainerStatTable.CODE_NOT_USED;
    }

    private void initStats(){
        if(containerStats == null)
            containerStats = ContainerStatTable.getStatsForContainer(containerName,databaseName);
    }

    private double getCastDouble(String tag, Map<String,String> stats){
        String s = stats.get(tag);
        if(s != null) {
            try {
                return Double.parseDouble(s);
            }catch (Exception ignored){}
        }
        return 0.0;
    }

    private int getCastInt(String tag, Map<String,String> stats){
        String s = stats.get(tag);
        if(s != null) {
            try {
                return Integer.parseInt(s);
            }catch (Exception ignored){}
        }
        return 0;
    }
}
