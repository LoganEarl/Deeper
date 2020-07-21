package main.java.world.room;

import main.java.database.DatabaseManager;
import main.java.database.EnumTable;
import main.java.world.entity.EntityTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RoomConnectionDiscoveryTable implements DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "roomConnectionDiscovery";

    public static final String CONNECTION_ID = RoomConnectionTable.CONNECTION_ID;
    public static final String ENTITY_ID = EntityTable.ENTITY_ID;
    public static final String LAST_UPDATE = "lastUpdate";
    public static final String DETECTION_STATUS = "detectionStatus";

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final Set<String> CONSTRAINTS = new HashSet<>(2);

    public RoomConnectionDiscoveryTable(){
        TABLE_DEFINITION.put(CONNECTION_ID,"VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(ENTITY_ID,"VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(LAST_UPDATE,"INT NOT NULL");
        TABLE_DEFINITION.put(DETECTION_STATUS,"VARCHAR(16) NOT NULL DEFAULT unencountered");

        CONSTRAINTS.add(String.format(Locale.US,"PRIMARY KEY (%s, %s)",CONNECTION_ID,ENTITY_ID));
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                CONNECTION_ID, RoomConnectionTable.TABLE_NAME, RoomConnectionTable.CONNECTION_ID));
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ENTITY_ID, EntityTable.TABLE_NAME, EntityTable.ENTITY_ID));
        CONSTRAINTS.add(String.format(Locale.US, "FOREIGN KEY (%s) REFERENCES %s(%s)",
                                      DETECTION_STATUS, RoomDiscoveryToken.DetectionStatus.TABLE_NAME, EnumTable.ENUM_NAME));
    }

    private static final String GET_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE %s=? AND %s=?",TABLE_NAME,CONNECTION_ID,ENTITY_ID);

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return TABLE_DEFINITION;
    }

    @Override
    public Set<String> getConstraints() {
        return CONSTRAINTS;
    }
}
