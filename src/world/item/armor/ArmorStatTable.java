package world.item.armor;

import database.DatabaseManager;
import world.item.ItemStatTable;

import java.util.*;

public class ArmorStatTable implements DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "armorStats";

    public static final String ITEM_NAME = ItemStatTable.ITEM_NAME;
    public static final String ARMOR_CLASS = "armorClass";
    public static final String PIERCE_DEFENCE = "defencePierce";
    public static final String SLASH_DEFENCE = "defenceSlash";
    public static final String CRUSH_DEFENCE = "defenceCrush";
    public static final String HEAT_DEFENCE = "defenceHeat";
    public static final String COLD_DEFENCE = "defenceCold";
    public static final String POISON_DEFENCE = "defencePoison";
    public static final String CORROSIVE_DEFENCE = "defenceCorrosive";
    public static final String ELECTRIC_DEFENCE = "defenceElectric";
    public static final String PLASMA_DEFENCE = "defencePlasma";
    public static final String OBLIVION_DEFENCE = "defenceOblivion";

    public static final String ARMOR_SLOT = "armorSlot";
    public static final String ARMOR_TYPE = "armorType";

    private static final String GET_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?", TABLE_NAME, ITEM_NAME);

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    private static final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private static final Set<String> CONSTRAINTS = new HashSet<>(1);

    public ArmorStatTable(){
        TABLE_DEFINITION.put(ITEM_NAME,"VARCHAR(32) PRIMARY KEY NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(ARMOR_CLASS,"INT");

        TABLE_DEFINITION.put(PIERCE_DEFENCE, "DECIMAL");
        TABLE_DEFINITION.put(SLASH_DEFENCE, "DECIMAL");
        TABLE_DEFINITION.put(CRUSH_DEFENCE, "DECIMAL");
        TABLE_DEFINITION.put(HEAT_DEFENCE, "DECIMAL");
        TABLE_DEFINITION.put(COLD_DEFENCE, "DECIMAL");
        TABLE_DEFINITION.put(POISON_DEFENCE, "DECIMAL");
        TABLE_DEFINITION.put(CORROSIVE_DEFENCE, "DECIMAL");
        TABLE_DEFINITION.put(ELECTRIC_DEFENCE, "DECIMAL");
        TABLE_DEFINITION.put(PLASMA_DEFENCE, "DECIMAL");
        TABLE_DEFINITION.put(OBLIVION_DEFENCE, "DECIMAL");
        TABLE_DEFINITION.put(ARMOR_TYPE,"VARCHAR(16)");
        TABLE_DEFINITION.put(ARMOR_SLOT,"VARCHAR(16)");

        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ITEM_NAME, ItemStatTable.TABLE_NAME, ItemStatTable.ITEM_NAME));
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    public static Map<String,String> getStatsForArmor(String itemName, String databaseName){
        return ItemStatTable.getStatsForRawItem(itemName,databaseName,GET_SQL,TABLE_DEFINITION.keySet());
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
