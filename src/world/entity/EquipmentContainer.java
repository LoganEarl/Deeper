package world.entity;

import world.item.Item;
import world.item.ItemType;
import world.item.armor.Armor;
import world.item.armor.ArmorSlot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static world.entity.EntityTable.*;
import static world.item.armor.ArmorSlot.*;

public class EquipmentContainer implements Entity.SqlExtender {
    static final String SIGNIFIER = "items";

    private Map<ArmorSlot,Integer> slots = new HashMap<>();

    private Entity entity;

    private static final List<String> HEADERS = Arrays.asList(SLOT_HEAD, SLOT_CHEST, SLOT_LEGS, SLOT_FEET, SLOT_HANDS, SLOT_HAND_LEFT, SLOT_HAND_RIGHT, SLOT_BACK, SLOT_BELT_POUCH, SLOT_BELT_UTIL);

    private static final List<ArmorSlot> SLOTS = Arrays.asList(head, chest, legs, feet, hands, leftHand, rightHand, back, beltPouch, beltUtil);

    EquipmentContainer(){}

    EquipmentContainer(ResultSet readFrom, Entity entity) throws SQLException{
        for(int i = 0; i < HEADERS.size(); i++){
            slots.put(SLOTS.get(i),readFrom.getInt(HEADERS.get(i)));
        }

        this.entity = entity;
    }

    public int getEquipmentAC(){
        int total = 0;
        for(Integer i: slots.values()){
            Item item;
            if( i != null && (item = Item.getItemByID(i,entity.getDatabaseName())) != null){
                if(item.getItemType() == ItemType.armor){
                    total += ((Armor)item).getArmorClass();
                }
            }
        }
        return total;
    }

    /**
     * equips the given piece of armor from either the left or right hand into the fitting slot. If there was already an item in that slot, it will be placed in the hand that equipped it
     * @param armorPiece the armor piece in either the left or right hand
     * @return true if item was equipped
     */
    public boolean equipArmor(Armor armorPiece){
        ArmorSlot sourceSlot = getSlotOfPiece(armorPiece);
        ArmorSlot slotType = armorPiece.getSlot();
        Integer curEquipID = slots.get(slotType);

        if(sourceSlot == leftHand || sourceSlot == rightHand){
            if(curEquipID != null) {
                Item curEquip = Item.getItemByID(curEquipID, entity.getDatabaseName());
                if (curEquip != null)
                    slots.put(sourceSlot, curEquipID);
            }
            slots.put(slotType,armorPiece.getItemID());
            return true;
        }
        return false;
    }

    /**
     * unequipps the given piece of armor if equipped, and places it in a free hand
     * @param armorPiece the piece of armor to remove
     * @return the previously free hand if successful. null if not successful
     */
    public ArmorSlot unequipArmor(Armor armorPiece){
        ArmorSlot freeHand = getFreeHand();
        ArmorSlot sourceSlot = getSlotOfPiece(armorPiece);

        if(freeHand != null && sourceSlot != null){
            slots.put(freeHand,armorPiece.getItemID());
            slots.remove(sourceSlot);
            return freeHand;
        }
        return null;
    }

    private ArmorSlot getSlotOfPiece(Armor armorPiece){
        for(ArmorSlot slot: slots.keySet()){
            Integer equipped = slots.get(slot);
            if(equipped != null && equipped.equals(armorPiece.getItemID())){
                return slot;
            }
        }
        return null;
    }

    private ArmorSlot getFreeHand(){
        if(slots.get(rightHand) == null)
            return rightHand;
        if(slots.get(leftHand) == null)
            return leftHand;
        return null;
    }

    @Override
    public String getSignifier() {
        return SIGNIFIER;
    }

    @Override
    public Object[] getInsertSqlValues() {
        List<Object> values = new ArrayList<>(10);
        for(ArmorSlot slot: SLOTS)
            values.add(slots.get(slot));
        return values.toArray(new Object[0]);
    }

    @Override
    public String[] getSqlColumnHeaders() {
        return HEADERS.toArray(new String[0]);
    }
}
