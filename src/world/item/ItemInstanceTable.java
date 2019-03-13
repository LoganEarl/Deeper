package world.item;

import database.DatabaseManager.DatabaseTable;

import java.util.Map;

public class ItemInstanceTable implements DatabaseTable {
    public static final String ITEM_ID = "itemID";

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return null;
    }
}
