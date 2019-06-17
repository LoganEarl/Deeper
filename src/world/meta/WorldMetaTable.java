package world.meta;

import database.DatabaseManager;
import world.room.RoomTable;

import java.util.*;

/**
 * Each world template needs a bit of extra info when instantiating the world. Templates have these tables, but they are removed from
 * the instantiated worlds during construction. The information stored in this table is stored in the Global meta database instead of
 * in the individual databases, so that the data can be accessed once the simulated world is over.
 */
public class WorldMetaTable implements DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "meta";

    public static final String WORLD_NAME = "worldName";
    public static final String PREFERRED_DURATION_MINUTES = "durationMinutes";
    public static final String ENTRY_PORTAL_ROOM_NAME = "entryPortalRoomName";
    public static final String EXIT_PORTAL_ROOM_NAME = "exitPortalRoomName";
    public static final String PORTAL_SIZE = "portalSize";
    public static final String ESTIMATED_DIFFICULTY = "difficulty";

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    private static final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    private static final Set<String> CONSTRAINTS = new HashSet<>();

    public WorldMetaTable(){
        TABLE_DEFINITION.put(WORLD_NAME, "VARCHAR(32) PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(PREFERRED_DURATION_MINUTES, "INT NOT NULL");
        TABLE_DEFINITION.put(ENTRY_PORTAL_ROOM_NAME, "VARCHAR(32) NOT NULL");
        TABLE_DEFINITION.put(EXIT_PORTAL_ROOM_NAME, "VARCHAR(32) NOT NULL");
        TABLE_DEFINITION.put(PORTAL_SIZE, "INT NOT NULL");
        TABLE_DEFINITION.put(ESTIMATED_DIFFICULTY, "INT");

        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ENTRY_PORTAL_ROOM_NAME, RoomTable.TABLE_NAME, RoomTable.ROOM_NAME));
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                EXIT_PORTAL_ROOM_NAME, RoomTable.TABLE_NAME, RoomTable.ROOM_NAME));
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
