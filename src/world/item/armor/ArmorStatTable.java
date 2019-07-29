package world.item.armor;

import database.DatabaseManager;
import world.item.ItemStatTable;
import world.meta.World;

import java.util.*;

import static world.item.Item.NULL_ITEM_NAME;

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
    private static final String INSERT_SQL = String.format(Locale.US, "INSERT INTO %s(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            TABLE_NAME, ITEM_NAME, ARMOR_CLASS, PIERCE_DEFENCE, SLASH_DEFENCE, CRUSH_DEFENCE, HEAT_DEFENCE, COLD_DEFENCE, POISON_DEFENCE,
            CORROSIVE_DEFENCE, ELECTRIC_DEFENCE, PLASMA_DEFENCE, OBLIVION_DEFENCE, ARMOR_SLOT, ARMOR_TYPE);

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

    public static boolean existsInWorld(String itemName, World targetWorld){
        return null != getStatsForArmor(itemName,targetWorld.getDatabaseName());
    }

    public static boolean writeStatsToWorld(Map<String,String> stats, World targetWorld){
        if(existsInWorld(stats.getOrDefault(ITEM_NAME,NULL_ITEM_NAME),targetWorld))
            return false;

        String name = stats.get(ITEM_NAME);
        String rawAC = stats.get(ARMOR_CLASS);

        String rawPierce = stats.get(PIERCE_DEFENCE);
        String rawSlash = stats.get(SLASH_DEFENCE);
        String rawCrush = stats.get(CRUSH_DEFENCE);
        String rawHeat = stats.get(HEAT_DEFENCE);
        String rawCold = stats.get(COLD_DEFENCE);
        String rawPoison = stats.get(POISON_DEFENCE);
        String RawCorrosive = stats.get(CORROSIVE_DEFENCE);
        String RawElectric = stats.get(ELECTRIC_DEFENCE);
        String rawPlasma = stats.get(PLASMA_DEFENCE);
        String rawOblivion = stats.get(OBLIVION_DEFENCE);

        String armorSlot = stats.get(ARMOR_SLOT);
        String armorType = stats.get(ARMOR_TYPE);

        Integer AC;
        Double pierce, slash, crush, heat, cold, poison, corrosive,
            electric, plasma, oblivion;
        try {
            AC = Integer.parseInt(rawAC);
            pierce = Double.parseDouble(rawPierce);
            slash = Double.parseDouble(rawSlash);
            crush = Double.parseDouble(rawCrush);
            heat = Double.parseDouble(rawHeat);
            cold = Double.parseDouble(rawCold);
            poison = Double.parseDouble(rawPoison);
            corrosive = Double.parseDouble(RawCorrosive);
            electric = Double.parseDouble(RawElectric);
            plasma = Double.parseDouble(rawPlasma);
            oblivion = Double.parseDouble(rawOblivion);
        }catch (NumberFormatException ignored){
            return false;
        }

        return DatabaseManager.executeStatement(INSERT_SQL, targetWorld.getDatabaseName(),
                name, AC, pierce, slash, crush, heat, cold, poison,
                corrosive, electric, plasma, oblivion, armorSlot, armorType) > 0;
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
