package world.entity.race;

import database.DatabaseManager;

import java.util.*;

/**
 * Instantiated form of the table of available races. Contains the definition for the table.
 * @author Logan Earl
 */
public class RaceTable implements DatabaseManager.DatabaseTable {
    /**The name of the database table*/
    public static final String TABLE_NAME = "race";

    /**The unique identifier of the race as written in the database. EG "human"*/
    public static final String RACE_ID = "raceID";
    /**The displayed value of the race. Works like the DisplayName of an entity*/
    public static final String DISPLAY_NAME = "displayName";
    /**The description for the race*/
    public static final String DESCRIPTION = "description";
    /**Base intelligence value for the race. 0-100 scale with an average human citizen being 10*/
    public static final String BASE_INT = "baseInt";
    /**Base wisdom value for the race. 0-100 scale with an average human citizen being 10*/
    public static final String BASE_WIS = "baseWis";
    /**Base strength value for the race. 0-100 scale with an average human citizen being 10*/
    public static final String BASE_STR = "baseStr";
    /**Base dexterity value for the race. 0-100 scale with an average human citizen being 10*/
    public static final String BASE_DEX = "baseDex";
    /**Base toughness value for the race. 0-100 scale with an average human citizen being 10*/
    public static final String BASE_TOUGH = "baseTough";
    /**Base fitness value for the race. 0-100 scale with an average human citizen being 10*/
    public static final String BASE_FIT = "baseFit";

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    public static final Map<String,String> COLUMN_DEFINITIONS = new LinkedHashMap<>();

    public RaceTable(){
        COLUMN_DEFINITIONS.put(RACE_ID, "VARCHAR(16) PRIMARY KEY NOT NULL");
        COLUMN_DEFINITIONS.put(DISPLAY_NAME, "VARCHAR(32)");
        COLUMN_DEFINITIONS.put(DESCRIPTION, "TEXT");
        COLUMN_DEFINITIONS.put(BASE_INT, "INT");
        COLUMN_DEFINITIONS.put(BASE_WIS, "INT");
        COLUMN_DEFINITIONS.put(BASE_STR, "INT");
        COLUMN_DEFINITIONS.put(BASE_DEX, "INT");
        COLUMN_DEFINITIONS.put(BASE_TOUGH, "INT");
        COLUMN_DEFINITIONS.put(BASE_FIT, "INT");
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return COLUMN_DEFINITIONS;
    }

    @Override
    public Set<String> getConstraints() {
        return Collections.emptySet();
    }
}
