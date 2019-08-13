package world.item;

import database.DatabaseManager;
import world.WorldModel;
import world.entity.Entity;
import world.meta.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import static world.item.ItemInstanceTable.*;

/**
 * Holds data for a standard item, whether it be a sword, a chair, or a potion. The type of item is decided by the value returned by getItemType() and will be one of the TYPE_* constants
 * declared in ItemStatTable. They are not declared here as item instances do not have their own stat definitions. Stats are determined by the item name, as such all Iron Swords will have
 * the same stats.
 * @author Logan Earl
 */
public abstract class Item implements DatabaseManager.DatabaseEntry {
    private static final String CREATE_SQL = String.format(Locale.US,"INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)",
            TABLE_NAME, ITEM_ID, ROOM_NAME, ITEM_NAME, DISPLAY_NAME, STATE);
    private static final String DELETE_SQL = String.format(Locale.US,"DELETE FROM %s WHERE %s=?",
            TABLE_NAME, ITEM_ID);
    private final String UPDATE_SQL = String.format(Locale.US,"UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=? WHERE %s=?",
            TABLE_NAME, ROOM_NAME, ITEM_NAME, DISPLAY_NAME, CONTAINER_ID, STATE, ITEM_ID);
    private static final String GET_ID_SQL = String.format(Locale.US,"SELECT * FROM %s INNER JOIN %s ON %s.%s=%s.%s WHERE %s=?",
            TABLE_NAME, ItemStatTable.TABLE_NAME, TABLE_NAME, ITEM_NAME, ItemStatTable.TABLE_NAME, ItemStatTable.ITEM_NAME, ITEM_ID);
    private static final String GET_NAME_SQL = String.format(Locale.US,"SELECT * FROM %s INNER JOIN %s ON %s.%s=%s.%s WHERE ((%s.%s=? OR %s=?) AND %s=?)",
            TABLE_NAME, ItemStatTable.TABLE_NAME, TABLE_NAME, ITEM_NAME, ItemStatTable.TABLE_NAME, ItemStatTable.ITEM_NAME, TABLE_NAME,ITEM_NAME,DISPLAY_NAME, ROOM_NAME);
    private static final String GET_ROOM_SQL = String.format(Locale.US, "SELECT * FROM %s INNER JOIN %s ON %s.%s=%s.%s WHERE (%s=?)",
            TABLE_NAME, ItemStatTable.TABLE_NAME, TABLE_NAME, ITEM_NAME, ItemStatTable.TABLE_NAME, ItemStatTable.ITEM_NAME, ROOM_NAME);
    private static final String GET_OF_CONTAINER_ID_SQL = String.format(Locale.US, "SELECT * FROM %s INNER JOIN %s ON %s.%s=%s.%s WHERE %s=?",
            TABLE_NAME, ItemStatTable.TABLE_NAME, TABLE_NAME, ITEM_NAME, ItemStatTable.TABLE_NAME, ItemStatTable.ITEM_NAME, CONTAINER_ID);

    private int itemID;
    private String roomName;
    private String itemName;
    private String displayName;
    private String databaseName;
    private ItemState state;
    private int containerID;
    private int lockNumber;
    private Map<String,String> itemStats;
    private ItemFactory itemFactory;

    public static final String NULL_ITEM_NAME = "!nullItemName!";

    protected Item(ResultSet entry, ItemFactory factory, String databaseName) throws Exception{
        itemID = entry.getInt(ITEM_ID);
        roomName = entry.getString(ROOM_NAME);
        if(roomName == null)
            roomName = "";
        itemName = entry.getString(ITEM_NAME);
        if(itemName == null ||
                itemName.isEmpty() ||
                itemName.toLowerCase().equals(NULL_ITEM_NAME.toLowerCase()) ||
                ItemStatTable.getStatsForItem(itemName,databaseName) == null)
            throw new IllegalArgumentException("Passed in an entry without a container name");
        containerID = entry.getInt(CONTAINER_ID);
        displayName = entry.getString(DISPLAY_NAME);
        if(displayName == null)
            displayName = "";
        lockNumber = entry.getInt(LOCK_NUMBER);
        this.databaseName = databaseName;
        this.itemFactory = factory;
    }

    /**
     * constructor for a new item. !Automatically persists itself! to avoid the possibility of more than one item with the same id
     * @param itemName the name of the item, used to get the item's stats
     * @param databaseName the name of the database containing the item's stats. item is stored in that database
     */
    public Item(String itemName, ItemFactory factory, String databaseName){
        this(itemName, "",factory, databaseName);
    }

