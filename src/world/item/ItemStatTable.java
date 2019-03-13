package world.item;

import database.DatabaseManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the definition for a SQL table that holds the stats for different types of items. Does not hold the
 * specific instances of each item, only the stats.
 * @author Logan Earl
 */
public class ItemStatTable implements DatabaseManager.DatabaseTable {
    /**The name of the table used to store item stats*/
    public static final String TABLE_NAME = "itemStats";

    /**The name of the item. Used as a unique identifier*/
    public static final String ITEM_NAME = "itemName";
    /**The description of the item*/
    public static final String ITEM_DESCRIPTION = "itemDescription";
    /**The weight of the item in KGs*/
    public static final String WEIGHT = "weightKgs";
    /**The size of the item in Liters*/
    public static final String SIZE = "size";

    /**The minimum damage roll, inclusive. Ignored if itemType is not {@value TYPE_WEAPON}*/
    public static final String MIN_DAMAGE = "minDmg";
    /**The maximum damage roll, inclusive. Ignored if itemType is not {@value TYPE_WEAPON}*/
    public static final String MAX_DAMAGE = "maxDmg";
    /**The addition to the chance to initially hit the target, similar to the D&D model. Ignored if itemType is not {@value TYPE_WEAPON}*/
    public static final String HIT_CHANCE = "hitChance";    //d10 + hitChance against AC
    /**The amount of AC contributed by the item. Ignored if itemType is not {@value TYPE_ARMOR}*/
    public static final String ARMOR_CLASS = "armorClass";

    /**The amount of HP regained by using the item. Ignored if itemType is not {@value TYPE_CONSUMABLE}*/
    public static final String HP_REGEN = "hpRegen";
    /**The amount of MP regained by using the item. Ignored if itemType is not {@value TYPE_CONSUMABLE}*/
    public static final String MP_REGEN = "mpRegen";

    /**The type of the item. Must be one of the TYPE_* constants defined in this class*/
    public static final String ITEM_TYPE = "itemType";

    /**Denotes that the item is a type of weapon*/
    public static final String TYPE_WEAPON = "weapon";
    /**Denotes that the item is a type of armor*/
    public static final String TYPE_ARMOR = "armor";
    /**Denotes that the item is a type of ammo*/
    public static final String TYPE_AMMO = "ammo";
    /**Denotes that the item is a type of consumable*/
    public static final String TYPE_CONSUMABLE = "consumable";
    /**Denotes that the item is of no particular type*/
    public static final String TYPE_MISC = "misc";

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    public final Map<String, String> TABLE_DEFINITION = new HashMap<>();

    public ItemStatTable(){
        TABLE_DEFINITION.put(ITEM_NAME, "VARCHAR(32) PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(ITEM_DESCRIPTION, "TEXT");
        TABLE_DEFINITION.put(WEIGHT,"DECIMAL");
        TABLE_DEFINITION.put(SIZE,"DECIMAL");
        TABLE_DEFINITION.put(MIN_DAMAGE,"INT");
        TABLE_DEFINITION.put(MAX_DAMAGE,"INT");
        TABLE_DEFINITION.put(HIT_CHANCE,"INT");
        TABLE_DEFINITION.put(ARMOR_CLASS,"INT");
        TABLE_DEFINITION.put(HP_REGEN,"INT");
        TABLE_DEFINITION.put(MP_REGEN,"INT");
        TABLE_DEFINITION.put(ITEM_TYPE,"VARCHAR(16)");
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
