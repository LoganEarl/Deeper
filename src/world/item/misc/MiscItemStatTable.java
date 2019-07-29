package world.item.misc;

import database.DatabaseManager;
import world.item.ItemStatTable;
import world.meta.World;

import java.util.*;

import static world.item.Item.NULL_ITEM_NAME;

public class MiscItemStatTable implements  DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "miscStat";

    public static final String ITEM_NAME = ItemStatTable.ITEM_NAME;

    private static final String GET_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?", TABLE_NAME, ITEM_NAME);
    private static final String INSERT_SQL = String.format(Locale.US, "INSERT INTO %s(%s) VALUES (?)",
            TABLE_NAME, ITEM_NAME);

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    private static final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    private static final Set<String> CONSTRAINTS = new HashSet<>(1);

    public MiscItemStatTable(){
        TABLE_DEFINITION.put(ITEM_NAME, "VARCHAR(32) PRIMARY KEY NOT NULL COLLATE NOCASE");

        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ITEM_NAME, ItemStatTable.TABLE_NAME, ItemStatTable.ITEM_NAME));
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

    public static Map<String,String> getStatsForMisc(String itemName, String databaseName){
        return ItemStatTable.getStatsForRawItem(itemName,databaseName,GET_SQL,TABLE_DEFINITION.keySet());
    }

    public static boolean existsInWorld(String itemName, World targetWorld){
        return null != getStatsForMisc(itemName,targetWorld.getDatabaseName());
    }

    public static boolean writeStatsToWorld(Map<String,String> stats, World targetWorld){
        if(existsInWorld(stats.getOrDefault(ITEM_NAME,NULL_ITEM_NAME),targetWorld))
            return false;

        String name = stats.get(ITEM_NAME);

        /*try {

        }catch (NumberFormatException ignored){
            return false;
        }*/

        return DatabaseManager.executeStatement(INSERT_SQL, targetWorld.getDatabaseName(),
                name) > 0;
    }
}
