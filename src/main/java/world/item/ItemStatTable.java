package main.java.world.item;

import main.java.database.DatabaseManager;
import main.java.world.meta.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static main.java.world.item.Item.NULL_ITEM_NAME;

/**
 * Contains the definition for a SQL table that holds the stats for different types of items. Does not hold the
 * specific instances of each item, only the stats.
 * @author Logan Earl
 */
public class ItemStatTable implements DatabaseManager.DatabaseTable {
    /**The name of the table used to store item stats*/
    public static final String TABLE_NAME = "itemStats";

    /**The name of the item. Used as a unique identifier*/
    public static final String ITEM_NAME = "itemName";
    /**The description of the item*/
    public static final String ITEM_DESCRIPTION = "itemDescription";
    /**The weight of the item in KGs*/
    public static final String WEIGHT = "weightKgs";
    /**The size of the item in Liters*/
    public static final String VOLUME = "volume";
    /**The traits that the item (and all items of this type) have. Must be a semi-colon separated list*/
    public static final String GLOBAL_INHERENT_TRAITS = "globalInherentTraits";
    /**The traits the item (and all items of this type) bestow on their user. Must be a semi-colon separated list*/
    public static final String GLOBAL_BESTOWED_TRAITS = "globalBestowedTraits";

    /**The type of the item. Must be one of the TYPE_* constants defined in this class*/
    public static final String ITEM_TYPE = "itemType";

    //TODO create a table for these and set up foreign keys
    public enum WeaponType{
        head, chest, legs, feet, hands
    }

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    public final static Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    private final Set<String> CONSTRAINTS = new HashSet<>();

    public ItemStatTable(){
        TABLE_DEFINITION.put(ITEM_NAME, "VARCHAR(32) PRIMARY KEY NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(ITEM_DESCRIPTION, "TEXT");
        TABLE_DEFINITION.put(WEIGHT,"DECIMAL");
        TABLE_DEFINITION.put(VOLUME,"DECIMAL");
        TABLE_DEFINITION.put(ITEM_TYPE,"VARCHAR(16) DEFAULT misc");
        TABLE_DEFINITION.put(GLOBAL_INHERENT_TRAITS, "TEXT");
        TABLE_DEFINITION.put(GLOBAL_BESTOWED_TRAITS, "TEXT");

        CONSTRAINTS.add(String.format(Locale.US, "FOREIGN KEY %s REFERENCES %s(%s)", ITEM_NAME, ItemType.ItemTypeTable.TABLE_NAME, ItemType.ItemTypeTable.TYPE));
    }

    private static final String GET_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?", TABLE_NAME, ITEM_NAME);
    private static final String INSERT_SQL = String.format(Locale.US, "INSERT INTO %s(%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)",
        TABLE_NAME, ITEM_NAME, ITEM_DESCRIPTION, WEIGHT, VOLUME, ITEM_TYPE);

    public static Map<String,String> getStatsForItem(String itemName, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        Map<String, String> itemStats;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1,itemName);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next()) {
                    itemStats= getStatsFromResultSet(accountSet, TABLE_DEFINITION.keySet());
                }else
                    itemStats = null;
                getSQL.close();
                //c.close();
            }catch (SQLException e){
                itemStats = null;
            }
        }
        return itemStats;
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

    public static Map<String,String> getStatsFromResultSet(ResultSet statEntry, Set<String> columns) throws SQLException{
        Map<String,String> results = new HashMap<>();

        for(String key: columns){
            results.put(key,statEntry.getString(key));
        }
        return results;
    }

    public static Map<String,String> getStatsForRawItem(String itemName, String databaseName, String rawGetSQL, Set<String> columns){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        Map<String, String> containerState;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(rawGetSQL);
                getSQL.setString(1,itemName);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next()) {
                    containerState = ItemStatTable.getStatsFromResultSet(accountSet, columns);
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

    public static boolean existsInWorld(String itemName, World targetWorld){
        return null != getStatsForRawItem(itemName,targetWorld.getDatabaseName(),
                GET_SQL, new ItemStatTable().getColumnDefinitions().keySet());
    }

    public static boolean writeStatsToWorld(Map<String,String> stats, World targetWorld){
        if(existsInWorld(stats.getOrDefault(ITEM_NAME,NULL_ITEM_NAME),targetWorld))
            return false;

        String name = stats.get(ITEM_NAME);
        String desc = stats.get(ITEM_DESCRIPTION);
        String type = stats.get(ITEM_TYPE);
        String rawWeight = stats.get(WEIGHT);
        String rawVolume = stats.get(VOLUME);
        Double weight, volume;
        try {
            weight = Double.parseDouble(rawWeight);
            volume = Double.parseDouble(rawVolume);
        }catch (NumberFormatException ignored){
            return false;
        }

        return DatabaseManager.executeStatement(INSERT_SQL, targetWorld.getDatabaseName(),
                name, desc, weight, volume, type) > 0;
    }
}
