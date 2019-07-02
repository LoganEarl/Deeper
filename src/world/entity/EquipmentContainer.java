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

    private Map<ArmorSlot, Integer> slots = new HashMap<>();

    private Entity entity;

    private static final List<String> HEADERS = Arrays.asList(SLOT_HEAD, SLOT_CHEST, SLOT_LEGS, SLOT_FEET, SLOT_HANDS, SLOT_HAND_LEFT, SLOT_SHEATH_LEFT,  SLOT_HAND_RIGHT, SLOT_SHEATH_RIGHT, SLOT_BACK, SLOT_BELT_POUCH, SLOT_BELT_UTIL);

    private static final List<ArmorSlot> SLOTS = Arrays.asList(head, chest, legs, feet, hands, leftHand, leftSheath, rightHand, rightSheath, back, beltPouch, beltUtil);

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_CONTAINER_FULL = -1;
    public static final int CODE_TOO_HEAVY = -2;
    public static final int CODE_NOT_NEAR = -3;
    public static final int CODE_NO_ITEM = -4;
    public static final int CODE_WRONG_TYPE = -5;
    public static final int CODE_ERROR = -100;

    EquipmentContainer() {
    }

    EquipmentContainer(ResultSet readFrom, Entity entity) throws SQLException {
        for (int i = 0; i < HEADERS.size(); i++) {
            slots.put(SLOTS.get(i), readFrom.getInt(HEADERS.get(i)));
        }

        this.entity = entity;
    }

    public int getEquipmentAC() {
        int total = 0;
        for (Integer i : slots.values()) {
            Item item;
            if (i != null && (item = Item.getItemByID(i, entity.getDatabaseName())) != null) {
                if (item.getItemType() == ItemType.armor) {
                    total += ((Armor) item).getArmorClass();
                }
            }
        }
        return total;
    }

    public double getEquipmentWeight() {
        double total = 0;
        for (Integer i: slots.values()){
            Item item;
            if (i != null && (item = Item.getItemByID(i, entity.getDatabaseName())) != null) {
                total += item.getWeight();
            }
        }
        return total;
    }

    public Item getEquippedItem(ArmorSlot selectedSlot){
        Integer itemID = slots.get(selectedSlot);
        if(itemID == null) return null;
        return Item.getItemByID(itemID, entity.getDatabaseName());
    }

    public Item getEquippedItem(int itemID){
        for(Integer slotItemID : slots.values()){
            if(slotItemID != null && slotItemID.equals(itemID)){
                Item equipped = Item.getItemByID(slotItemID, entity.getDatabaseName());
                if(equipped != null)
                    return equipped;
            }
        }
        return null;
    }

    public Item getEquippedItem(String itemName){
        for(Integer itemID : slots.values()){
            if(itemID != null){
                Item equipped = Item.getItemByID(itemID, entity.getDatabaseName());
                if(equipped != null && equipped.getItemName().equals(itemName))
                    return equipped;
            }
        }
        return null;
    }

    public boolean hasItemEquipped(Item equipped) {
        for (Integer itemID : slots.values())
            if (itemID != null &&
                    itemID.equals(equipped.getItemID()) &&
                    entity.getDatabaseName().equals(equipped.getDatabaseName()))
                return true;
        return false;
    }

    /**
     * equips the given piece of armor from either the left or right hand into the fitting slot. If there was already an item in that slot, it will be placed in the hand that equipped it
     *
     * @param armorPiece the armor piece in either the left or right hand
     * @return true if item was equipped
     */
    public boolean equipArmor(Armor armorPiece) {
        ArmorSlot sourceSlot = getSlotOfItem(armorPiece);
        ArmorSlot slotType = armorPiece.getSlot();
        Integer curEquipID = slots.get(slotType);

        if (sourceSlot == leftHand || sourceSlot == rightHand) {
            if (curEquipID != null) {
                Item curEquip = Item.getItemByID(curEquipID, entity.getDatabaseName());
                if (curEquip != null)
                    slots.put(sourceSlot, curEquipID);
            }
            slots.put(slotType, armorPiece.getItemID());
            return true;
        }
        return false;
    }

    public int holdItem(Item toHold){
        int holdCode;
        if((holdCode = canHoldItem(toHold)) != CODE_SUCCESS)
            return holdCode;

        ArmorSlot freeHand = getFreeHand();
        slots.put(freeHand, toHold.getItemID());
        toHold.setRoomName("");
        return CODE_SUCCESS;
    }

    public int dropItem(Item toDrop){
        if(!hasItemEquipped(toDrop))
            return CODE_NO_ITEM;
        ArmorSlot holdingSlot = getSlotOfItem(toDrop);
        if(holdingSlot == rightHand || holdingSlot == leftHand){
            slots.remove(holdingSlot);
            toDrop.setRoomName(entity.getRoomName());
            return CODE_SUCCESS;
        }
        return CODE_ERROR;
    }

    public boolean isHoldingItem(Item item){
        ArmorSlot holdingSlot = getSlotOfItem(item);
        return (holdingSlot == rightHand || holdingSlot == leftHand);
    }

    public boolean hasFreeHand(){
        return getFreeHand() != null;
    }

    public boolean isEncumbered(){
        return entity.getStats().getWeightSoftLimit() < getEquipmentWeight();
    }

    /**
     * will stow the weapon in the given hand slot into its sheath/holster slot
     * @param handSlot wither ArmorSlot.rightHand or ArmorSlot.leftHand
     * @return one of the CODE_* constants. {@link #CODE_SUCCESS} if successful
     */
    public int stowWeapon(ArmorSlot handSlot){
        if(handSlot != leftHand && handSlot != rightHand)
            return CODE_ERROR;

        Integer handID = slots.get(leftHand);
        if(handID == null)
            return CODE_NO_ITEM;

        if(slots.get(leftSheath) != null)
            return CODE_CONTAINER_FULL;

        Item leftWeapon = Item.getItemByID(handID,entity.getDatabaseName());
        if(leftWeapon == null)
            return CODE_NO_ITEM;
        if(leftWeapon.getItemType() != ItemType.weapon)
            return CODE_WRONG_TYPE;

        slots.put(rightSheath,leftWeapon.getItemID());
        slots.remove(rightHand);
        return CODE_SUCCESS;
    }

    /**
     * determines if it is possible to hold the given item in a free hand
     * @param toHold the item to hold
     * @return one of teh CODE_* constants. CODE_SUCCESS if able to hold
     */
    public int canHoldItem(Item toHold){
        if(!hasFreeHand())
            return CODE_CONTAINER_FULL;
        if(toHold.getWeight() + getEquipmentWeight() > entity.getStats().getWeightHardLimit())
            return CODE_TOO_HEAVY;
        if(!toHold.getRoomName().equals(entity.getRoomName())){
            return CODE_NOT_NEAR;
        }
        return CODE_SUCCESS;
    }

    /**
     * unequipps the given piece of armor if equipped, and places it in a free hand
     *
     * @param armorPiece the piece of armor to remove
     * @return the previously free hand if successful. null if not successful
     */
    public ArmorSlot unequipArmor(Armor armorPiece) {
        ArmorSlot freeHand = getFreeHand();
        ArmorSlot sourceSlot = getSlotOfItem(armorPiece);

        if (freeHand != null && sourceSlot != null) {
            slots.put(freeHand, armorPiece.getItemID());
            slots.remove(sourceSlot);
            return freeHand;
        }
        return null;
    }

    private ArmorSlot getSlotOfItem(Item item) {
        for (ArmorSlot slot : slots.keySet()) {
            Integer equipped = slots.get(slot);
            if (equipped != null && equipped.equals(item.getItemID())) {
                return slot;
            }
        }
        return null;
    }

    private ArmorSlot getFreeHand() {
        if (slots.get(rightHand) == null)
            return rightHand;
        if (slots.get(leftHand) == null)
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
        for (ArmorSlot slot : SLOTS)
            values.add(slots.get(slot));
        return values.toArray(new Object[0]);
    }

    @Override
    public String[] getSqlColumnHeaders() {
        return HEADERS.toArray(new String[0]);
    }
}
