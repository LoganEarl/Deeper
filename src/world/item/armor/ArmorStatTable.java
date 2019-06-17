package world.item.armor;

import database.DatabaseManager;

import java.util.Map;
import java.util.Set;

public class ArmorStatTable implements DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "armorStats";

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return null;
    }

    @Override
    public Set<String> getConstraints() {
        return null;
    }
}