    /**
     * constructor for a new item. !Automatically persists itself! to avoid the possibility of more than one item with the same id
     * @param itemName the name of the item, used to get the item's stats
     * @param displayName the custom displayed name for the item
     * @param databaseName the name of the database containing the item's stats. item is stored in that database
     */
    protected Item(String itemName, String displayName, ItemFactory factory, String databaseName){
        int id = Long.valueOf(System.currentTimeMillis()).hashCode();
        while(getItemByID(id, factory, databaseName) != null)
            id++;
        this.itemID = id;
        this.displayName = displayName;
        if(this.displayName == null)
            this.displayName = "";
        this.itemStats = ItemStatTable.getStatsForItem(itemName,databaseName);
        if(itemStats == null)
            throw new IllegalArgumentException("No stats exists for item name (" + itemName + ") in database (" + databaseName + ")");
        saveToDatabase(databaseName);
    }

    /**
     * Will attempt to get an item as if the given entity was searching for it in the same room
     * @param identifier either the item name or item id in an integer-parsable format
     * @param sourceEntity the entity doing the \"searching\"
     * @return either the Item or null if it could not be found
     */
    public static Item getFromEntityContext(String identifier, Entity sourceEntity, ItemFactory itemFactory){
        Item rawItem;
        try {
            int itemID = Integer.parseInt(identifier);
            rawItem = Item.getItemByID(itemID,itemFactory, sourceEntity.getDatabaseName());
            if (rawItem == null)
                rawItem = sourceEntity.getEquipment().getEquippedItem(itemID);
        } catch (NumberFormatException e) {
            rawItem = Item.getItemByNameRoom(identifier, sourceEntity.getRoomName(),itemFactory, sourceEntity.getDatabaseName());
            if (rawItem == null)
                rawItem = sourceEntity.getEquipment().getEquippedItem(identifier);
        }
        return rawItem;
    }

