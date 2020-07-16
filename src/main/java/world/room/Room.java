package main.java.world.room;

import main.java.database.DatabaseManager;
import main.java.world.entity.Entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static main.java.world.room.RoomDiscoveryToken.DetectionStatus.known;
import static main.java.world.room.RoomTable.*;

/**
 * This class is used to persist and store information on a single game room.
 *
 * @author Logan Earl
 */
public class Room implements DatabaseManager.DatabaseEntry {
    private String roomName;
    private String roomDescription;

    private List<Domain> domains;

    private String databaseName;

    private Map<EnvironmentType, EnvironmentLevelContainer> environmentalLevels = new LinkedHashMap<>();

    private enum EnvironmentType {
        wind(0, 0.1, 0, 100, WIND_LEVEL, WIND_EQUILIBRIUM, WIND_CONDUCTIVITY),
        rain(0, .05, 0, 100, RAIN_LEVEL, RAIN_EQUILIBRIUM, WIND_CONDUCTIVITY),
        temperature(0, 0.2, -100, 100, TEMP_LEVEL, TEMP_EQUILIBRIUM, TEMP_CONDUCTIVITY),
        toxic(0, .1, 0, 100, TOXIC_LEVEL, TOXIC_EQUILIBRIUM, TOXIC_CONDUCTIVITY),
        acid(0, .2, 0, 100, ACID_LEVEL, ACID_EQUILIBRIUM, ACID_CONDUCTIVITY),
        electricity(0, .1, 0, 100, ELECTRICITY_LEVEL, ELECTRICITY_EQUILIBRIUM, ELECTRICITY_CONDUCTIVITY), //depletes all at once. No conductivity
        information(100, .1, 0, 100, INFO_LEVEL, INFO_EQUILIBRIUM, INFO_CONDUCTIVITY),
        quake(0, .1, 0, 100, QUAKE_LEVEL, QUAKE_EQUILIBRIUM, QUAKE_CONDUCTIVITY),
        oil(0, .05, 0, 100, OIL_LEVEL, OIL_EQUILIBRIUM, OIL_CONDUCTIVITY);

        private final String TAG_LEVEL;
        private final String TAG_EQUILIBRIUM;
        private final String TAG_CONDUCTIVITY;
        private double defaultEquilibrium;
        private double defaultConductivity;
        private double maxLevel;
        private double minLevel;

        EnvironmentType(double defaultEquilibrium, double defaultConductivity, double minLevel, double maxLevel, String levelTag, String equalTag, String condTag) {
            this.defaultEquilibrium = defaultEquilibrium;
            this.defaultConductivity = defaultConductivity;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.TAG_LEVEL = levelTag;
            this.TAG_EQUILIBRIUM = equalTag;
            this.TAG_CONDUCTIVITY = condTag;
        }

        private double clampLevel(double level) {
            if (level < minLevel) level = minLevel;
            if (level > maxLevel) level = maxLevel;
            return level;
        }
    }

    private static class EnvironmentLevelContainer {
        private double level;
        private double equilibrium;
        private double conductivity;
        private EnvironmentType type;

        public EnvironmentLevelContainer(EnvironmentType type) {
            level = type.defaultEquilibrium;
            equilibrium = type.defaultEquilibrium;
            conductivity = type.defaultConductivity;
        }

        public EnvironmentLevelContainer(ResultSet parseFrom, EnvironmentType type) throws SQLException {
            this.level = parseFrom.getDouble(type.TAG_LEVEL);
            if (parseFrom.wasNull()) this.level = type.defaultEquilibrium;

            this.equilibrium = parseFrom.getDouble(type.TAG_EQUILIBRIUM);
            if (parseFrom.wasNull()) this.equilibrium = type.defaultEquilibrium;

            this.conductivity = parseFrom.getDouble(type.TAG_CONDUCTIVITY);
            if (parseFrom.wasNull()) this.conductivity = type.defaultConductivity;

            if (this.conductivity < 0.001) this.conductivity = 0.001;
            level = type.clampLevel(level);

        }

