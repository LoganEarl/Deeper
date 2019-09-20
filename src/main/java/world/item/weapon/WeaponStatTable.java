package main.java.world.item.weapon;

import main.java.database.DatabaseManager;
import main.java.world.item.ItemStatTable;
import main.java.world.meta.World;

import java.util.*;

import static main.java.world.item.Item.NULL_ITEM_NAME;

public class WeaponStatTable implements  DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "weaponStat";

    public static final String ITEM_NAME = ItemStatTable.ITEM_NAME;
    public static final String MIN_BASE_DAMAGE = "minBaseDmg";
    public static final String MAX_BASE_DAMAGE = "maxBaseDmg";
    public static final String BALANCE = "balance";
    /**In attacks per second*/
    public static final String ATTACK_SPEED = "attackSpeed";
    public static final String STR_SCALAR = "strScalar";
    public static final String DEX_SCALAR = "dexScalar";
    public static final String INT_SCALAR = "intScalar";
    public static final String WIS_SCALAR = "wisScalar";
    /**A 2 is a 2% chance of a crit*/
    public static final String CRIT_PERCENT = "critPercent";

    public static final String HIT_BONUS = "hitBonus";
    public static final String SIMPLE_BONUS = "simpleBonus";
    public static final String DAMAGE_TYPE = "damageType";

    private static final String GET_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?", TABLE_NAME, ITEM_NAME);
    private static final String INSERT_SQL = String.format(Locale.US, "INSERT INTO %s(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            TABLE_NAME, ITEM_NAME, MIN_BASE_DAMAGE, MAX_BASE_DAMAGE, BALANCE, ATTACK_SPEED, STR_SCALAR, DEX_SCALAR, INT_SCALAR, WIS_SCALAR, CRIT_PERCENT, HIT_BONUS, SIMPLE_BONUS, DAMAGE_TYPE);

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    private static final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    private static final Set<String> CONSTRAINTS = new HashSet<>(1);

    public WeaponStatTable(){
        TABLE_DEFINITION.put(ITEM_NAME, "VARCHAR(32) PRIMARY KEY NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(MIN_BASE_DAMAGE, "INT");
        TABLE_DEFINITION.put(MAX_BASE_DAMAGE, "INT");
        TABLE_DEFINITION.put(BALANCE, "DECIMAL");
        TABLE_DEFINITION.put(ATTACK_SPEED, "DECIMAL");
        TABLE_DEFINITION.put(STR_SCALAR, "DECIMAL");
        TABLE_DEFINITION.put(DEX_SCALAR, "DECIMAL");
        TABLE_DEFINITION.put(INT_SCALAR, "DECIMAL");
        TABLE_DEFINITION.put(WIS_SCALAR, "DECIMAL");
        TABLE_DEFINITION.put(CRIT_PERCENT, "INT");

        TABLE_DEFINITION.put(HIT_BONUS, "INT");
        TABLE_DEFINITION.put(SIMPLE_BONUS, "INT");
        TABLE_DEFINITION.put(DAMAGE_TYPE, "VARCHAR(16)");

        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ITEM_NAME, ItemStatTable.TABLE_NAME, ItemStatTable.ITEM_NAME));
    }

    public static Map<String,String> getStatsForWeapon(String itemName, String databaseName){
        return ItemStatTable.getStatsForRawItem(itemName,databaseName,GET_SQL,TABLE_DEFINITION.keySet());
    }

    public static boolean existsInWorld(String itemName, World targetWorld){
        return null != getStatsForWeapon(itemName,targetWorld.getDatabaseName());
    }

    public static boolean writeStatsToWorld(Map<String,String> stats, World targetWorld){
        if(existsInWorld(stats.getOrDefault(ITEM_NAME,NULL_ITEM_NAME),targetWorld))
            return false;

        String name = stats.get(ITEM_NAME);
        String rawMin = stats.get(MIN_BASE_DAMAGE);
        String rawMax = stats.get(MAX_BASE_DAMAGE);
        String rawBalance = stats.get(BALANCE);
        String rawSpeed = stats.get(ATTACK_SPEED);
        String rawDex = stats.get(DEX_SCALAR);
        String rawStr = stats.get(STR_SCALAR);
        String rawInt = stats.get(INT_SCALAR);
        String rawWis = stats.get(WIS_SCALAR);
        String rawCrit = stats.get(CRIT_PERCENT);
        String rawBonus = stats.get(HIT_BONUS);
        String rawSimpleBonus = stats.get(SIMPLE_BONUS);
        String damageType = stats.get(DAMAGE_TYPE);

        Double balance, speed, str, dex, intel, wis;
        Integer min, max, bonus, simpleBonus, crit;
        try {
            min = Integer.parseInt(rawMin);
            max = Integer.parseInt(rawMax);
            bonus = Integer.parseInt(rawBonus);
            simpleBonus = Integer.parseInt(rawSimpleBonus);
            crit = Integer.parseInt(rawCrit);

            balance = Double.parseDouble(rawBalance);
            speed = Double.parseDouble(rawSpeed);
            str = Double.parseDouble(rawStr);
            dex = Double.parseDouble(rawDex);
            intel = Double.parseDouble(rawInt);
            wis = Double.parseDouble(rawWis);
        }catch (NumberFormatException ignored){
            return false;
        }

        return DatabaseManager.executeStatement(INSERT_SQL, targetWorld.getDatabaseName(),
                name, min, max, balance, speed, str, dex, intel, wis, crit, bonus, simpleBonus, damageType) > 0;
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
