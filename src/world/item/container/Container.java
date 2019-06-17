package world.item.container;

import database.DatabaseManager;
import world.item.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import static world.item.container.ContainerInstanceTable.*;

/**
 * Holds the data for a container. Containers can hold any item as long as the item does not
 * exceed the storage capabilities of the container. Containers are constrained by a maximum volume,
 * a maximum weight, and a maximum number of items. A given container can have any/all of these
 * constraints. Or none if you want to make a bag of holding or something
 * @author Logan Earl
 */
public class Container extends Item {
    private static final String GET_ROOM_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=? AND %s IS NULL",
            TABLE_NAME, ROOM_NAME, ENTITY_ID);
    private static final String GET_NAME_ROOM_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=? AND %s=? AND %s IS NULL",
            TABLE_NAME, ROOM_NAME, CONTAINER_NAME, ENTITY_ID);


    public Container(ResultSet entry, String databaseName) throws Exception {
        super(entry,databaseName);
    }

    /**
     * used to get the first container in the given room with the given name
     * @param containerName the name of the container
     * @param roomName the name of the room the container is in
     * @param databaseName the name of the database containing the container
     * @return a Container instance or null if not found
     */
    public static Container getContainerByNameRoom(String containerName, String roomName, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL = null;
        Container toReturn;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_NAME_ROOM_SQL);
                getSQL.setString(1,roomName);
                getSQL.setString(2,containerName);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next()) {
                    toReturn = new Container(accountSet,databaseName);
                }else
                    toReturn = null;
                getSQL.close();
                //c.close();
            }catch (Exception e){
                toReturn = null;
            }
        }
        return toReturn;
    }

    /**
     * gets all the containers in the given room
     * @param roomName the name of the room to check for containers
     * @param databaseName the name of the database containing the containers
     * @return a list of all containers in the room
     */
    public static List<Container> getContainersInRoom(String roomName, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        List<Container> foundContainers = new LinkedList<>();
        if(c == null)
            return Collections.emptyList();
        else{
            try {
                getSQL = c.prepareStatement(GET_ROOM_SQL);
                getSQL.setString(1,roomName);
                ResultSet accountSet = getSQL.executeQuery();
                while(accountSet.next()) {
                    foundContainers.add(new Container(accountSet,databaseName));
                }
                getSQL.close();
                //c.close();
            }catch (Exception e){
                foundContainers = Collections.emptyList();
            }
        }
        return foundContainers;
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

    public String getRoomName() {
        return roomName;
    }

    public int getLockNumber() {
        return lockNumber;
    }

    public boolean getIsLocked(){
        return this.containerState.equals(STATE_LOCKED);
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
        return getCastDouble(ContainerStatTable.MAX_KGS);
    }

    public double getMaxLiters(){
        initStats();
        return getCastDouble(ContainerStatTable.MAX_LITERS);
    }

    public int getMaxItems(){
        initStats();
        return getCastInt(ContainerStatTable.MAX_NUMBER);
    }

    public int getLockDifficulty(){
        initStats();
        return getCastInt(ContainerStatTable.LOCK_DIFFICULTY);
    }

    public boolean getIsLockable(){
        initStats();
        return getLockDifficulty() != ContainerStatTable.CODE_NOT_USED;
    }
}
