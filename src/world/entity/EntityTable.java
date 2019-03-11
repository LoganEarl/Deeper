package world.entity;

import database.DatabaseManager;

import java.util.Map;

public class EntityTable implements DatabaseManager.DatabaseTable {
    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return null;
    }
}
