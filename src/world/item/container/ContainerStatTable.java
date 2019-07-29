package world.item.container;

import database.DatabaseManager;
import world.item.ItemStatTable;
import world.meta.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static world.item.Item.NULL_ITEM_NAME;

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
    public static final String ITEM_NAME = ItemStatTable.ITEM_NAME;
    /**The maximum storage potential of the container in KGs*/
    public static final String MAX_KGS = "maxKgs";
    /**The maximum storage potential of the container in Liters*/
    public static final String MAX_LITERS = "maxLiters";
    /**The maximum storage potential of the container in number of items stored*/
    public static final String MAX_NUMBER = "maxItems";
    /**Stores how difficult the lock is to pick. {@value CODE_NOT_USED} means it has no lock*/
    public static final String LOCK_DIFFICULTY = "lockDifficulty";

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    private static final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    private static final Set<String> CONSTRAINTS = new HashSet<>(1);

    private static final String GET_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?", TABLE_NAME, ITEM_NAME);
    private static final String INSERT_SQL = String.format(Locale.US, "INSERT INTO %s(%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
            TABLE_NAME, ITEM_NAME, MAX_KGS, MAX_LITERS, MAX_NUMBER);


    public ContainerStatTable(){
        TABLE_DEFINITION.put(ITEM_NAME, "VARCHAR(32) PRIMARY KEY NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(MAX_KGS,"DECIMAL");
        TABLE_DEFINITION.put(MAX_LITERS,"DECIMAL");
        TABLE_DEFINITION.put(MAX_NUMBER,"INT");

        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ITEM_NAME, ItemStatTable.TABLE_NAME, ItemStatTable.ITEM_NAME));
    }

    public static Map<String,String> getStatsForContainer(String itemName, String databaseName){
        return ItemStatTable.getStatsForRawItem(itemName,databaseName,GET_SQL,TABLE_DEFINITION.keySet());
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

    public static boolean existsInWorld(String itemName, World targetWorld){
        return null != getStatsForContainer(itemName,targetWorld.getDatabaseName());
    }

    public static boolean writeStatsToWorld(Map<String,String> stats, World targetWorld){
        if(existsInWorld(stats.getOrDefault(ITEM_NAME,NULL_ITEM_NAME),targetWorld))
            return false;

        String name = stats.get(ITEM_NAME);
        String rawMaxKg = stats.get(MAX_KGS);
        String rawMaxLiters = stats.get(MAX_LITERS);
        String rawMaxNumber = stats.get(MAX_NUMBER);

        Double maxKg, maxLiters, maxNumber;
        try {
            maxKg = Double.parseDouble(rawMaxKg);
            maxLiters = Double.parseDouble(rawMaxLiters);
            maxNumber = Double.parseDouble(rawMaxNumber);
        }catch (NumberFormatException ignored){
            return false;
        }

        return DatabaseManager.executeStatement(INSERT_SQL, targetWorld.getDatabaseName(),
                name, maxKg, maxLiters, maxNumber) > 0;
    }
}