        public double getLevel() {
            return level;
        }

        public double getEquilibrium() {
            return equilibrium;
        }

        public double getConductivity() {
            return conductivity;
        }
    }

    //<editor-fold desc="SQL Operations/Constructor">
    private static final String GET_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?", TABLE_NAME, ROOM_NAME);
    private static final String CREATE_SQL = String.format(Locale.US, "INSERT INTO %s " +
                    "(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)" +
                    " VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            TABLE_NAME, ROOM_NAME, ROOM_DESCRIPTION, DOMAINS,
            WIND_LEVEL, WIND_EQUILIBRIUM, WIND_CONDUCTIVITY,
            RAIN_LEVEL, RAIN_EQUILIBRIUM, RAIN_CONDUCTIVITY,
            TEMP_LEVEL, TEMP_EQUILIBRIUM, TEMP_CONDUCTIVITY,
            TOXIC_LEVEL, TOXIC_EQUILIBRIUM, TEMP_CONDUCTIVITY,
            ACID_LEVEL, ACID_EQUILIBRIUM, ACID_CONDUCTIVITY,
            INFO_LEVEL, INFO_CONDUCTIVITY, INFO_EQUILIBRIUM,
            QUAKE_LEVEL, QUAKE_EQUILIBRIUM, QUAKE_CONDUCTIVITY,
            OIL_LEVEL, OIL_EQUILIBRIUM, OIL_CONDUCTIVITY);
    private static final String DELETE_SQL = String.format(Locale.US, "DELETE FROM %s WHERE %s=?", TABLE_NAME, ROOM_NAME);
    private static final String UPDATE_SQL = String.format(Locale.US, "UPDATE %s SET " +
                    "%s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?," +
                    "%s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?," +
                    "%s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=? WHERE %s=?",
            TABLE_NAME, ROOM_DESCRIPTION, DOMAINS,
            WIND_LEVEL, WIND_EQUILIBRIUM, WIND_CONDUCTIVITY,
            RAIN_LEVEL, RAIN_EQUILIBRIUM, RAIN_CONDUCTIVITY,
            TEMP_LEVEL, TEMP_EQUILIBRIUM, TEMP_CONDUCTIVITY,
            TOXIC_LEVEL, TOXIC_EQUILIBRIUM, TEMP_CONDUCTIVITY,
            ACID_LEVEL, ACID_EQUILIBRIUM, ACID_CONDUCTIVITY,
            INFO_LEVEL, INFO_CONDUCTIVITY, INFO_EQUILIBRIUM,
            QUAKE_LEVEL, QUAKE_EQUILIBRIUM, QUAKE_CONDUCTIVITY,
            OIL_LEVEL, OIL_EQUILIBRIUM, OIL_CONDUCTIVITY, ROOM_NAME);

    private Room(ResultSet readEntry, String databaseName) throws SQLException {
        roomName = readEntry.getString(ROOM_NAME);
        roomDescription = readEntry.getString(ROOM_DESCRIPTION);

        domains = Domain.decodeDomains(readEntry.getString(DOMAINS));
        if(domains.isEmpty())domains.add(Domain.grey);

        for (EnvironmentType type : EnvironmentType.values()) {
            environmentalLevels.put(type, new EnvironmentLevelContainer(readEntry, type));
        }

        this.databaseName = databaseName;
    }

