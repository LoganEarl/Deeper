package world.room;

import database.DatabaseManager;

import java.util.Locale;
import static world.room.RoomTable.*;

public class Room implements DatabaseManager.DatabaseEntry {
    private String roomName;
    private String roomDescription;

    private String upRoomID;
    private String downRoomID;
    private String northRoomID;
    private String southRoomID;
    private String eastRoomID;
    private String westRoomID;

    private int visibilityCode;

    private static final String GET_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE %s=?",TABLE_NAME,ROOM_NAME);
    private static final String CREATE_SQL = String.format(Locale.US,"INSERT INTO %s (%s %s %s %s %s %s %s %s %s) VALUES (? ? ? ? ? ? ? ?)",
            TABLE_NAME, ROOM_NAME, ROOM_DESCRIPTION, ROOM_VISIBILITY, ROOM_UP_ID, ROOM_DOWN_ID, ROOM_NORTH_ID, ROOM_SOUTH_ID, ROOM_EAST_ID, ROOM_WEST_ID);
    private static final String DELETE_SQL = String.format(Locale.US,"DELETE FROM %s WHERE %s=?", TABLE_NAME,ROOM_NAME);
    private static final String UPDATE_SQL = String.format(Locale.US,"UPDATE %s SET %s=? %s=? %s=? %s=? %s=? %s=? %s=? WHERE %s=?",
            TABLE_NAME, ROOM_DESCRIPTION, ROOM_VISIBILITY, ROOM_UP_ID, ROOM_DOWN_ID, ROOM_NORTH_ID, ROOM_SOUTH_ID, ROOM_EAST_ID, ROOM_WEST_ID, ROOM_NAME);

    public static Room getRoomByRoomName(String roomName, String databseName){
        return null;
    }

    @Override
    public boolean saveToDatabase(String databaseName) {
        return false;
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        return false;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        return false;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return false;
    }
}
