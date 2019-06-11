package world.item;

import database.DatabaseManager;

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
public class Item implements DatabaseManager.DatabaseEntry {
    private static final String CREATE_SQL = String.format(Locale.US,"INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)",
            TABLE_NAME, ITEM_ID, ENTITY_ID, ROOM_NAME, ITEM_NAME, DISPLAY_NAME);
    private static final String DELETE_SQL = String.format(Locale.US,"DELETE FROM %s WHERE %s=?",
            TABLE_NAME, ITEM_ID);
    private static final String UPDATE_SQL = String.format(Locale.US,"UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=? WHERE %s=?",
            TABLE_NAME, ENTITY_ID, ROOM_NAME, ITEM_NAME, DISPLAY_NAME, CONTAINER_ID, ITEM_ID);
    private static final String GET_ID_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE %s=?",
            TABLE_NAME, ITEM_ID);
    private static final String GET_NAME_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE (%s=? AND %s=? AND %s IS NULL AND %s IS NULL)",
            TABLE_NAME, ITEM_NAME, ROOM_NAME, ENTITY_ID, CONTAINER_ID);
    private static final String GET_ROOM_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE (%s=? AND %s IS NULL AND %s IS NULL)",
            TABLE_NAME, ROOM_NAME, ENTITY_ID, CONTAINER_ID);
    private static final String GET_OF_CONTAINER_ID_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?",
            ItemInstanceTable.TABLE_NAME, CONTAINER_ID);

    private int itemID;
    private String entityID;
    private String roomName;
    private String itemName;
    private String displayName;
    private String databaseName;
    private int containerID;
    private int lockNumber;
    private Map<String,String> itemStats;

    private Item(ResultSet entry, String databaseName) throws Exception{
        itemID = entry.getInt(ITEM_ID);
        entityID = entry.getString(ENTITY_ID);
        roomName = entry.getString(ROOM_NAME);
        if(roomName == null)
            roomName = "";
        itemName = entry.getString(ITEM_NAME);
        if(itemName == null || itemName.isEmpty() || ItemStatTable.getStatsForItem(itemName,databaseName) == null)
            throw new IllegalArgumentException("Passed in an entry without a container name");
        containerID = entry.getInt(CONTAINER_ID);
        displayName = entry.getString(DISPLAY_NAME);
        if(displayName == null)
            displayName = "";
        lockNumber = entry.getInt(LOCK_NUMBER);
        this.databaseName = databaseName;
    }

    /**
     * constructor for a new item. !Automatically persists itself! to avoid the possibility of more than one item with the same id
     * @param itemName the name of the item, used to get the item's stats
     * @param databaseName the name of the database containing the item's stats. item is stored in that database
     */
    public Item(String itemName, String databaseName){
        this(itemName, "", databaseName);
    }

    /**
     * constructor for a new item. !Automatically persists itself! to avoid the possibility of more than one item with the same id
     * @param itemName the name of the item, used to get the item's stats
     * @param displayName the custom displayed name for the item
     * @param databaseName the name of the database containing the item's stats. item is stored in that database
     */
    public Item(String itemName, String displayName, String databaseName){
        int id = Long.valueOf(System.currentTimeMillis()).hashCode();
        while(getItemByID(id, databaseName) != null)
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
     * used to get the first item in the given room with the given item name
     * @param itemName the name of the item
     * @param roomName the name of the room to search
     * @param databaseName the name of the database containing the item
     * @return an Item instance or null if not found
     */
    public static Item getItemByNameRoom(String itemName, String roomName, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL = null;
        Item toReturn;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_NAME_SQL);
                getSQL.setString(1,itemName);
                getSQL.setString(2,roomName);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next()) {
                    toReturn = new Item(accountSet,databaseName);
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
    public static List<Item> getItemsInRoom(String roomName, String databaseName){
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
                    foundItems.add(new Item(accountSet,databaseName));
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
    public static Item getItemByID(int itemID, String databaseName){
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
                    toReturn = new Item(accountSet,databaseName);
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
    public static List<Item> getItemsOfContainerID(int containerID, String databaseName){
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
                    foundItems.add(new Item(accountSet,databaseName));
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
        Item item = getItemByID(itemID,databaseName);
        if(item == null)
            return DatabaseManager.executeStatement(CREATE_SQL, databaseName,
                    itemID, entityID, roomName, itemName, displayName) > 0;
        else
            return updateInDatabase(databaseName);
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        return DatabaseManager.executeStatement(DELETE_SQL,databaseName,itemID) > 0;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        return DatabaseManager.executeStatement(UPDATE_SQL,databaseName,
                entityID, roomName, itemName, displayName, containerID, itemID) > 0;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return getItemByID(itemID,databaseName) != null;
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
    public String getItemName(){
        return itemName;
    }

    private void initStats(){
        if(itemStats == null)
            itemStats = ItemStatTable.getStatsForItem(itemName,databaseName);
    }

    public String getItemDescription(){
        initStats();
        String s = itemStats.get(ItemStatTable.ITEM_DESCRIPTION);
        return s == null? "":s;
    }

    private double getCastDouble(String tag){
        String s = itemStats.get(tag);
        if(s != null) {
            try {
                return Double.parseDouble(s);
            }catch (Exception ignored){}
        }
        return 0.0;
    }

    private int getCastInt(String tag){
        String s = itemStats.get(tag);
        if(s != null) {
            try {
                return Integer.parseInt(s);
            }catch (Exception ignored){}
        }
        return 0;
    }

    public double getWeight(){
        initStats();
        return getCastDouble(ItemStatTable.WEIGHT);
    }

    public double getVolume(){
        initStats();
        return getCastDouble(ItemStatTable.SIZE);
    }

    public int getMinDamage(){
        initStats();
        return getCastInt(ItemStatTable.MIN_DAMAGE);
    }

    public int getMaxDamage(){
        initStats();
        return getCastInt(ItemStatTable.MAX_DAMAGE);
    }

    public int getHitChance(){
        initStats();
        return getCastInt(ItemStatTable.HIT_CHANCE);
    }

    public int getArmorClass(){
        initStats();
        return getCastInt(ItemStatTable.ARMOR_CLASS);
    }

    public int getHPRegen(){
        initStats();
        return getCastInt(ItemStatTable.HP_REGEN);
    }

    public int getMPRegen(){
        initStats();
        return getCastInt(ItemStatTable.MP_REGEN);
    }

    public String getItemType(){
        initStats();
        String s = itemStats.get(ItemStatTable.ITEM_DESCRIPTION);
        return s == null? "":s;
    }

    public int getLockNumber(){
        return lockNumber;
    }

    public int getContainerID(){
        return containerID;
    }

    /**
     * stores the item in the container with the given ID. NOTE!!!, this ignores any constraints that the container may have. Do not manually place items inside of containers with this method. Instead, use the tryStoreItem() of the container. This method will null out the entityID and the roomName of the item, to show the location of the item is dependant on that of the container it is stored in. Persists changes automatically
     */
    public void setContainerID(int containerID){
        this.containerID = containerID;
        this.entityID = "";
        this.roomName = "";
        updateInDatabase(databaseName);
    }

    @Override
    public String toString(){
        return displayName;
    }
}
