package main.java.world.item;

import main.java.database.DatabaseManager.DatabaseTable;
import main.java.database.EnumTable;
import main.java.world.entity.EntityTable;
import main.java.world.room.Domain;
import main.java.world.room.RoomTable;

import java.util.*;

/**
 * Table definition for a SQL table that holds all the specific instances of items.<br>
 * Note for retrieving items from the database, item locations follow a hierarchy.<br>
 * 1. Check the {@value CONTAINER_ID} , if it is there ignore all other values
 * This means the item is in that container and the container's location should be used instead<br>
 * 2. Check the value stored under {@value ENTITY_ID}, if it is there ignore all other values.
 * This means that the given entity is holding the item<br>
 * 3. Check the value stored under {@value ROOM_NAME}, if it is there that means that the item is
 * laying around on the floor of that room. If this value in turn is null
 * then that means the item had no location, and cannot be found by players.<br>
 *
 * @author Logan Earl
 */
public class ItemInstanceTable implements DatabaseTable {
    /**
     * The name of the database table containing item instances
     */
    public static final String TABLE_NAME = "itemInstance";

    /**
     * The unique identifier of this item. stored as an int
     */
    public static final String ITEM_ID = "itemID";
    /**
     * The unique identifier of the container that the item is in. If null, check the value under ENTITY_ID, then ROOM_NAME. Foreign key to the ContainerInstanceTable
     */
    public static final String CONTAINER_ID = "containerID";
    /**
     * The unique identifier of the entity holding this item. Use the ROOM_NAME value if this is null. Always check the Container field first as the item may be stored
     */
    public static final String ENTITY_ID = EntityTable.ENTITY_ID;
    /**
     * If the item is not stored in a container and is not being held, this contains the room it is laying about in
     */
    public static final String ROOM_NAME = RoomTable.ROOM_NAME;

    /**
     * The name of the domain the item is currently in. If the item is being held or is in a container this field should be ignored
     */
    public static final String DOMAIN = "domain";

    /**
     * The name of the item entry in the ItemStatTable that serves as this items template
     */
    public static final String ITEM_NAME = ItemStatTable.ITEM_NAME;
    /**
     * The default displayed name of the item is the value stored under ITEM_NAME, but if this contains a value, it is used instead. Used for specifically named weapons and items
     */
    public static final String DISPLAY_NAME = "displayName";
    /**
     * The code this item can unlock. If attempting to unlock a container, this will be compared to the lock number of the container. If they match, the item can serve as a key to it. If 0 or NULL it is not a key
     */
    public static final String LOCK_NUMBER = "lockNumber";
    /**
     * The traits that the item (and all items of this type) have. Must be a semi-colon separated list
     */
    public static final String INHERENT_TRAITS = "inherentTraits";
    /**
     * The traits the item (and all items of this type) bestow on their user. Must be a semi-colon separated list
     */
    public static final String BESTOWED_TRAITS = "bestowedTraits";

    public static final String STATE = "state";

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final Set<String> CONSTRAINTS = new HashSet<>();

    public ItemInstanceTable() {
        TABLE_DEFINITION.put(ITEM_ID, "INT PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(CONTAINER_ID, "INT");
        TABLE_DEFINITION.put(ROOM_NAME, "VARCHAR(32) COLLATE NOCASE");
        TABLE_DEFINITION.put(DOMAIN, "VARCHAR(16) COLLATE NOCASE DEFAULT " + Domain.surface.name());
        TABLE_DEFINITION.put(ENTITY_ID, "VARCHAR(32) COLLATE NOCASE");
        TABLE_DEFINITION.put(ITEM_NAME, "VARCHAR(32) COLLATE NOCASE");
        TABLE_DEFINITION.put(DISPLAY_NAME, "VARCHAR(32) COLLATE NOCASE");
        TABLE_DEFINITION.put(STATE, "VARCHAR(16)");
        TABLE_DEFINITION.put(LOCK_NUMBER, "INT");
        TABLE_DEFINITION.put(INHERENT_TRAITS, "TEXT");
        TABLE_DEFINITION.put(BESTOWED_TRAITS, "TEXT");

        CONSTRAINTS.add(String.format(Locale.US, "FOREIGN KEY (%s) REFERENCES %s(%s)",
                ITEM_NAME, ItemStatTable.TABLE_NAME, ItemStatTable.ITEM_NAME));
        CONSTRAINTS.add(String.format(Locale.US, "FOREIGN KEY (%s) REFERENCES %s(%s)",
                CONTAINER_ID, TABLE_NAME, ITEM_ID));
        CONSTRAINTS.add(String.format(Locale.US, "FOREIGN KEY (%s) REFERENCES %s(%s)",
                ENTITY_ID, EntityTable.TABLE_NAME, EntityTable.ENTITY_ID));
        CONSTRAINTS.add(String.format(Locale.US, "FOREIGN KEY (%s) REFERENCES %s(%s)",
                ROOM_NAME, RoomTable.TABLE_NAME, RoomTable.ROOM_NAME));
        CONSTRAINTS.add(String.format(Locale.US, "FOREIGN KEY (%s) REFERENCES %s(%s)",
                ENTITY_ID, EntityTable.TABLE_NAME, EntityTable.ENTITY_ID));
        CONSTRAINTS.add(String.format(Locale.US, "FOREIGN KEY (%s) REFERENCES %s(%s)",
                DOMAIN, Domain.TABLE_NAME, EnumTable.ENUM_NAME));
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
    public Set<String> getConstraints() {
        return CONSTRAINTS;
    }
}
