package world.room;

import database.DatabaseManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds information relating to the creation of a SQL table that holds each room that has been created.
 * @author Logan Earl
 */
public class RoomTable implements DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "room";

    /**The name of the room*/
    public static final String ROOM_NAME = "roomName";
    /**The extended description of the room*/
    public static final String ROOM_DESCRIPTION = "roomDescription";
    /**The visibility of the room. If not visible, will not show up as a traversable direction to nearby rooms. Visibility is determined like so<br>
     * 0 = visible<br>
     * >0 = invisible, difficulty to detect hidden room<br>
     * -1 = invisible, cannot be discovered with skill*/
    public static final String ROOM_VISIBILITY = "roomVisibility";

    public static final String ROOM_UP_NAME = "roomUpName";
    public static final String ROOM_DOWN_NAME = "roomDownName";
    public static final String ROOM_NORTH_NAME = "roomNorthName";
    public static final String ROOM_SOUTH_NAME = "roomSouthName";
    public static final String ROOM_EAST_NAME = "roomEastName";
    public static final String ROOM_WEST_NAME = "roomWestName";

    public final Map<String, String> TABLE_DEFINITION = new HashMap<>();

    public RoomTable(){
        TABLE_DEFINITION.put(ROOM_NAME,"VARCHAR(40) PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(ROOM_DESCRIPTION,"TEXT");
        TABLE_DEFINITION.put(ROOM_VISIBILITY,"INT NOT NULL");
        TABLE_DEFINITION.put(ROOM_UP_NAME,"TEXT");
        TABLE_DEFINITION.put(ROOM_DOWN_NAME,"TEXT");
        TABLE_DEFINITION.put(ROOM_NORTH_NAME,"TEXT");
        TABLE_DEFINITION.put(ROOM_SOUTH_NAME,"TEXT");
        TABLE_DEFINITION.put(ROOM_EAST_NAME,"TEXT");
        TABLE_DEFINITION.put(ROOM_WEST_NAME,"TEXT");
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
