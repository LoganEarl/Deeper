package world.meta;

import database.DatabaseManager;
import utils.FileUtils;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

import static world.meta.WorldTable.*;

/**
 * An instantiation of a world. Each world is stored as a database file in the following format. [world id].db where the world id is a
 * parsable integer. The world id must be unique. World templates on the other hand are file that are stored in the format [world name].db.
 * When creating a new world, the world template file is copied and renamed to have the world instantiation format. This is the file
 * that players will be interacting with, not the template files
 * @author Logan Earl
 */
public class World implements DatabaseManager.DatabaseEntry {
    public static final String META_DATABASE_NAME = "meta.db";

    private static final String DROP_META_TABLE_SQL = String.format(Locale.US, "DROP TABLE %s", WorldMetaTable.TABLE_NAME);
    private static final String GET_FROM_TEMPLATE = String.format(Locale.US, "SELECT * FROM %s", WorldMetaTable.TABLE_NAME);
    private static final String GET_ENTITY_SQL = String.format(Locale.US, "SELECT * FROM %s INNER JOIN %s WHERE %s=?", EntityWorldTable.TABLE_NAME, TABLE_NAME, EntityWorldTable.ENTITY_ID);
    private static final String GET_ALL_SQL = String.format(Locale.US, "SELECT * FROM %s", TABLE_NAME);
    private static final String GET_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE %s=?",TABLE_NAME, WORLD_ID);
    private static final String CREATE_SQL = String.format(Locale.US,"INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            TABLE_NAME,WORLD_ID,WORLD_NAME, WORLD_STATUS,WORLD_START_TIME,WORLD_END_TIME, ENTRY_PORTAL_ROOM_NAME, EXIT_PORTAL_ROOM_NAME, ESTIMATED_DIFFICULTY, PORTAL_SIZE, PREFERRED_DURATION_MINUTES);
    private static final String DELETE_SQL = String.format(Locale.US,"DELETE FROM %s WHERE %s=?", TABLE_NAME,WORLD_ID);
    private static final String UPDATE_SQL = String.format(Locale.US,"UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?  WHERE %s=?",
            TABLE_NAME, WORLD_NAME, WORLD_STATUS,WORLD_START_TIME,WORLD_END_TIME, ENTRY_PORTAL_ROOM_NAME, EXIT_PORTAL_ROOM_NAME, ESTIMATED_DIFFICULTY, PORTAL_SIZE, PREFERRED_DURATION_MINUTES, WORLD_ID);

    private int worldID;
    private String name;
    private String status;
    private long endTime;
    private long startTime;
    private String entryRoomName;
    private String exitRoomName;
    private int estimatedDifficulty;
    private int portalSize;
    private int durationMinutes;

    public static final int MINIMUM_SIM_TIME_MINUTES = 30;

    /**Value of getStatus() when the world is newly created and has not yet started*/
    public static final String STATUS_NEW = "new";
    /**Value of getStatus() when the world is created and actively being solved by players*/
    public static final String STATUS_IN_PROGRESS = "inProgress";
    /**Value of getStatus() when the world timed out without getting solved and was destroyed. World file has been deleted*/
    public static final String STATUS_FAILED = "failed";
    /**Value of getStatus() when the world has been completed but there are still players active in the sim*/
    public static final String FINISHING = "finishing";
    /**Value of getStatus() when the world was completed and all players have left. World file has been deleted*/
    public static final String STATUS_COMPLETE = "complete";

    private static boolean nullOrEmpty(String s){
        return s == null || s.isEmpty();
    }

    private World(int worldID, ResultSet templateMetaTableEntry) throws Exception{
        this.worldID = worldID;
        this.name = templateMetaTableEntry.getString(WorldMetaTable.WORLD_NAME);
        this.status = STATUS_NEW;
        this.startTime = 0;
        this.endTime = 0;
        this.entryRoomName = templateMetaTableEntry.getString(WorldMetaTable.ENTRY_PORTAL_ROOM_NAME);
        this.exitRoomName = templateMetaTableEntry.getString(WorldMetaTable.EXIT_PORTAL_ROOM_NAME);
        this.portalSize = templateMetaTableEntry.getInt(WorldMetaTable.PORTAL_SIZE);
        this.durationMinutes =  templateMetaTableEntry.getInt(WorldMetaTable.PREFERRED_DURATION_MINUTES);
        if(durationMinutes < MINIMUM_SIM_TIME_MINUTES)
            durationMinutes = MINIMUM_SIM_TIME_MINUTES;

        if(nullOrEmpty(name) || nullOrEmpty(entryRoomName) || nullOrEmpty(exitRoomName))
            throw new IllegalArgumentException("Bad template config");

        if(!saveToDatabase(""))
            throw new IllegalStateException("Unable to create world");
    }

    private World(ResultSet readEntry) throws SQLException {
        worldID = readEntry.getInt(WORLD_ID);
        name = readEntry.getString(WORLD_NAME);
        status = readEntry.getString(WORLD_STATUS);
        endTime = readEntry.getLong(WORLD_END_TIME);
        startTime = readEntry.getLong(WORLD_START_TIME);
        entryRoomName = readEntry.getString(ENTRY_PORTAL_ROOM_NAME);
        exitRoomName = readEntry.getString(EXIT_PORTAL_ROOM_NAME);
        estimatedDifficulty = readEntry.getInt(ESTIMATED_DIFFICULTY);
        portalSize = readEntry.getInt(PORTAL_SIZE);
        durationMinutes = readEntry.getInt(PREFERRED_DURATION_MINUTES);

        if(name == null || name.isEmpty() || status == null || status.isEmpty())
            throw new IllegalArgumentException("Illegal world: " + worldID + ", unable to parse");
    }

    public static World createWorldFromTemplate(String templateName){
        File templateFile = new File(DatabaseManager.TEMPLATE_DIRECTORY + templateName + ".db");
        if(!templateFile.exists())
            return null;
        int newWorldID = Math.abs(String.valueOf(System.currentTimeMillis()).hashCode());
        while(getWorldByWorldID(newWorldID) != null){
            newWorldID = Math.abs(String.valueOf(newWorldID).hashCode());
        }
        try {
            FileUtils.copyFile(templateFile, new File(DatabaseManager.DATA_DIRECTORY + newWorldID + ".db"));
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

        Connection c = DatabaseManager.getDatabaseConnection(newWorldID + ".db");
        PreparedStatement getSQL;
        Statement deleteSQL;
        World newWorld;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_FROM_TEMPLATE);
                ResultSet metaSet = getSQL.executeQuery();
                if(metaSet.next()) {
                    newWorld = new World(newWorldID, metaSet);
                }else
                    newWorld = null;
                getSQL.close();


            if(newWorld != null){
                deleteSQL = c.createStatement();
                deleteSQL.executeUpdate(DROP_META_TABLE_SQL);
            }
            }catch (Exception e){
                newWorld = null;
            }
        }

