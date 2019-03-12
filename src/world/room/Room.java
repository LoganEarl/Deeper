package world.room;

import database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import static world.room.RoomTable.*;

/**
 * This class is used to persist and store information on a single game room.
 * @author Logan Earl
 */
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
            TABLE_NAME, ROOM_NAME, ROOM_DESCRIPTION, ROOM_VISIBILITY, ROOM_UP_NAME, ROOM_DOWN_NAME, ROOM_NORTH_NAME, ROOM_SOUTH_NAME, ROOM_EAST_NAME, ROOM_WEST_NAME);
    private static final String DELETE_SQL = String.format(Locale.US,"DELETE FROM %s WHERE %s=?", TABLE_NAME,ROOM_NAME);
    private static final String UPDATE_SQL = String.format(Locale.US,"UPDATE %s SET %s=? %s=? %s=? %s=? %s=? %s=? %s=? %s=? WHERE %s=?",
            TABLE_NAME, ROOM_DESCRIPTION, ROOM_VISIBILITY, ROOM_UP_NAME, ROOM_DOWN_NAME, ROOM_NORTH_NAME, ROOM_SOUTH_NAME, ROOM_EAST_NAME, ROOM_WEST_NAME, ROOM_NAME);

    private Room(ResultSet readEntry) throws SQLException{
        roomName = readEntry.getString(ROOM_NAME);
        roomDescription = readEntry.getString(ROOM_DESCRIPTION);

        upRoomID = readEntry.getString(ROOM_UP_NAME);
        downRoomID = readEntry.getString(ROOM_DOWN_NAME);
        northRoomID = readEntry.getString(ROOM_NORTH_NAME);
        southRoomID = readEntry.getString(ROOM_SOUTH_NAME);
        eastRoomID = readEntry.getString(ROOM_EAST_NAME);
        westRoomID = readEntry.getString(ROOM_WEST_NAME);

        visibilityCode = readEntry.getInt(ROOM_VISIBILITY);
    }

    /**
     * How rooms are instantiated initially. Creates one from the given database
     * @param roomName the name of the room to look up
     * @param databaseName the name of the database file to search for the room
     * @return the room if it was found. null otherwise
     */
    public static Room getRoomByRoomName(String roomName, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL = null;
        Room toReturn;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1,roomName);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next())
                    toReturn = new Room(accountSet);
                else
                    toReturn = null;
                getSQL.close();
                c.close();
            }catch (SQLException e){
                toReturn = null;
            }
        }
        return toReturn;
    }

    @Override
    public boolean saveToDatabase(String databaseName) {
        Room room = getRoomByRoomName(roomName,databaseName);
        if(room == null){
            return DatabaseManager.executeStatement(CREATE_SQL,databaseName,
                    roomName,roomDescription,visibilityCode,upRoomID,downRoomID,northRoomID,southRoomID,eastRoomID,westRoomID) > 0;
        }else{
            return updateInDatabase(databaseName);
        }
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        return DatabaseManager.executeStatement(DELETE_SQL,databaseName, roomName) > 0;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        return DatabaseManager.executeStatement(UPDATE_SQL,databaseName,
                roomDescription,visibilityCode,upRoomID,downRoomID,northRoomID,southRoomID,eastRoomID,westRoomID) > 0;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return getRoomByRoomName(roomName,databaseName) != null;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public String getUpRoomID() {
        return upRoomID;
    }

    public String getDownRoomID() {
        return downRoomID;
    }

    public String getNorthRoomID() {
        return northRoomID;
    }

    public String getSouthRoomID() {
        return southRoomID;
    }

    public String getEastRoomID() {
        return eastRoomID;
    }

    public String getWestRoomID() {
        return westRoomID;
    }

    /**
     * gets the room's visibility status.
     * @return Will be 0 if visible like normal. Will be -1 if invisible with no hope of discovering it with a skill.
     * if >0 the number represents the difficulty of discovering the room with a skill. Recommended for hidden loot and the like
     */
    public int getVisibilityCode() {
        return visibilityCode;
    }
}
