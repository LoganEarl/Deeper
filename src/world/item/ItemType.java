package world.item;

import java.sql.ResultSet;

public enum ItemType {
    weapon, armor, consumable, ammo, container, misc;

    public static ItemType extractFromResultSet(ResultSet readFrom) {
        try{
            return valueOf(readFrom.getString(ItemStatTable.ITEM_TYPE));
        }catch (Exception e){
            System.out.println("An item had an un-parsable type and defaulted to misc");
            return misc;
        }
    }
}
