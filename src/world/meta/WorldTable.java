package world.meta;

import database.DatabaseManager;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorldTable implements DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "world";

    public static final String WORLD_ID = "worldID";
    public static final String WORLD_NAME = "worldName";
    public static final String WORLD_STATUS = "worldStatus";
    public static final String WORLD_END_TIME = "worldEndTime";
    public static final String WORLD_START_TIME = "worldStartTime";
    public static final String PREFERRED_DURATION_MINUTES = "durationMinutes";
    public static final String ENTRY_PORTAL_ROOM_NAME = "entryPortalRoomName";
    public static final String EXIT_PORTAL_ROOM_NAME = "exitPortalRoomName";
    public static final String PORTAL_SIZE = "portalSize";
    public static final String ESTIMATED_DIFFICULTY = "difficulty";

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    public final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    public WorldTable(){
        TABLE_DEFINITION.put(WORLD_ID, "INT PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(WORLD_NAME,"VARCHAR(32) NOT NULL");
        TABLE_DEFINITION.put(WORLD_STATUS,"VARCHAR(16) NOT NULL");
        TABLE_DEFINITION.put(WORLD_START_TIME,"BIGINT");
        TABLE_DEFINITION.put(WORLD_END_TIME, "BIGINT");
        TABLE_DEFINITION.put(PREFERRED_DURATION_MINUTES, "INT NOT NULL");
        TABLE_DEFINITION.put(ENTRY_PORTAL_ROOM_NAME, "VARCHAR(32) NOT NULL");
        TABLE_DEFINITION.put(EXIT_PORTAL_ROOM_NAME, "VARCHAR(32) NOT NULL");
        TABLE_DEFINITION.put(PORTAL_SIZE, "INT NOT NULL");
        TABLE_DEFINITION.put(ESTIMATED_DIFFICULTY, "INT");
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
        return Collections.emptyList();
    }
}
