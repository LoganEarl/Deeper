package main.java.world.room;

import main.java.database.DatabaseManager;
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
    public static final String IS_VISIBLE = "isVisible";

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final Set<String> CONSTRAINTS = new HashSet<>(2);

    public RoomConnectionDiscoveryTable(){
        TABLE_DEFINITION.put(CONNECTION_ID,"VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(ENTITY_ID,"VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(LAST_UPDATE,"INT NOT NULL");
        TABLE_DEFINITION.put(IS_VISIBLE,"INT NOT NULL");

        CONSTRAINTS.add(String.format(Locale.US,"PRIMARY KEY (%s, %s)",CONNECTION_ID,ENTITY_ID));
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                CONNECTION_ID, RoomConnectionTable.TABLE_NAME, RoomConnectionTable.CONNECTION_ID));
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ENTITY_ID, EntityTable.TABLE_NAME, EntityTable.ENTITY_ID));
    }

    private static final String GET_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE %s=? AND %s=?",TABLE_NAME,CONNECTION_ID,ENTITY_ID);

    static boolean connectionIsVisible(String connectionID, String entityID, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL = null;
        boolean toReturn;
        if(c == null)
            return false;
        else{
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1,connectionID);
                getSQL.setString(2,entityID);
                ResultSet resultSet = getSQL.executeQuery();
                if(resultSet.next())
                    toReturn = resultSet.getInt(IS_VISIBLE) == 1;
                else
                    toReturn = false;
                getSQL.close();
            }catch (SQLException e){
                toReturn = false;
            }
        }
        return toReturn;
    }

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
