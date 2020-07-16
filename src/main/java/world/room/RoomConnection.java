package main.java.world.room;

import main.java.database.DatabaseManager;
import main.java.world.cache.WorldSpecificCache;
import main.java.world.entity.Entity;
import main.java.world.entity.skill.Skill;
import main.java.world.trait.EffectArchetype;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static main.java.world.room.RoomConnectionTable.*;
import static main.java.world.room.RoomConnectionTable.TABLE_NAME;

public class RoomConnection implements DatabaseManager.DatabaseEntry, Comparable<RoomConnection> {
    private String connectionID;
    private String displayName;

    private String databaseName;

    //skill check info
    private String successMessage;
    private String failureMessage;
    private String sourceRoomName;
    private String destRoomName;
    private int traverseDifficulty;
    private Skill traverseSkill;
    private int detectDifficulty;
    private EffectArchetype failureEffect;
    private EffectArchetype successEffect;
    private String failureRoomName;
    private List<Domain> failureDestinationDomains;

    private List<Domain> detectDomains;
    private String detectWord;
    private int staminaCost;
    private int keyCode;
    private List<Domain> sourceDomains;
    private List<Domain> destinationDomains;
    private State state;
    private Direction direction;
    private Direction failureDirection;

    public enum State {
        locked, unlocked, impassible
    }

    public enum Direction {
        north, south, northeast, southwest, east, west, southeast, northwest, above(""), below("");

        private String transitionWord = "the ";

        Direction() {
        }

        Direction(String transitionWord) {
            this.transitionWord = transitionWord;
        }

        public static Direction oppositeOf(Direction direction) {
            Direction[] directions = Direction.values();
            for (int i = 0; i < directions.length; i++) {
                if (directions[i] == direction) {
                    return i % 2 == 0 ? directions[i + 1] : directions[i - 1];
                }
            }
            throw new IllegalArgumentException("Invalid direction given");
        }

        public Direction opposite() {
            return oppositeOf(this);
        }

        @Override
        public String toString() {
            return transitionWord + name();
        }
    }

    private static final Map<String, WorldSpecificCache<String, RoomConnection>> roomConnectionCache = new HashMap<>();

