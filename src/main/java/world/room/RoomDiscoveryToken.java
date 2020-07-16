package main.java.world.room;

import main.java.database.DatabaseManager;
import main.java.world.cache.WorldSpecificCache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static main.java.world.room.RoomConnectionDiscoveryTable.*;

public class RoomDiscoveryToken implements DatabaseManager.DatabaseEntry {
    private long lastUpdate;
    private String entityID;
    private String connectionID;
    private DetectionStatus detectedStatus;
    private String databaseName;

    public enum DetectionStatus{
        /**Player has not seen it and has not had the opportunity to detect it*/
        unencountered,
        /**Player had the chance to detect the connection and failed the check*/
        undetected,
        /**Player detected the room, or traveled an unhidden connection*/
        known
    }

    private static final String GET_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=? AND %s=?", TABLE_NAME, ENTITY_ID, CONNECTION_ID);

    private static Map<String, WorldSpecificCache<String, RoomDiscoveryToken>> roomTokenCache = new HashMap<>();
    private static String getCacheTag(String entityID, String connectionID){
        return entityID + "<!DIVIDER!>" + connectionID;
    }

    public static RoomDiscoveryToken getToken(String entityID, String connectionID, String databaseName){
        RoomDiscoveryToken toReturn = null;
        String cacheTag = getCacheTag(entityID, connectionID);
        WorldSpecificCache<String, RoomDiscoveryToken> worldCache = roomTokenCache.get(databaseName);
        if (worldCache == null)
            roomTokenCache.put(databaseName, new WorldSpecificCache<>(databaseName));
        else
            toReturn = worldCache.getValue(cacheTag);
        if (toReturn != null) return toReturn;

        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        if (c == null)
            return null;
        else {
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1, entityID);
                getSQL.setString(2, connectionID);
                ResultSet resultSet = getSQL.executeQuery();
                if (resultSet.next())
                    toReturn = new RoomDiscoveryToken(resultSet, databaseName);
                else
                    toReturn = null;
                getSQL.close();
            } catch (SQLException e) {
                toReturn = null;
            }
        }
        if (toReturn != null) addToCache(toReturn);
        return toReturn;
    }

    private RoomDiscoveryToken(ResultSet readEntry, String databaseName) throws SQLException {
        this.databaseName = databaseName;
        lastUpdate = readEntry.getLong(LAST_UPDATE);
        entityID = readEntry.getString(ENTITY_ID);
        connectionID = readEntry.getString(CONNECTION_ID);
        String rawDetectionStatus = readEntry.getString(DETECTION_STATUS);
        try{
            if(rawDetectionStatus != null)
                detectedStatus = DetectionStatus.valueOf(rawDetectionStatus);
        }catch (IllegalArgumentException e){
            System.out.println("Illegal detection status detected:" + rawDetectionStatus);
            rawDetectionStatus = null;
        }
        if(rawDetectionStatus == null) detectedStatus = DetectionStatus.unencountered;
    }

    @Override
    public boolean saveToDatabase(String databaseName) {
        //TODO
        return false;
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        //TODO
        return false;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        //TODO
        return false;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        //TODO
        return false;
    }

    private static void addToCache(RoomDiscoveryToken connection) {
        WorldSpecificCache<String, RoomDiscoveryToken> worldCache = roomTokenCache.get(connection.databaseName);
        if (worldCache == null)
            roomTokenCache.put(connection.databaseName, worldCache = new WorldSpecificCache<>(connection.databaseName));
        worldCache.putValue(getCacheTag(connection.entityID, connection.connectionID), connection);
    }
}
