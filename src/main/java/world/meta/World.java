package main.java.world.meta;

import main.java.database.DatabaseManager;
import main.java.database.EnumTable;
import main.java.utils.FileUtils;
import main.java.world.entity.Entity;
import main.java.world.entity.EntityTable;
import main.java.world.entity.race.Race;
import main.java.world.entity.race.RaceTable;
import main.java.world.entity.skill.SkillTable;
import main.java.world.item.DamageType;
import main.java.world.item.ItemInstanceTable;
import main.java.world.item.ItemStatTable;
import main.java.world.item.ItemType;
import main.java.world.item.armor.Armor;
import main.java.world.item.armor.ArmorSlot;
import main.java.world.item.armor.ArmorStatTable;
import main.java.world.item.armor.ArmorType;
import main.java.world.item.consumable.ConsumableStatTable;
import main.java.world.item.container.ContainerStatTable;
import main.java.world.item.weapon.WeaponStatTable;
import main.java.world.room.*;
import main.java.world.story.StoryArcTable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static main.java.world.meta.WorldTable.*;

/**
 * An instantiation of a main.java.world. Each main.java.world is stored as a database file in the following format. [main.java.world id].db where the main.java.world id is a
 * parsable integer. The main.java.world id must be unique. World templates on the other hand are file that are stored in the format [main.java.world name].db.
 * When creating a new main.java.world, the main.java.world template file is copied and renamed to have the main.java.world instantiation format. This is the file
 * that players will be interacting with, not the template files
 *
 * @author Logan Earl
 */
public class World implements DatabaseManager.DatabaseEntry {
    public static final String META_DATABASE_NAME = "meta.db";

    public static final int HUB_WORLD_ID = 0;
    public static final String HUB_TEMPLATE_NAME = "hubTemplate";
    public static final int LIMBO_WORLD_ID = 1;
    public static final String LIMBO_TEMPLATE_NAME = "limboTemplate";

    private static World hubWorld;
    private static World limbo;

