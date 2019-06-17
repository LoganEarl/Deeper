package world.item.consumable;

import database.DatabaseManager;
import world.item.ItemStatTable;

import java.util.*;

public class ConsumableStatTable implements  DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "consumableStats";

    public static final String ITEM_NAME = ItemStatTable.ITEM_NAME;
    public static final String EFFECT_DURATION_MILLIS = "effectDurationMillis";
    public static final String HP_GAIN = "hpGain";
    public static final String MP_GAIN = "mpGain";
    public static final String STAMINA_GAIN = "stamGain";
    public static final String BURNOUT_GAIN = "burnGain";

    public static final String HP_REGEN = "hpRegenPerSec";
    public static final String MP_REGEN = "mpGainPerSec";
    public static final String STAMINA_REGEN = "stamGainPerSec";
    public static final String BURNOUT_REGEN = "burnGainerSec";

    public static final String DAMAGE_TYPE = "damageType";

    private static final String GET_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?", TABLE_NAME, ITEM_NAME);

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    private static final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private static final Set<String> CONSTRAINTS = new HashSet<>(1);

    public ConsumableStatTable(){
        TABLE_DEFINITION.put(ITEM_NAME, "VARCHAR(32) PRIMARY KEY NOT NULL");

        TABLE_DEFINITION.put(EFFECT_DURATION_MILLIS, "INT");
        TABLE_DEFINITION.put(HP_GAIN, "INT");
        TABLE_DEFINITION.put(MP_GAIN, "INT");
        TABLE_DEFINITION.put(STAMINA_GAIN, "INT");
        TABLE_DEFINITION.put(BURNOUT_GAIN, "INT");

        TABLE_DEFINITION.put(HP_REGEN, "INT");
        TABLE_DEFINITION.put(MP_REGEN, "INT");
        TABLE_DEFINITION.put(STAMINA_REGEN, "INT");
        TABLE_DEFINITION.put(BURNOUT_REGEN, "INT");

        TABLE_DEFINITION.put(DAMAGE_TYPE, "VARCHAR(16)");

        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ITEM_NAME, ItemStatTable.TABLE_NAME, ItemStatTable.ITEM_NAME));
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

    public static Map<String,String> getStatsForConsumable(String itemName, String databaseName){
        return ItemStatTable.getStatsForRawItem(itemName,databaseName,GET_SQL,TABLE_DEFINITION.keySet());
    }

}
