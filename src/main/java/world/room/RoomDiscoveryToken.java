package main.java.world.room;

import main.java.database.DatabaseManager;
import main.java.world.cache.WorldSpecificCache;
import main.java.world.entity.Entity;

import javax.xml.crypto.Data;
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

    public enum DetectionStatus {
        /**
         * Player has not seen it and has not had the opportunity to detect it
         */
        unencountered,
        /**
         * Player had the chance to detect the connection and failed the check
         */
        undetected,
        /**
         * Player detected the room, or traveled an unhidden connection
         */
        known
    }

    private static final String GET_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=? AND %s=?", TABLE_NAME, ENTITY_ID, CONNECTION_ID);
    private static final String UPDATE_SQL = String.format(Locale.US, "REPLACE INTO %s(%s, %s, %s, %s) VALUES (?, ?, ?, ?)", TABLE_NAME, ENTITY_ID, CONNECTION_ID, LAST_UPDATE, DETECTION_STATUS);
    private static final String DELETE_SQL = String.format(Locale.US, "DELETE FROM %s WHERE %s=? AND %s=?", TABLE_NAME, ENTITY_ID, CONNECTION_ID);

    private static Map<String, WorldSpecificCache<String, RoomDiscoveryToken>> roomTokenCache = new HashMap<>();

    public RoomDiscoveryToken(long lastUpdate, String entityID, String connectionID, DetectionStatus detectedStatus, String databaseName) {
        this.lastUpdate = lastUpdate;
        this.entityID = entityID;
        this.connectionID = connectionID;
        this.detectedStatus = detectedStatus;
        this.databaseName = databaseName;
    }

    private RoomDiscoveryToken(ResultSet readEntry, String databaseName) throws SQLException {
        this.databaseName = databaseName;
        lastUpdate = readEntry.getLong(LAST_UPDATE);
        entityID = readEntry.getString(ENTITY_ID);
        connectionID = readEntry.getString(CONNECTION_ID);
        String rawDetectionStatus = readEntry.getString(DETECTION_STATUS);
        try {
            if (rawDetectionStatus != null)
                detectedStatus = DetectionStatus.valueOf(rawDetectionStatus);
        } catch (IllegalArgumentException e) {
            System.out.println("Illegal detection status detected:" + rawDetectionStatus);
            rawDetectionStatus = null;
        }
        if (rawDetectionStatus == null) detectedStatus = DetectionStatus.unencountered;
    }

    public static void revealConnection(String entityID, String connectionID, long time, String databaseName){
        new RoomDiscoveryToken(time,entityID,connectionID,DetectionStatus.known,databaseName).saveToDatabase(databaseName);
    }

    public static void hideConnection(String entityID, String connectionID, long time, String databaseName){
        new RoomDiscoveryToken(time,entityID,connectionID,DetectionStatus.undetected,databaseName).saveToDatabase(databaseName);
    }

    public void hide(long time){
        detectedStatus = DetectionStatus.undetected;
        lastUpdate = time;
        updateInDatabase(databaseName);
    }

    public static RoomDiscoveryToken getToken(String entityID, String connectionID, String databaseName) {
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
        if (c != null) {
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

        if(toReturn == null)
            toReturn = new RoomDiscoveryToken(0,entityID,connectionID,DetectionStatus.unencountered,databaseName);
        addToCache(toReturn);
        return toReturn;
    }

    @Override
    public boolean saveToDatabase(String databaseName) {
        int result = DatabaseManager.executeStatement(UPDATE_SQL, databaseName,
                entityID, connectionID, lastUpdate, detectedStatus.name());
        if (result > 0) addToCache(this);
        return result > 0;
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        int result = DatabaseManager.executeStatement(DELETE_SQL, databaseName, entityID, connectionID);
        if (result >= 0) removeFromCache(this);
        return result >= 0;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        return saveToDatabase(databaseName);
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return true; //it is assumed the entry exists, even if it does not. This is because there is always a token available for any request
    }

    private static String getCacheTag(String entityID, String connectionID) {
        return entityID + "<!DIVIDER!>" + connectionID;
    }

    private static void addToCache(RoomDiscoveryToken connection) {
        WorldSpecificCache<String, RoomDiscoveryToken> worldCache = roomTokenCache.get(connection.databaseName);
        if (worldCache == null)
            roomTokenCache.put(connection.databaseName, worldCache = new WorldSpecificCache<>(connection.databaseName));
        worldCache.putValue(getCacheTag(connection.entityID, connection.connectionID), connection);
    }

    private static void removeFromCache(RoomDiscoveryToken token) {
        WorldSpecificCache<String, RoomDiscoveryToken> worldCache;
        if ((worldCache = roomTokenCache.get(token.databaseName)) != null)
            worldCache.remove(getCacheTag(token.entityID, token.connectionID));
    }

    public void update(DetectionStatus status, long time){
        detectedStatus = status;
        lastUpdate = time;
        updateInDatabase(databaseName);
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public String getEntityID() {
        return entityID;
    }

    public String getConnectionID() {
        return connectionID;
    }

    public DetectionStatus getDetectedStatus() {
        return detectedStatus;
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
