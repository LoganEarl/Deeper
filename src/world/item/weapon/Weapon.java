package world.item.weapon;

import world.item.DamageType;
import world.item.Item;
import world.item.ItemFactory;
import world.item.ItemType;

import java.sql.ResultSet;
import java.util.Map;
import java.util.Random;

import static world.item.weapon.WeaponStatTable.*;

public class Weapon extends Item {
    private static final Random  rnd = new Random();

    public Weapon(ResultSet fromEntry, String databaseName) throws Exception{
        super(fromEntry, databaseName);
    }

    @Override
    public ItemType getItemType() {
        return ItemType.weapon;
    }

    @Override
    protected Map<String, String> getDerivedStats() {
        return WeaponStatTable.getStatsForWeapon(getItemName(),getDatabaseName());
    }

    public int getStaminaUsage(){
        double weight = super.getWeight();
        //1 kg = 5
        //5 kg = 25

        //~40dex, ~30str @ 1kg: 120 stam = 24 swings
        //~40dex, ~30str @ 5kg: 120 stam = ~5 swings
        //~30dex, ~40str @ 1kg: 70 stam  = 14 swings
        //~30dex, ~40str & 5kg: 70 stam  = ~3 swings
    }

    public int rollDamage(int str, int dex, int intel, int wis){
        int dmgRange = getMaxBaseDamage() - getMinBaseDamage();

        int rawDmg = rnd.nextInt(dmgRange) + getMinBaseDamage();

        float strAddition = str * getStrScalar();
        float dexAddition = dex * getDexScalar();
        float intAddition = intel * getIntScalar();
        float wisAddition = wis * getWisScalar();

        rawDmg += Math.floor(strAddition + dexAddition + intAddition + wisAddition);
        return rawDmg;
    }

    public int getMinBaseDamage(){
        initStats();
        return getCastInt(WeaponStatTable.MIN_BASE_DAMAGE);
    }

    public int getMaxBaseDamage(){
        initStats();
        return getCastInt(WeaponStatTable.MAX_BASE_DAMAGE);
    }

    public float getStrScalar(){
        initStats();
        return getCastFloat(STR_SCALAR);
    }

    public float getDexScalar() {
        initStats();
        return getCastFloat(DEX_SCALAR);
    }

    public float getIntScalar() {
        initStats();
        return getCastFloat(INT_SCALAR);
    }

    public float getWisScalar() {
        initStats();
        return getCastFloat(WIS_SCALAR);
    }

    public int getHitBonus() {
        initStats();
        return getCastInt(HIT_BONUS);
    }

    public DamageType getDamageType() {
        initStats();
        try {
            return DamageType.valueOf(getCastString(DAMAGE_TYPE));
        }catch (Exception ignored){}
        return DamageType.slash;
    }

    private static ItemFactory.ItemParser parser = new ItemFactory.ItemParser() {
        @Override
        public ItemType getAssociatedType() {
            return ItemType.weapon;
        }

        @Override
        public Item parseFromResultSet(ResultSet fromEntry, String databaseName) throws Exception {
            return new Weapon(fromEntry,databaseName);
        }
    };

    public static ItemFactory.ItemParser factory(){
        return parser;
    }
}
