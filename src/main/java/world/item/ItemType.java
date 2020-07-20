package main.java.world.item;

import main.java.database.DatabaseManager;

import java.sql.ResultSet;
import java.util.*;

public enum ItemType {
    //TODO add a table definition to this
    weapon, armor, consumable, ammo, container, misc;

    private static final String INSERT_SQL = String.format(Locale.US,"REPLACE INTO %s(%s) VALUES(?)",ItemTypeTable.TABLE_NAME,ItemTypeTable.TYPE);

    public static ItemType extractFromResultSet(ResultSet readFrom) {
        try{
            return valueOf(readFrom.getString(ItemStatTable.ITEM_TYPE));
        }catch (Exception e){
            System.out.println("An item had an un-parsable type and defaulted to misc");
            return misc;
        }
    }

    public static void writeItemTypesToDatabaseFile(String databaseName){
        for(ItemType type: values())
            DatabaseManager.executeStatement(INSERT_SQL,databaseName, type.name());
    }

    public static class ItemTypeTable implements DatabaseManager.DatabaseTable{
        public static final String TABLE_NAME = "itemType";

        public static final String TYPE = "type";

        private final Map<String,String> definitions = new HashMap<>();

        public ItemTypeTable() {
            definitions.put(TYPE, "VARCHAR(16) PRIMARY KEY NOT NULL DEFAULT misc");
        }

        @Override
        public String getTableName() {
            return TABLE_NAME;
        }

        @Override
        public Map<String, String> getColumnDefinitions() {
            return definitions;
        }

        @Override
        public Set<String> getConstraints() {
            return Collections.emptySet();
        }
    }
}