    /**
     * How rooms are instantiated initially. Creates one from the given database
     *
     * @param roomName     the name of the room to look up
     * @param databaseName the name of the database file to search for the room
     * @return the room if it was found. null otherwise
     */
    public static Room getByRoomName(String roomName, String databaseName) {
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        Room toReturn;
        if (c == null)
            return null;
        else {
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1, roomName);
                ResultSet accountSet = getSQL.executeQuery();
                if (accountSet.next())
                    toReturn = new Room(accountSet, databaseName);
                else
                    toReturn = null;
                getSQL.close();
                //c.close();
            } catch (SQLException e) {
                toReturn = null;
            }
        }
        return toReturn;
    }

    @Override
    public boolean saveToDatabase(String databaseName) {
        Room room = getByRoomName(roomName, databaseName);
        if (room == null) {
            Object[] args = new Object[3 + environmentalLevels.size() * 3];
            args[0] = roomName;
            args[1] = roomDescription;
            args[2] = Domain.encodeDomains(domains);
            int count = 3;
            for (EnvironmentLevelContainer container : environmentalLevels.values()) {
                args[count] = container.getLevel();
                args[count + 1] = container.getEquilibrium();
                args[count + 2] = container.getConductivity();
                count += 3;
            }

            return DatabaseManager.executeStatement(CREATE_SQL, databaseName, args) > 0;
        } else {
            return updateInDatabase(databaseName);
        }
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        return DatabaseManager.executeStatement(DELETE_SQL, databaseName, roomName) > 0;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        Object[] args = new Object[2 + environmentalLevels.size() * 3];
        args[0] = roomDescription;
        args[1] = Domain.encodeDomains(domains);
        int count = 2;
        for (EnvironmentLevelContainer container : environmentalLevels.values()) {
            args[count] = container.getLevel();
            args[count + 1] = container.getEquilibrium();
            args[count + 2] = container.getConductivity();
            count += 3;
        }

        return DatabaseManager.executeStatement(UPDATE_SQL, databaseName,
                roomDescription, args) > 0;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return getByRoomName(roomName, databaseName) != null;
    }
    //</editor-fold>

    //<editor-fold desc="Getters">
    public String getRoomName() {
        return roomName;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public List<RoomConnection> getOutgoingConnections() {
        return RoomConnection.getConnectionsBySourceRoom(roomName, databaseName);
    }

    public RoomConnection getOutgoingConnectionByIndex(int index, Entity viewer){
        List<RoomConnection> visibleRooms = getOutgoingConnectionsFromPOV(viewer, known);
        if(index >= 0 && index < visibleRooms.size())
            return visibleRooms.get(index);
        return null;
    }

    public void detectTriviallyVisibleConnections(Entity viewer){
        for(RoomConnection connection: getOutgoingConnections()) {
            RoomDiscoveryToken token = RoomDiscoveryToken.getToken(viewer.getID(), connection.getConnectionID(), viewer.getDatabaseName());
            if (connection.getDetectDifficulty() == null && token.getDetectedStatus() != known)
                RoomDiscoveryToken.revealConnection(viewer.getID(), connection.getConnectionID(), System.currentTimeMillis(), viewer.getDatabaseName());
            else{
                if(token.getDetectedStatus() == RoomDiscoveryToken.DetectionStatus.unencountered) {
                    token.hide(System.currentTimeMillis());
                }
            }
        }
    }

    public List<RoomConnection> getOutgoingConnectionsFromPOV(Entity viewer, RoomDiscoveryToken.DetectionStatus desiredStatus) {
        List<RoomConnection> availableConnections = RoomConnection.getConnectionsBySourceRoom(roomName, databaseName);
        List<RoomConnection> applicableConnections = new ArrayList<>();
        for (RoomConnection connection : availableConnections) {
            RoomDiscoveryToken token = RoomDiscoveryToken.getToken(viewer.getID(),connection.getConnectionID(),databaseName);
            if (token.getDetectedStatus() == desiredStatus)
                applicableConnections.add(connection);
        }
        return applicableConnections;
    }

    public List<Domain> getDomains(){
        return domains;
    }

    public Domain getDefaultDomain(){
        return domains.get(0);
    }

    public String getDatabaseName() {
        return databaseName;
    }
    //</editor-fold>
}
