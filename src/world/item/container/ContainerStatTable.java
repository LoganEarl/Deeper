package world.item.container;

import database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Holds the stats of different containers. Distinct from an instance of a container in that it has no location,
 * bound items, etc. This table just stores the stats so that the data usage is minimized.<br>
 *     Note, when it comes to maximum storage potential, any value (kg,l,#) can be null, and all not null values are applied
 * @author Logan Earl
 */
public class ContainerStatTable implements DatabaseManager.DatabaseTable {
    /**The name of the table used to store container stats*/
    public static final String TABLE_NAME = "containerStats";

    /**If the MAX_KGS, MAX_LITERS, MAX_NUMBER, or LOCK_DIFFICULTY has this value the value is not applicable or not used*/
    public static final int CODE_NOT_USED = -1;
    /**The name of the container*/
    public static final String CONTAINER_NAME = "containerName";
    /**The description of the container*/
    public static final String CONTAINER_DESCRIPTION = "containerDescription";
    /**The maximum storage potential of the container in KGs*/
    public static final String MAX_KGS = "maxKgs";
    /**The maximum storage potential of the container in Liters*/
    public static final String MAX_LITERS = "maxLiters";
    /**The maximum storage potential of the container in number of items stored*/
    public static final String MAX_NUMBER = "maxItems";
    /**Stores how difficult the lock is to pick. {@value CODE_NOT_USED} means it has no lock*/
    public static final String LOCK_DIFFICULTY = "lockDifficulty";

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    public final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    private static final String GET_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?", TABLE_NAME, CONTAINER_NAME);

    public ContainerStatTable(){
        TABLE_DEFINITION.put(CONTAINER_NAME, "VARCHAR(32) PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(CONTAINER_DESCRIPTION, "TEXT");
        TABLE_DEFINITION.put(MAX_KGS,"DECIMAL");
        TABLE_DEFINITION.put(MAX_LITERS,"DECIMAL");
        TABLE_DEFINITION.put(MAX_NUMBER,"INT");
    }

    public static Map<String,String> getStatsForContainer(String itemName, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        Map<String, String> containerState = new HashMap<>();
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1,itemName);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next()) {
                    containerState.put(CONTAINER_NAME, accountSet.getString(CONTAINER_NAME));
                    containerState.put(CONTAINER_DESCRIPTION, accountSet.getString(CONTAINER_DESCRIPTION));
                    containerState.put(MAX_KGS, accountSet.getString(MAX_KGS));
                    containerState.put(MAX_LITERS, accountSet.getString(MAX_LITERS));
                    containerState.put(MAX_NUMBER, accountSet.getString(MAX_NUMBER));
                    containerState.put(LOCK_DIFFICULTY, accountSet.getString(LOCK_DIFFICULTY));
                }else
                    containerState = null;
                getSQL.close();
                //c.close();
            }catch (SQLException e){
                containerState = null;
            }
        }
        return containerState;
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
    public List<String> getConstraints() {
        return null;
    }
}
