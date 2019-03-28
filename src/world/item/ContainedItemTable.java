package world.item;

import database.DatabaseManager;

import java.util.*;

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

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final List<String> CONSTRAINTS = new ArrayList<>();

    public ContainedItemTable(){
        TABLE_DEFINITION.put(CONTAINER_ID, "INT NOT NULL");
        TABLE_DEFINITION.put(ITEM_ID, "INT NOT NULL");
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                CONTAINER_ID, ContainerInstanceTable.TABLE_NAME, ContainerInstanceTable.CONTAINER_ID));
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ITEM_ID, ItemInstanceTable.TABLE_NAME, ItemInstanceTable.ITEM_ID));
        CONSTRAINTS.add(String.format(Locale.US,"PRIMARY KEY (%s, %s)",
                CONTAINER_ID, ITEM_ID));

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
    public List getConstraints() {
        return CONSTRAINTS;
    }
}