    //<editor-fold desc="SQL Operations">
    private static final String GET_SOURCE_SQL = String.format(Locale.US, "SELECT %s FROM %s WHERE %s=?", CONNECTION_ID, TABLE_NAME, SOURCE_ROOM_NAME);
    private static final String GET_ID_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?", TABLE_NAME, CONNECTION_ID);
    private static final String REPLACE_SQL = String.format(Locale.US,
            "REPLACE INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            TABLE_NAME, CONNECTION_ID, NAME, SUCCESS_MESSAGE, FAILURE_MESSAGE, SOURCE_ROOM_NAME, DEST_ROOM_NAME, SOURCE_DOMAINS, DESTINATION_DOMAINS, TRAVERSE_DIFFICULTY, TRAVERSE_SKILL_NAME, DETECT_DIFFICULTY, DETECT_DOMAINS, DETECT_WORD, FAILURE_EFFECT_NAME, FAILURE_ROOM_NAME, FAILURE_DESTINATION_DOMAINS, SUCCESS_EFFECT_NAME, STAMINA_COST, KEY_CODE, STATE, DIRECTION, FAILURE_DIRECTION);
    private static final String DELETE_SQL = String.format(Locale.US, "DELETE FROM %s WHERE %s=?", TABLE_NAME, CONNECTION_ID);


    private RoomConnection(ResultSet readEntry, String databaseName) throws SQLException {
        connectionID = readEntry.getString(CONNECTION_ID);
        displayName = readEntry.getString(NAME);
        successMessage = readEntry.getString(SUCCESS_MESSAGE);
        failureMessage = readEntry.getString(FAILURE_MESSAGE);
        sourceRoomName = readEntry.getString(SOURCE_ROOM_NAME);
        destRoomName = readEntry.getString(DEST_ROOM_NAME);
        traverseDifficulty = readEntry.getInt(TRAVERSE_DIFFICULTY);
        traverseSkill = Skill.getGeneralSkill(readEntry.getString(TRAVERSE_SKILL_NAME));
        detectDifficulty = readEntry.getInt(DETECT_DIFFICULTY);

        String raw = "";
        try {
            raw = readEntry.getString(FAILURE_EFFECT_NAME);
            failureEffect = raw != null ? EffectArchetype.valueOf(raw) : null;
        } catch (IllegalArgumentException e) {
            System.out.printf("Failed to load failure effect for room connection:%s:%s\n", connectionID, raw);
            failureEffect = null;
        }

        try {
            raw = readEntry.getString(SUCCESS_EFFECT_NAME);
            successEffect = raw != null ? EffectArchetype.valueOf(raw) : null;
        } catch (IllegalArgumentException e) {
            System.out.printf("Failed to load success effect for room connection:%s:%s\n", connectionID, raw);
            successEffect = null;
        }

        try {
            raw = readEntry.getString(STATE);
            state = raw != null ? State.valueOf(raw) : State.unlocked;
        } catch (IllegalArgumentException e) {
            System.out.printf("Failed to load state for room connection:%s:%s\n", connectionID, raw);
            state = State.unlocked;
        }

        try {
            direction = Direction.valueOf(readEntry.getString(DIRECTION));
        } catch (IllegalArgumentException e) {
            System.out.printf("Failed to load direction for connection:%s:%s\n", connectionID, raw);
            direction = Direction.north;
        }

        failureRoomName = readEntry.getString(FAILURE_ROOM_NAME);

        String rawFailureDirection = readEntry.getString(FAILURE_DIRECTION);
        try {
            if(rawFailureDirection != null)
                failureDirection = Direction.valueOf(rawFailureDirection);
        } catch (IllegalArgumentException e) {
            rawFailureDirection = null;
        }
        if(rawFailureDirection == null && failureRoomName != null){
            System.out.printf("Failed to load failure direction for connection:%s:%s\n", connectionID, raw);
            failureDirection = Direction.north;
        }

        failureDestinationDomains = Domain.decodeDomains(readEntry.getString(FAILURE_DESTINATION_DOMAINS));
        sourceDomains = Domain.decodeDomains(readEntry.getString(SOURCE_DOMAINS));
        destinationDomains = Domain.decodeDomains(readEntry.getString(DESTINATION_DOMAINS));
        detectDomains = Domain.decodeDomains(readEntry.getString(DETECT_DOMAINS));

        if (destinationDomains.isEmpty())
            destinationDomains.add(Domain.surface);

        detectWord = readEntry.getString(DETECT_WORD);
        staminaCost = readEntry.getInt(STAMINA_COST);
        keyCode = readEntry.getInt(KEY_CODE);

        this.databaseName = databaseName;
    }

    @Override
    public boolean saveToDatabase(String databaseName) {
        //Kinda wish i knew about hibernate when i started this.
        int result = DatabaseManager.executeStatement(REPLACE_SQL, databaseName,
                connectionID,
                displayName,
                successMessage,
                failureMessage,
                sourceRoomName,
                destRoomName,
                Domain.encodeDomains(sourceDomains),
                Domain.encodeDomains(destinationDomains),
                traverseDifficulty,
                traverseSkill != null ? traverseSkill.getSavableName() : "",
                detectDifficulty,
                Domain.encodeDomains(detectDomains),
                detectWord,
                failureEffect.name(),
                failureRoomName,
                Domain.encodeDomains(failureDestinationDomains),
                successEffect.name(),
                staminaCost,
                keyCode,
                state.name(),
                direction.name(),
                failureDirection != null? failureDirection.name():null);
        if (result > 0) addToCache(this);

        return result > 0;
    }

    public static RoomConnection getConnectionByID(String id, String databaseName) {
        RoomConnection toReturn = null;
        WorldSpecificCache<String, RoomConnection> worldCache = roomConnectionCache.get(databaseName);
        if (worldCache == null)
            roomConnectionCache.put(databaseName, new WorldSpecificCache<>(databaseName));
        else
            toReturn = worldCache.getValue(id);
        if (toReturn != null) return toReturn;

        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        if (c == null)
            return null;
        else {
            try {
                getSQL = c.prepareStatement(GET_ID_SQL);
                getSQL.setString(1, id);
                ResultSet resultSet = getSQL.executeQuery();
                if (resultSet.next())
                    toReturn = new RoomConnection(resultSet, databaseName);
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

    public static List<RoomConnection> getConnectionsBySourceRoom(String sourceRoomName, String databaseName) {
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        List<String> connectionIDs = new ArrayList<>();
        if (c == null)
            return Collections.emptyList();
        else {
            try {
                getSQL = c.prepareStatement(GET_SOURCE_SQL);
                getSQL.setString(1, sourceRoomName);
                ResultSet resultSet = getSQL.executeQuery();
                while (resultSet.next())
                    connectionIDs.add(resultSet.getString(CONNECTION_ID));
                getSQL.close();
            } catch (SQLException e) {
                System.out.println("Failed to retrieve connections with source: " + sourceRoomName);
                e.printStackTrace();
            }
        }

        //this is done so that if they have a room object, they can guarantee its the only room object with that ID
        List<RoomConnection> connections = new ArrayList<>();
        for (String id : connectionIDs)
            connections.add(getConnectionByID(id, databaseName));

        return connections;
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        int result = DatabaseManager.executeStatement(DELETE_SQL, databaseName, connectionID);
        if (result >= 0) removeFromCache(this);
        return result > 0;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        return saveToDatabase(databaseName);
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return getConnectionByID(connectionID, databaseName) != null;
    }
    //</editor-fold>

    //<editor-fold desc="Utility">
    public boolean isVisibleTo(Entity entity) {
        if (!entity.getDatabaseName().equals(databaseName))
            return false;
        if (detectDifficulty == 0)
            return true;

        return true;
    }

    private static void addToCache(RoomConnection connection) {
        WorldSpecificCache<String, RoomConnection> worldCache = roomConnectionCache.get(connection.databaseName);
        if (worldCache == null)
            roomConnectionCache.put(connection.databaseName, worldCache = new WorldSpecificCache<>(connection.databaseName));
        worldCache.putValue(connection.connectionID, connection);
    }

    private static void removeFromCache(RoomConnection connection) {
        WorldSpecificCache<String, RoomConnection> worldCache;
        if ((worldCache = roomConnectionCache.get(connection.databaseName)) != null)
            worldCache.remove(connection.connectionID);
    }

    @Override
    public int compareTo(RoomConnection o) {
        return this.connectionID.compareTo(o.connectionID);
    }
    //</editor-fold>

    //<editor-fold desc="Getters">
    public String getConnectionID() {
        return connectionID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public String getSourceRoomName() {
        return sourceRoomName;
    }

    public String getDestRoomName() {
        return destRoomName;
    }

    public int getTraverseDifficulty() {
        return traverseDifficulty;
    }

    public Skill getTraverseSkill() {
        return traverseSkill;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getDetectDifficulty() {
        return detectDifficulty;
    }

    public EffectArchetype getFailureEffect() {
        return failureEffect;
    }

    public EffectArchetype getSuccessEffect() {
        return successEffect;
    }

    public String getFailureRoomName() {
        return failureRoomName;
    }

    public List<Domain> getFailureDestinationDomains() {
        return failureDestinationDomains;
    }

    public List<Domain> getDetectDomains() {
        return detectDomains;
    }

    public String getDetectWord() {
        return detectWord;
    }

    public int getStaminaCost() {
        return staminaCost;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public Direction getDirection() {
        return direction;
    }

    public Direction getFailureDirection() {
        return failureDirection;
    }

    public List<Domain> getSourceDomains() {
        return sourceDomains;
    }

    /**
     * Get all available destination domains. Refer to RoomConnectionTable for more info.
     *
     * @return a non-empty list of domains, where the first element is the default domain
     */
    public List<Domain> getDestinationDomains() {
        if (destinationDomains.isEmpty())
            destinationDomains.add(Domain.surface);
        return destinationDomains;
    }

    //</editor-fold>
}
