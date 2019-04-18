package world.item;

import database.DatabaseManager;
import world.entity.EntityTable;
import world.room.RoomTable;
import java.util.*;

/**
 * Table definition for a SQL table that holds all the specific instances of containers.
 * @author Logan Earl
 */
public class ContainerInstanceTable implements DatabaseManager.DatabaseTable {
    /**The name of the table storing container instances*/
    public static final String TABLE_NAME = "containerInstance";

    /**The identifier of this container instance*/
    public static final String CONTAINER_ID = "containerID";
    /**The name of the container. References the ContainerStateTable, and is used to get container state*/
    public static final String CONTAINER_NAME = ContainerStatTable.CONTAINER_NAME; //foreign key
    /**
     * The identifier of the entity that is holding this container. This container's position is set to the same as the player's position. If there is
     * a value in this field, the value of stored under ROOM_NAME should be ignored
     */
    public static final String ENTITY_ID = EntityTable.ENTITY_ID; //foreign key
    /**The name of the room this container is sitting in. References the RoomTable*/
    public static final String ROOM_NAME = RoomTable.ROOM_NAME; //foreign key
    /**The current state of this container. Must store one of the STATE_* constants defined in this class;*/
    public static final String CONTAINER_STATE = "state";
    /**The code of this container's lock. Null if not used. When trying to unlock the container, the lock number of the key is compared with this field.*/
    public static final String LOCK_NUMBER = "lockNumber";

    /**Value stored in the {@value CONTAINER_STATE} field, means the container is locked*/
    public static final String STATE_LOCKED = "locked";
    /**Value stored in the {@value CONTAINER_STATE} field, means the container is unlocked*/
    public static final String STATE_UNLOCKED = "unlocked";

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final List<String> CONSTRAINTS = new ArrayList<>();

    public ContainerInstanceTable(){
        TABLE_DEFINITION.put(CONTAINER_ID, "INT PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(CONTAINER_NAME, "VARCHAR(32) NOT NULL");
        TABLE_DEFINITION.put(ENTITY_ID,"VARCHAR(32)");
        TABLE_DEFINITION.put(ROOM_NAME,"VARCHAR(32)");
        TABLE_DEFINITION.put(CONTAINER_STATE, "VARCHAR(16)");
        TABLE_DEFINITION.put(LOCK_NUMBER,"INT");

        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                CONTAINER_NAME, ContainerStatTable.TABLE_NAME, ContainerStatTable.CONTAINER_NAME));
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ENTITY_ID, EntityTable.TABLE_NAME, EntityTable.ENTITY_ID));
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
    public List getConstraints() {
        return CONSTRAINTS;
    }
}