    /**
     * used to get the first item in the given room with the given item name
     * @param itemName the name of the item
     * @param roomName the name of the room to search
     * @param databaseName the name of the database containing the item
     * @return an Item instance or null if not found
     */
    public static Item getItemByNameRoom(String itemName, String roomName, ItemFactory itemFactory, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL = null;
        Item toReturn;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_NAME_SQL);
                getSQL.setString(1,itemName);
                getSQL.setString(2,itemName);
                getSQL.setString(3,roomName);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next()) {
                    toReturn = itemFactory.parseFromResultSet(accountSet,databaseName);
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
     * gets all the items in the given room
     * @param roomName the room name to check for items
     * @param databaseName the database containing the items
     * @return a list of all items in the room
     */
    public static List<Item> getItemsInRoom(String roomName, ItemFactory itemFactory, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        List<Item> foundItems = new LinkedList<>();
        if(c == null)
            return Collections.emptyList();
        else{
            try {
                getSQL = c.prepareStatement(GET_ROOM_SQL);
                getSQL.setString(1,roomName);
                ResultSet accountSet = getSQL.executeQuery();
                while(accountSet.next()) {
                    foundItems.add(itemFactory.parseFromResultSet(accountSet,databaseName));
                }
                getSQL.close();
                //c.close();
            }catch (Exception e){
                foundItems = Collections.emptyList();
            }
        }
        return foundItems;
    }

    /**
     * gets an item by the item ID
     * @param itemID the unique identifier of the item
     * @param databaseName the database containing the item
     * @return the instantiated item or null if not found
     */
    public static Item getItemByID(int itemID, ItemFactory itemFactory, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        Item toReturn;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_ID_SQL);
                getSQL.setInt(1,itemID);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next())
                    toReturn = itemFactory.parseFromResultSet(accountSet,databaseName);
                else
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
     * gets all items that are contained by the given container id
     * @param containerID the unique identifier of the container
     * @param databaseName the name of the database containing the data being searched for
     * @return a list of all items in the container
     */
    public static List<Item> getItemsOfContainerID(int containerID, ItemFactory itemFactory, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        List<Item> foundItems = new LinkedList<>();
        if(c == null)
            return Collections.emptyList();
        else{
            try {
                getSQL = c.prepareStatement(GET_OF_CONTAINER_ID_SQL);
                getSQL.setInt(1,containerID);
                ResultSet accountSet = getSQL.executeQuery();
                while(accountSet.next()) {
                    foundItems.add(itemFactory.parseFromResultSet(accountSet,databaseName));
                }
                getSQL.close();
                //c.close();
            }catch (Exception e){
                foundItems = Collections.emptyList();
            }
        }
        return foundItems;
    }

    @Override
    public boolean saveToDatabase(String databaseName) {
        Item item = getItemByID(itemID,itemFactory,databaseName);
        String stateString = "";
        if(state != null)
            stateString = state.toString();
        if(item == null)
            return DatabaseManager.executeStatement(CREATE_SQL, databaseName,
                    itemID, roomName, itemName, displayName, stateString) > 0;
        else
            return updateInDatabase(databaseName);
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        return DatabaseManager.executeStatement(DELETE_SQL,databaseName,itemID) > 0;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        String stateString = "";
        if(state != null)
            stateString = state.toString();

        return DatabaseManager.executeStatement(UPDATE_SQL,databaseName,
                roomName, itemName, displayName, containerID, stateString, itemID) > 0;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return getItemByID(itemID,itemFactory,databaseName) != null;
    }

    protected ItemFactory getItemFactory() {
        return itemFactory;
    }

    /**
     * @return the display name of the item if it has one. The normal item name otherwise
     */
    public String getDisplayableName(){
        return displayName != null && !displayName.isEmpty()? displayName: itemName;
    }

    /**
     * @return the name of the item, as it's unique identifier in the ItemStatTable. Not affected by the item having a display name
     */
    public final String getItemName(){
        return itemName;
    }

    protected final void initStats(){
        if(itemStats == null)
            itemStats = ItemStatTable.getStatsForItem(itemName,databaseName);
        Map<String,String> derived = getDerivedStats();
        if(derived != null)
            itemStats.putAll(getDerivedStats());
    }

    protected abstract Map<String, String> getDerivedStats();

    public abstract ItemType getItemType();

    public final String getItemDescription(){
        initStats();
        String s = itemStats.get(ItemStatTable.ITEM_DESCRIPTION);
        return s == null? "":s;
    }

    protected final String getCastString(String tag){
        String s = itemStats.get(tag);
        if(s != null) {
            return s;
        }
        return "";
    }

    protected final double getCastDouble(String tag){
        String s = itemStats.get(tag);
        if(s != null) {
            try {
                return Double.parseDouble(s);
            }catch (Exception ignored){}
        }
        return 0.0;
    }

    protected final int getCastInt(String tag){
        String s = itemStats.get(tag);
        if(s != null) {
            try {
                return Integer.parseInt(s);
            }catch (Exception ignored){}
        }
        return 0;
    }

    protected final float getCastFloat(String tag){
        String s = itemStats.get(tag);
        if(s != null) {
            try {
                return Float.parseFloat(s);
            }catch (Exception ignored){}
        }
        return 0;
    }

    public double getWeight(){
        return getIntrinsicWeight();
    }

    public final double getIntrinsicWeight(){
        initStats();
        return getCastDouble(ItemStatTable.WEIGHT);
    }

    public final double getVolume(){
        initStats();
        return getCastDouble(ItemStatTable.VOLUME);
    }

    public final String getDescription(){
        initStats();
        String s = itemStats.get(ItemStatTable.ITEM_DESCRIPTION);
        return s == null? "":s;
    }

    public final int getLockNumber(){
        return lockNumber;
    }

    public final int getContainerID(){
        return containerID;
    }

    /**
     * stores the item in the container with the given ID. NOTE!!!, this ignores any constraints that the container may have. Do not manually place items inside of containers with this method. Instead, use the tryStoreItem() of the container. This method will null out the entityID and the roomName of the item, to show the location of the item is dependant on that of the container it is stored in. Persists changes automatically
     */
    public final void setContainerID(int containerID){
        this.containerID = containerID;
        this.roomName = "";
        updateInDatabase(databaseName);
    }

    public interface ItemState{
    }

    public ItemState getState(){
        return state;
    }

    public void setState(ItemState state){
        this.state = state;
    }

    public void setRoomName(String roomName){
        this.roomName = roomName;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getItemID() {
        return itemID;
    }

    public final String getDatabaseName(){
        return databaseName;
    }

    public final boolean statsExistInWorld(World targetWorld){
        return ItemStatTable.existsInWorld(itemName,targetWorld) &&
                compositeStatsExistInWorld(targetWorld);
    }

    public final boolean writeStatsToWorld(World targetWorld){
        initStats();
        return ItemStatTable.writeStatsToWorld(itemStats,targetWorld) &&
                writeCompositeStatsToWorld(targetWorld);
    }

    public final void transferToWorld(World newWorld){
        if(!newWorld.getDatabaseName().equals(databaseName)) {
            boolean canWrite = false;
            if (!statsExistInWorld(newWorld))
                canWrite = writeStatsToWorld(newWorld);
            else
                canWrite = true;
            if(canWrite) {
                removeFromDatabase(databaseName);
                databaseName = newWorld.getDatabaseName();
                if (roomName != null && !roomName.isEmpty())
                    roomName = newWorld.getEntryRoomName();
                saveToDatabase(databaseName);
            }
        }
    }

    protected abstract boolean compositeStatsExistInWorld(World targetWorld);
    protected abstract boolean writeCompositeStatsToWorld(World targetWorld);

    @Override
    public String toString(){
        return displayName;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(obj instanceof Item){
            Item that = (Item)obj;
            return that.itemID == this.itemID;
        }
        return false;
    }
}
