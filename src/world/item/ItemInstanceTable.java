package world.item;

import database.DatabaseManager.DatabaseTable;
import world.entity.EntityTable;
import world.item.container.ContainerInstanceTable;
import world.room.RoomTable;

import java.util.*;

/**
 * Table definition for a SQL table that holds all the specific instances of items.<br>
 * Note for retrieving items from the database, item locations follow a hierarchy.<br>
 *     1. Check the {@value CONTAINER_ID} , if it is there ignore all other values
 *     This means the item is in that container and the container's location should be used instead<br>
 *     2. Check the value stored under {@value ENTITY_ID}, if it is there ignore all other values.
 *     This means that the given entity is holding the item<br>
 *     3. Check the value stored under {@value ROOM_NAME}, if it is there that means that the item is
 *     laying around on the floor of that room. If this value in turn is null
 *     then that means the item had no location, and cannot be found by players.<br>
 * @author Logan Earl
 */
public class ItemInstanceTable implements DatabaseTable {
    /**The name of the database table containing item instances*/
    public static final String TABLE_NAME =  "itemInstance";

    /**The unique identifier of this item. stored as an int*/
    public static final String ITEM_ID = "itemID";
    /**The unique identifier of the container that the item is in. If null, check the value under ENTITY_ID, then ROOM_NAME. Foreign key to the ContainerInstanceTable*/
    public static final String CONTAINER_ID = ContainerInstanceTable.CONTAINER_ID;
    /**The unique identifier of the entity holding this item. Use the ROOM_NAME value if this is null. Always check the Container field first as the item may be stored*/
    public static final String ENTITY_ID = EntityTable.ENTITY_ID;
    /**If the item is not stored in a container and is not being held, this contains the room it is laying about in*/
    public static final String ROOM_NAME = RoomTable.ROOM_NAME;
    /**The name of the item entry in the ItemStatTable that serves as this items template*/
    public static final String ITEM_NAME = ItemStatTable.ITEM_NAME;
    /**The default displayed name of the item is the value stored under ITEM_NAME, but if this contains a value, it is used instead. Used for specifically named weapons and items*/
    public static final String DISPLAY_NAME = "displayName";
    /**The code this item can unlock. If attempting to unlock a container, this will be compared to the lock number of the container. If they match, the item can serve as a key to it. If 0 or NULL it is not a key*/
    public static final String LOCK_NUMBER = ContainerInstanceTable.LOCK_NUMBER;

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final List<String> CONSTRAINTS = new ArrayList<>();

    public ItemInstanceTable(){
        TABLE_DEFINITION.put(ITEM_ID,"INT PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(CONTAINER_ID,"INT");
        TABLE_DEFINITION.put(ROOM_NAME, "VARCHAR(32)");
        TABLE_DEFINITION.put(ENTITY_ID, "VARCHAR(32)");
        TABLE_DEFINITION.put(ITEM_NAME, "VARCHAR(32)");
        TABLE_DEFINITION.put(DISPLAY_NAME,"VARCHAR(32)");
        TABLE_DEFINITION.put(LOCK_NUMBER, "INT");

        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ITEM_NAME, ItemStatTable.TABLE_NAME, ItemStatTable.ITEM_NAME));
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                CONTAINER_ID, ContainerInstanceTable.TABLE_NAME, ContainerInstanceTable.CONTAINER_ID));
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return TABLE_DEFINITION;
    }

    @Override
    public List<String> getConstraints() {
        return CONSTRAINTS;
    }
}