        return newWorld;
    }

    /**
     * gets an instantiated world by it's unique ID. Will retrieve it from the meta table
     * @param worldID the id of the world to get.
     * @return the instantiated world or null if it was not found
     */
    public static World getWorldByWorldID(int worldID){
        return getWorldByDatabaseName(worldID + ".db");
    }

    /**
     * gets an instantiated world by the name of the database file it is stored in.
     * @param databaseName the name of the database file. The only file extension supported is .db
     * @return the instantiated world or null if it could not be found
     */
    public static World getWorldByDatabaseName(String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(META_DATABASE_NAME);
        PreparedStatement getSQL = null;
        World toReturn;

        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1,databaseName.replace(".db",""));
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next())
                    toReturn = new World(accountSet);
                else
                    toReturn = null;
                getSQL.close();
                c.close();
            }catch (Exception e){
                toReturn = null;
            }
        }
        return toReturn;
    }

    /**
     * gets all worlds saved to the meta table
     * @return a collection of all the worlds saved, regardless of current activation. An empty list if there are no worlds
     */
    public static Collection<World> getAllWorlds(){
        Connection c = DatabaseManager.getDatabaseConnection(META_DATABASE_NAME);
        PreparedStatement getSQL = null;
        Collection<World> toReturn = new LinkedList<>();

        if(c != null){
            try {
                getSQL = c.prepareStatement(GET_ALL_SQL);
                ResultSet accountSet = getSQL.executeQuery();
                while(accountSet.next()) {
                    toReturn.add(new World(accountSet));
                }
                getSQL.close();
                c.close();
            }catch (Exception e){
                toReturn.clear();
            }
        }
        return toReturn;
    }

    /**
     * gets the world of the given entity. The entity must be an entity that has been associated with a player's account in order for it
     * to make it into the meta table to begin with.
     * @param entityID the id of a PC, NPCs are not stored in the meta table
     * @return the world the player is in or null if it could not be found
     */
    public static World getWorldOfEntityID(String entityID){
        Connection c = DatabaseManager.getDatabaseConnection(META_DATABASE_NAME);
        PreparedStatement getSQL;
        World toReturn;

        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1,entityID);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next())
                    toReturn = new World(accountSet);
                else
                    toReturn = null;
                getSQL.close();
                c.close();
            }catch (Exception e){
                toReturn = null;
            }
        }
        return toReturn;
    }

    /**
     * saves the world to the meta database.
     * @param ignored not used in the current implementation
     * @return true if it was saved successfully. False otherwise
     */
    @Override
    public boolean saveToDatabase(String ignored) {
        World w = getWorldByWorldID(worldID);
        if(w == null){
            return DatabaseManager.executeStatement(CREATE_SQL,META_DATABASE_NAME,
                    worldID, name, status, startTime, endTime,entryRoomName, exitRoomName, estimatedDifficulty, portalSize, durationMinutes) > 0;
        }else{
            return updateInDatabase("");
        }
    }

    /**
     * removes the world from the meta database.
     * @param ignored ignored in current implementation, as there is only one meta table
     * @return true if the entry was deleted
     */
    @Override
    public boolean removeFromDatabase(String ignored) {
        return DatabaseManager.executeStatement(DELETE_SQL,META_DATABASE_NAME, worldID) > 0;
    }

    /**
     * updates the world in the meta database
     * @param ignored ignored in current implementation, as there is only one meta table
     * @return true if the entry was updated
     */
    @Override
    public boolean updateInDatabase(String ignored) {
        return DatabaseManager.executeStatement(UPDATE_SQL,META_DATABASE_NAME,
                name,status,startTime,endTime,entryRoomName, exitRoomName, estimatedDifficulty, portalSize, durationMinutes) > 0;
    }

    /**
     * Determine if this world already exists in the meta database
     * @param ignored ignored in current implementation, as there is only one meta table
     * @return true if the world exists in the meta database
     */
    @Override
    public boolean existsInDatabase(String ignored) {
        return getWorldByWorldID(worldID) != null;
    }
}
