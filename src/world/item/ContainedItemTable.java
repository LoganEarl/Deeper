package world.item;

import database.DatabaseManager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Contains the schema for a small table meant to store what item instances are stored in what container
 * @author Logan Earl
 */
public class ContainedItemTable implements DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "containedItems";

    /**References the container that the item is stored in*/
    public static final String CONTAINER_ID = ContainerInstanceTable.CONTAINER_ID; //foreign key
    /**References the item that is stored in the container*/
    public static final String ITEM_ID = ItemInstanceTable.ITEM_ID; //foreign key

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    public final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    public ContainedItemTable(){
        TABLE_DEFINITION.put(CONTAINER_ID, String.format(Locale.US,"INT, FOREIGN KEY (%s) REFERENCES %s(%s) NOT NULL",
                CONTAINER_ID, ContainerInstanceTable.TABLE_NAME, ContainerInstanceTable.CONTAINER_ID));
        TABLE_DEFINITION.put(ITEM_ID, String.format(Locale.US,"INT, FOREIGN KEY (%s) REFERENCES %s(%s) NOT NULL",
                ITEM_ID, ItemInstanceTable.TABLE_NAME, ItemInstanceTable.ITEM_ID));
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return TABLE_DEFINITION;
    }
}
