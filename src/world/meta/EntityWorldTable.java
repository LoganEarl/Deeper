package world.meta;

import database.DatabaseManager;
import world.entity.EntityTable;

import java.util.*;

/**
 * A table made to record what world each entity is in. Each world is it's own database file,
 * so we can't put the entity's current world
 */
public class EntityWorldTable implements DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "entityWorldTable";

    public static final String ENTITY_ID = EntityTable.ENTITY_ID;
    public static final String WORLD_ID = WorldTable.WORLD_ID;

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    public final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();



    public EntityWorldTable(){
        TABLE_DEFINITION.put(ENTITY_ID, "VARCHAR(32) PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(WORLD_ID, "INT");
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
        return Collections.singleton(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
        WORLD_ID, WorldTable.TABLE_NAME, WorldTable.WORLD_ID));
    }
}