    private static final String DROP_META_TABLE_SQL = String.format(Locale.US, "DROP TABLE %s", WorldMetaTable.TABLE_NAME);
    private static final String GET_FROM_TEMPLATE = String.format(Locale.US, "SELECT * FROM %s", WorldMetaTable.TABLE_NAME);
    private static final String GET_ENTITY_SQL = String.format(Locale.US, "SELECT * FROM %s INNER JOIN %s WHERE %s=?", EntityWorldTable.TABLE_NAME, TABLE_NAME, EntityWorldTable.ENTITY_ID);
    private static final String SET_ENTITY_WORLD = String.format(Locale.US, "REPLACE INTO %s(%s, %s) VALUES (?, ?)", EntityWorldTable.TABLE_NAME, EntityWorldTable.ENTITY_ID, EntityWorldTable.WORLD_ID);
    private static final String GET_ALL_SQL = String.format(Locale.US, "SELECT * FROM %s", TABLE_NAME);
    private static final String GET_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?", TABLE_NAME, WORLD_ID);
    private static final String CREATE_SQL = String.format(Locale.US, "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                                           TABLE_NAME, WORLD_ID, WORLD_NAME, WORLD_STATUS, WORLD_START_TIME, WORLD_END_TIME, ENTRY_PORTAL_ROOM_NAME, EXIT_PORTAL_ROOM_NAME, ESTIMATED_DIFFICULTY, PORTAL_SIZE, PREFERRED_DURATION_MINUTES);
    private static final String DELETE_SQL = String.format(Locale.US, "DELETE FROM %s WHERE %s=?", TABLE_NAME, WORLD_ID);
    private static final String UPDATE_SQL = String.format(Locale.US, "UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?  WHERE %s=?",
                                                           TABLE_NAME, WORLD_NAME, WORLD_STATUS, WORLD_START_TIME, WORLD_END_TIME, ENTRY_PORTAL_ROOM_NAME, EXIT_PORTAL_ROOM_NAME, ESTIMATED_DIFFICULTY, PORTAL_SIZE, PREFERRED_DURATION_MINUTES, WORLD_ID);
    private static final String DELETE_ENTITY_SQL = String.format(Locale.US, "DELETE FROM %s WHERE %s=?",
                                                                  EntityWorldTable.TABLE_NAME, EntityWorldTable.ENTITY_ID);

    private int worldID;
    private String name;
    private Status status;
    private long endTime;
    private long startTime;
    private String entryRoomName;
    private String exitRoomName;
    private int estimatedDifficulty;
    private int portalSize;
    private int durationMinutes;

    public static final int MINIMUM_SIM_TIME_MINUTES = 30;

    //TODO set up a table for this with foreign keys
    public enum Status {
        /**
         * Value of getStatus() when the main.java.world is newly created and has not yet started
         */
        fresh,
        /**
         * Value of getStatus() when the main.java.world is created and actively being solved by players
         */
        inProgress,
        /**
         * Value of getStatus() when the main.java.world timed out without getting solved and was destroyed. World file has been deleted
         */
        failed,
        /**
         * Value of getStatus() when the main.java.world has been completed but there are still players active in the sim
         */
        finishing,
        /**
         * Value of getStatus() when the main.java.world was completed and all players have left. World file has been deleted
         */
        complete
    }

    private static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private World(int worldID, ResultSet templateMetaTableEntry) throws Exception {
        this.worldID = worldID;
        this.name = templateMetaTableEntry.getString(WorldMetaTable.WORLD_NAME);
        this.status = Status.fresh;
        this.startTime = 0;
        this.endTime = 0;
        this.entryRoomName = templateMetaTableEntry.getString(WorldMetaTable.ENTRY_PORTAL_ROOM_NAME);
        this.exitRoomName = templateMetaTableEntry.getString(WorldMetaTable.EXIT_PORTAL_ROOM_NAME);
        this.portalSize = templateMetaTableEntry.getInt(WorldMetaTable.PORTAL_SIZE);
        this.durationMinutes = templateMetaTableEntry.getInt(WorldMetaTable.PREFERRED_DURATION_MINUTES);
        if (durationMinutes < MINIMUM_SIM_TIME_MINUTES)
            durationMinutes = MINIMUM_SIM_TIME_MINUTES;

        if (nullOrEmpty(name) || nullOrEmpty(entryRoomName) || nullOrEmpty(exitRoomName))
            throw new IllegalArgumentException("Bad template config");

        if (!saveToDatabase(""))
            throw new IllegalStateException("Unable to create main.java.world");
    }

    private World(ResultSet readEntry) throws SQLException {
        worldID = readEntry.getInt(WORLD_ID);
        name = readEntry.getString(WORLD_NAME);
        try {
            status = Status.valueOf(readEntry.getString(WORLD_STATUS));
        } catch (IllegalArgumentException e) {
            status = Status.fresh;
        }
        endTime = readEntry.getLong(WORLD_END_TIME);
        startTime = readEntry.getLong(WORLD_START_TIME);
        entryRoomName = readEntry.getString(ENTRY_PORTAL_ROOM_NAME);
        exitRoomName = readEntry.getString(EXIT_PORTAL_ROOM_NAME);
        estimatedDifficulty = readEntry.getInt(ESTIMATED_DIFFICULTY);
        portalSize = readEntry.getInt(PORTAL_SIZE);
        durationMinutes = readEntry.getInt(PREFERRED_DURATION_MINUTES);

        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Illegal main.java.world: " + worldID + ", unable to parse");
    }

    public static void initWorldSystem() {
        List<EnumTable<?>> enumTables = getEnumTables();
        List<DatabaseManager.DatabaseTable> tables = getTables();
        tables.add(new WorldMetaTable());
        tables.addAll(enumTables);

        DatabaseManager.createNewTemplate(HUB_TEMPLATE_NAME + ".db");
        DatabaseManager.createTemplateTables(HUB_TEMPLATE_NAME + ".db", tables);

        DatabaseManager.createNewTemplate(LIMBO_TEMPLATE_NAME + ".db");
        DatabaseManager.createTemplateTables(LIMBO_TEMPLATE_NAME + ".db", tables);

        List<DatabaseManager.DatabaseTable> metaTables = new LinkedList<>();
        metaTables.add(new WorldTable());
        metaTables.add(new EntityWorldTable());
        DatabaseManager.createWorldTables(META_DATABASE_NAME, metaTables);

        createWorldWithID(HUB_WORLD_ID, HUB_TEMPLATE_NAME);
        createWorldWithID(LIMBO_WORLD_ID, LIMBO_TEMPLATE_NAME);

        hubWorld = getWorldByWorldID(HUB_WORLD_ID);
        Race.writePlayableRacesToDatabaseFile(hubWorld.getDatabaseName());

        limbo = getWorldByWorldID(LIMBO_WORLD_ID);
        Race.writePlayableRacesToDatabaseFile(limbo.getDatabaseName());

        writeConstantsToTables(enumTables,
                               "template/" + HUB_TEMPLATE_NAME + ".db",
                               "template/" + LIMBO_TEMPLATE_NAME + ".db",
                               hubWorld.getDatabaseName(),
                               limbo.getDatabaseName());

        for (World w : getAllWorlds()) {
            DatabaseManager.createWorldTables(w.getDatabaseName(), tables);
            Race.writePlayableRacesToDatabaseFile(w.getDatabaseName());
            writeConstantsToTables(enumTables, w.getDatabaseName());
        }
    }

    /**
     * creates a new main.java.world instance using the template of the given name. The given template must exists already
     *
     * @param templateName the name of the main.java.world template file without the file extension. Example, "testWorld"
     * @return the newly created main.java.world or null if it failed
     */
    public static World createWorldFromTemplate(String templateName) {
        File templateFile = new File(DatabaseManager.TEMPLATE_DIRECTORY + templateName + ".db");
        if (!templateFile.exists())
            return null;
        int newWorldID = Math.abs(String.valueOf(System.currentTimeMillis()).hashCode());
        while (getWorldByWorldID(newWorldID) != null) {
            newWorldID = Math.abs(String.valueOf(newWorldID).hashCode());
        }
        return createWorldWithID(newWorldID, templateName);
    }

    private static World createWorldWithID(int newWorldID, String templateName) {
        if (getWorldByWorldID(newWorldID) != null && new File(DatabaseManager.DATA_DIRECTORY + newWorldID + ".db").exists())
            return null;

        File templateFile = new File(DatabaseManager.TEMPLATE_DIRECTORY + templateName + ".db");
        if (!templateFile.exists())
            return null;
        try {
            FileUtils.copyFile(templateFile, new File(DatabaseManager.DATA_DIRECTORY + newWorldID + ".db"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Connection c = DatabaseManager.getDatabaseConnection(newWorldID + ".db");
        PreparedStatement getSQL;
        Statement deleteSQL;
        World newWorld;
        if (c == null)
            return null;
        else {
            try {
                getSQL = c.prepareStatement(GET_FROM_TEMPLATE);
                ResultSet metaSet = getSQL.executeQuery();
                if (metaSet.next()) {
                    newWorld = new World(newWorldID, metaSet);
                } else
                    newWorld = null;
                getSQL.close();


                if (newWorld != null) {
                    deleteSQL = c.createStatement();
                    deleteSQL.executeUpdate(DROP_META_TABLE_SQL);
                }
            } catch (Exception e) {
                newWorld = null;
            }
        }

        if (newWorld != null) {
            List<EnumTable<?>> enumTables = getEnumTables();
            List<DatabaseManager.DatabaseTable> tables = getTables();
            tables.addAll(enumTables);

            DatabaseManager.createWorldTables(newWorld.getWorldID() + ".db", tables);
            writeConstantsToTables(enumTables, newWorld.getWorldID() + ".db");
        }

        return newWorld;
    }

    private static void writeConstantsToTables(List<EnumTable<?>> enumTables, String... databaseFiles) {
        for (String databaseFile : databaseFiles)
            for (EnumTable<?> table : enumTables)
                table.writeConstantsToDatabaseFile(databaseFile);
    }

    private static List<EnumTable<?>> getEnumTables() {
        List<EnumTable<?>> enumTables = new LinkedList<>();
        enumTables.add(new EnumTable<>(ItemType.TABLE_NAME, ItemType.class));
        enumTables.add(new EnumTable<>(Domain.TABLE_NAME, Domain.class));
        enumTables.add(new EnumTable<>(DamageType.TABLE_NAME, DamageType.class));
        enumTables.add(new EnumTable<>(ArmorSlot.TABLE_NAME, ArmorSlot.class));
        enumTables.add(new EnumTable<>(ArmorType.TABLE_NAME, ArmorType.class));
        enumTables.add(new EnumTable<>(RoomConnection.State.TABLE_NAME, RoomConnection.State.class));
        enumTables.add(new EnumTable<>(RoomConnection.Direction.TABLE_NAME, RoomConnection.Direction.class));
        enumTables.add(new EnumTable<>(RoomDiscoveryToken.DetectionStatus.TABLE_NAME, RoomDiscoveryToken.DetectionStatus.class));
        //TODO rest of the enum tables
        //TODO make sure these are parsing through the new built in method

        return enumTables;
    }

    private static List<DatabaseManager.DatabaseTable> getTables() {
        List<DatabaseManager.DatabaseTable> tables = new ArrayList<>();
        tables.add(new ItemStatTable());
        tables.add(new ContainerStatTable());
        tables.add(new ArmorStatTable());
        tables.add(new ConsumableStatTable());
        //tables.add(new MiscItemStatTable());
        //tables.add(new AmmoStatTable());
        tables.add(new WeaponStatTable());
        tables.add(new SkillTable());
        tables.add(new RoomTable());
        tables.add(new RoomConnectionTable());
        tables.add(new RoomConnectionDiscoveryTable());
        tables.add(new RaceTable());
        tables.add(new EntityTable());
        tables.add(new StoryArcTable());
        tables.add(new ItemInstanceTable());
        return tables;
    }

    /**
     * gets an instantiated main.java.world by it's unique ID. Will retrieve it from the meta table
     *
     * @param worldID the id of the main.java.world to get.
     * @return the instantiated main.java.world or null if it was not found
     */
    public static World getWorldByWorldID(int worldID) {
        return getWorldByDatabaseName(worldID + ".db");
    }

    /**
     * gets an instantiated main.java.world by the name of the database file it is stored in.
     *
     * @param databaseName the name of the database file. The only file extension supported is .db
     * @return the instantiated main.java.world or null if it could not be found
     */
    public static World getWorldByDatabaseName(String databaseName) {
        Connection c = DatabaseManager.getDatabaseConnection(META_DATABASE_NAME);
        PreparedStatement getSQL = null;
        World toReturn;

        if (c == null)
            return null;
        else {
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1, databaseName.replace(".db", ""));
                ResultSet accountSet = getSQL.executeQuery();
                if (accountSet.next())
                    toReturn = new World(accountSet);
                else
                    toReturn = null;
                getSQL.close();
                //c.close();
            } catch (Exception e) {
                toReturn = null;
            }
        }
        return toReturn;
    }

    /**
     * use to registerThe Deep Dark an entity to the given main.java.world. Will not change the main.java.world the given entity thinks it is in. Not to be confused with Entity.transferToWorld() which is preferable in almost every instance
     *
     * @param e the entity to transfer
     * @param w the main.java.world to register the entity to
     * @return true if successful
     */
    public static boolean setWorldOfEntity(Entity e, World w) {
        return DatabaseManager.executeStatement(SET_ENTITY_WORLD, META_DATABASE_NAME, e.getID(), w.getWorldID()) > 0;
    }

    public static boolean deleteEntity(Entity e) {
        World w = getWorldOfEntityID(e.getID());
        if (w != null) {
            DatabaseManager.executeStatement(DELETE_ENTITY_SQL, META_DATABASE_NAME, e.getID());
            e.removeFromDatabase(w.getDatabaseName());
            return true;
        }
        return false;
    }

    /**
     * gets all worlds saved to the meta table
     *
     * @return a collection of all the worlds saved, regardless of current activation. An empty list if there are no worlds
     */
    public static Collection<World> getAllWorlds() {
        Connection c = DatabaseManager.getDatabaseConnection(META_DATABASE_NAME);
        PreparedStatement getSQL = null;
        Collection<World> toReturn = new LinkedList<>();

        if (c != null) {
            try {
                getSQL = c.prepareStatement(GET_ALL_SQL);
                ResultSet accountSet = getSQL.executeQuery();
                while (accountSet.next()) {
                    toReturn.add(new World(accountSet));
                }
                getSQL.close();
                //c.close();
            } catch (Exception e) {
                toReturn.clear();
            }
        }
        return toReturn;
    }

    /**
     * gets the main.java.world of the given entity. The entity must be an entity that has been associated with a player's account in order for it
     * to make it into the meta table to begin with.
     *
     * @param entityID the id of a PC, NPCs are not stored in the meta table
     * @return the main.java.world the player is in or null if it could not be found
     */
    public static World getWorldOfEntityID(String entityID) {
        Connection c = DatabaseManager.getDatabaseConnection(META_DATABASE_NAME);
        PreparedStatement getSQL;
        World toReturn;

        if (c == null)
            return null;
        else {
            try {
                getSQL = c.prepareStatement(GET_ENTITY_SQL);
                getSQL.setString(1, entityID);
                ResultSet accountSet = getSQL.executeQuery();
                if (accountSet.next())
                    toReturn = new World(accountSet);
                else
                    toReturn = null;
                getSQL.close();
                //c.close();
            } catch (Exception e) {
                toReturn = null;
            }
        }
        return toReturn;
    }

    public int getWorldID() {
        return worldID;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getEntryRoomName() {
        return entryRoomName;
    }

    public String getExitRoomName() {
        return exitRoomName;
    }

    public int getEstimatedDifficulty() {
        return estimatedDifficulty;
    }

    public int getPortalSize() {
        return portalSize;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getDatabaseName() {
        return worldID + ".db";
    }

    public void resetStatus() {
        this.status = Status.fresh;
        this.startTime = 0;
        this.endTime = 0;
        updateInDatabase("");
    }

    public void startWorld() {
        if (this.status != Status.fresh)
            throw new IllegalStateException("World was already started");
        this.status = Status.inProgress;
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime + (durationMinutes * 60 * 1000);
    }

    /**
     * saves the main.java.world to the meta database.
     *
     * @param ignored not used in the current implementation
     * @return true if it was saved successfully. False otherwise
     */
    @Override
    public boolean saveToDatabase(String ignored) {
        World w = getWorldByWorldID(worldID);
        if (w == null) {
            return DatabaseManager.executeStatement(CREATE_SQL, META_DATABASE_NAME,
                                                    worldID, name, status, startTime, endTime, entryRoomName, exitRoomName, estimatedDifficulty, portalSize, durationMinutes) > 0;
        } else {
            return updateInDatabase("");
        }
    }

    /**
     * removes the main.java.world from the meta database.
     *
     * @param ignored ignored in current implementation, as there is only one meta table
     * @return true if the entry was deleted
     */
    @Override
    public boolean removeFromDatabase(String ignored) {
        return DatabaseManager.executeStatement(DELETE_SQL, META_DATABASE_NAME, worldID) > 0;
    }

    /**
     * updates the main.java.world in the meta database
     *
     * @param ignored ignored in current implementation, as there is only one meta table
     * @return true if the entry was updated
     */
    @Override
    public boolean updateInDatabase(String ignored) {
        return DatabaseManager.executeStatement(UPDATE_SQL, META_DATABASE_NAME,
                                                name, status, startTime, endTime, entryRoomName, exitRoomName, estimatedDifficulty, portalSize, durationMinutes) > 0;
    }

    /**
     * Determine if this main.java.world already exists in the meta database
     *
     * @param ignored ignored in current implementation, as there is only one meta table
     * @return true if the main.java.world exists in the meta database
     */
    @Override
    public boolean existsInDatabase(String ignored) {
        return getWorldByWorldID(worldID) != null;
    }

    public static World getHubWorld() {
        return hubWorld;
    }

    public static World getLimboWorld() {
        return limbo;
    }
}
