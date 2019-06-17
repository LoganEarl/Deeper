package world.item.weapon;

import world.item.Item;
import world.item.ItemFactory;
import world.item.ItemType;

import java.sql.ResultSet;
import java.util.Map;

import static world.item.weapon.WeaponStatTable.*;

public class Weapon extends Item {
    public Weapon(ResultSet fromEntry, String databaseName) throws Exception{
        super(fromEntry, databaseName);
    }

    @Override
    protected Map<String, String> getDerivedStats() {
        return WeaponStatTable.getStatsForWeapon(getItemName(),getDatabaseName());
    }

    public enum DamageType{
        pierce, slash, crush, heat, cold, corrosive, electric, plasma, oblivion
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
