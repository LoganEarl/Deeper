package main.java.world.room;

import main.java.database.DatabaseManager;
import main.java.world.cache.WorldSpecificCache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RoomDiscoveryToken implements DatabaseManager.DatabaseEntry {
    private long lastUpdate;
    private String entityName;
    private String connectionID;
    private boolean detectedStatus;

    private static Map<String, WorldSpecificCache<String, RoomDiscoveryToken>> roomTokenCache = new HashMap<>();

    public static RoomDiscoveryToken getToken(String entityID, String connectionID, String databaseName){

    }

    private RoomDiscoveryToken(ResultSet readEntry) throws SQLException {
        //TODO
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
