package world.item.armor;

import world.item.DamageType;
import world.item.Item;
import world.item.ItemFactory;
import world.item.ItemType;
import world.item.container.Container;
import world.meta.World;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import static world.item.armor.ArmorStatTable.*;

public class Armor extends Item {
    private static Map<DamageType, String> mapDamageTypes(){
        Map<DamageType,String> bindings = new HashMap<>();
        bindings.put(DamageType.cold, COLD_DEFENCE);
        bindings.put(DamageType.corrosive, CORROSIVE_DEFENCE);
        bindings.put(DamageType.crush, CRUSH_DEFENCE);
        bindings.put(DamageType.electric, ELECTRIC_DEFENCE);
        bindings.put(DamageType.heat, HEAT_DEFENCE);
        bindings.put(DamageType.oblivion, OBLIVION_DEFENCE);
        bindings.put(DamageType.pierce, PIERCE_DEFENCE);
        bindings.put(DamageType.plasma, PLASMA_DEFENCE);
        bindings.put(DamageType.poison, POISON_DEFENCE);
        bindings.put(DamageType.slash, SLASH_DEFENCE);
        return bindings;
    }
    private static Map<DamageType, String> defenceColumnBindings = mapDamageTypes();

    protected Armor(ResultSet entry, ItemFactory factory, String databaseName) throws Exception {
        super(entry, factory, databaseName);
    }

    @Override
    protected Map<String, String> getDerivedStats() {
        return ArmorStatTable.getStatsForArmor(getItemName(),getDatabaseName());
    }

    @Override
    public ItemType getItemType() {
        return ItemType.armor;
    }

    public int getArmorClass(){
        initStats();
        return getCastInt(ARMOR_CLASS);
    }

    @Override
    protected boolean compositeStatsExistInWorld(World targetWorld) {
        initStats();
        return ArmorStatTable.existsInWorld(getItemName(),targetWorld);
    }

    @Override
    protected boolean writeCompositeStatsToWorld(World targetWorld) {
        initStats();
        return ArmorStatTable.writeStatsToWorld(getDerivedStats(), targetWorld);
    }

    /**
     * defence is a percentage reduction. So a .2 would mean a 20% reduction
     * to that damage type. -.2 would be a 20% increase, representing a vulnerability to the damage type
     * @param damageType the type of damage to check
     * @return the percentage of damage to subtract
     */
    public float getDefenceForDamageType(DamageType damageType){
        initStats();
        return getCastFloat(defenceColumnBindings.get(damageType));
    }

    public ArmorType getType(){
        initStats();
        String rawArmorType = "";
        try{
            return ArmorType.valueOf(rawArmorType = getCastString(ARMOR_TYPE));
        }catch (EnumConstantNotPresentException e){
            System.out.println("Unable to parse armor type:" + rawArmorType + " of item " + getItemName());
            return ArmorType.light;
        }
    }

    public ArmorSlot getSlot(){
        initStats();
        String rawArmorSlot = "";
        try{
            return ArmorSlot.valueOf(rawArmorSlot = getCastString(ARMOR_SLOT));
        }catch (EnumConstantNotPresentException e){
            System.out.println("Unable to parse armor type:" + rawArmorSlot + " of item " + getItemName());
            return ArmorSlot.chest;
        }
    }

    private static ItemFactory.ItemParser parser = new ItemFactory.ItemParser() {
        @Override
        public ItemType getAssociatedType() {
            return ItemType.armor;
        }

        @Override
        public Item parseFromResultSet(ResultSet fromEntry, ItemFactory sourceFactory, String databaseName) throws Exception {
            return new Armor(fromEntry, sourceFactory, databaseName);
        }
    };

    public static ItemFactory.ItemParser factory() {
        return parser;
    }
}
