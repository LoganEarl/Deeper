package main.java.world.story;

import main.java.database.DatabaseManager;
import main.java.world.entity.EntityTable;

import java.util.*;

/**
 * Contains the schema for a table that establishes the many to many relationship between entities and dialogs.
 * @author Logan Earl
 */
public class EntityDialogTable implements DatabaseManager.DatabaseTable {
    /**The name of the table used to store entity-dialog relations*/
    public static final String TABLE_NAME = "entityDialog";

    /**Foreign key to the entity table. Holds the identifier of the entity*/
    public static final String ENTITY_ID = EntityTable.ENTITY_ID;
    /**Foreign key to the dialog table. Holds the identifier of the dialog option the given entity can give*/
    public static final String DIALOG_ID = DialogTable.DIALOG_ID;

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final Set<String> CONSTRAINTS = new HashSet<>();

    public EntityDialogTable(){
        TABLE_DEFINITION.put(ENTITY_ID, "VARCHAR(32) NOT NULL");
        TABLE_DEFINITION.put(DIALOG_ID, "INT NOT NULL");

        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ENTITY_ID, EntityTable.TABLE_NAME, EntityTable.ENTITY_ID));
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                DIALOG_ID, DialogTable.TABLE_NAME, DialogTable.DIALOG_ID));
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
}
