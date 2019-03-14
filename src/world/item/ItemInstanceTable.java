package world.item;

import database.DatabaseManager.DatabaseTable;
import world.entity.EntityTable;
import world.room.RoomTable;

import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

/**
 * Table definition for a SQL table that holds all the specific instances of items.
 * @author Logan Earl
 */
public class ItemInstanceTable implements DatabaseTable {
    /**The name of the database table containing item instances*/
    public static final String TABLE_NAME =  "itemInstance";

    /**The unique identifier of this item. stored as an int*/
    public static final String ITEM_ID = "itemID";
    /**The unique identifier of the entity holding this item. Use the ROOM_NAME value if this is null. Always check the ContainedItemTable first as the item may be stored*/
    public static final String ENTITY_ID = EntityTable.ENTITY_ID;
    /**If the item is not stored in a container and is not being held, this contains the room it is laying about in*/
    public static final String ROOM_NAME = RoomTable.ROOM_NAME;
    /**The name of the item entry in the ItemStatTable that serves as this items template*/
    public static final String ITEM_NAME = ItemStatTable.ITEM_NAME;
    /**The default displayed name of the item is the value stored under ITEM_NAME, but if this contains a value, it is used instead. Used for specifically named weapons and items*/
    public static final String DISPLAY_NAME = "displayName";

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    public final Map<String, String> TABLE_DEFINITION = new HashMap<>();

    public ItemInstanceTable(){
        TABLE_DEFINITION.put(ITEM_ID,"INT PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(ROOM_NAME, "VARCHAR(32)");
        TABLE_DEFINITION.put(ENTITY_ID, "VARCHAR(32)");
        TABLE_DEFINITION.put(ITEM_NAME, "VARCHAR(32)");
        TABLE_DEFINITION.put(DISPLAY_NAME,"VARCHAR(32)");
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return TABLE_DEFINITION;
    }
}
