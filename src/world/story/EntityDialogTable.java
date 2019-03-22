package world.story;

import database.DatabaseManager;
import world.entity.EntityTable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

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

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    public final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    public EntityDialogTable(){
        TABLE_DEFINITION.put(ENTITY_ID, String.format(Locale.US,"VARCHAR(32), FOREIGN KEY (%s) REFERENCES %s(%s)",
                ENTITY_ID, EntityTable.TABLE_NAME, EntityTable.ENTITY_ID));
        TABLE_DEFINITION.put(DIALOG_ID, String.format(Locale.US,"INT, FOREIGN KEY (%s) REFERENCES %s(%s)",
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
}
