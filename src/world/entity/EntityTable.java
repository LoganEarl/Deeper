package world.entity;

import database.DatabaseManager;

import java.util.Map;

public class EntityTable implements DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "entityTable";
    public static final String ENTITY_ID = "entityID";

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return null;
    }
}
