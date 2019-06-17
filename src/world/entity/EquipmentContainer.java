package world.entity;

import world.item.Item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static world.entity.EntityTable.*;

public class EquipmentContainer implements Entity.SqlExtender {
    static final String SIGNIFIER = "items";

    private Integer headSlotID;
    private Integer chestSlotID;
    private Integer legsSlotID;
    private Integer handsSlotID;
    private Integer leftHandHoldID;
    private Integer rightHandHoldID;
    private Integer backSlotID;
    private Integer beltPouchSlot;
    private Integer beltUtilSlot;

    private static final List<String> HEADERS = Arrays.asList(SLOT_HEAD, SLOT_CHEST, SLOT_LEGS, SLOT_FEET, SLOT_HANDS, SLOT_HAND_LEFT, SLOT_HAND_RIGHT, SLOT_BACK, SLOT_BELT_POUCH, SLOT_BELT_UTIL);

    private Integer[] getSlotsByValue(){
        Integer[] slots = new Integer[HEADERS.size()];
        slots[0] = headSlotID;
        slots[1] = chestSlotID;
        slots[2] = legsSlotID;
        slots[3] = handsSlotID;
        slots[4] = leftHandHoldID;
        slots[5] = rightHandHoldID;
        slots[6] = backSlotID;
        slots[7] = beltPouchSlot;
        slots[8] = beltUtilSlot;
        return slots;
    }

    public EquipmentContainer() {

    }

    public EquipmentContainer(ResultSet readFrom) throws SQLException{
        this.headSlotID = readFrom.getInt(SLOT_HEAD);
        this.chestSlotID = readFrom.getInt(SLOT_CHEST);
        this.legsSlotID = readFrom.getInt(SLOT_LEGS);
        this.handsSlotID = readFrom.getInt(SLOT_HANDS);
        this.leftHandHoldID = readFrom.getInt(SLOT_HAND_LEFT);
        this.rightHandHoldID = readFrom.getInt(SLOT_HAND_RIGHT);
        this.backSlotID = readFrom.getInt(SLOT_BACK);
        this.beltPouchSlot = readFrom.getInt(SLOT_BELT_POUCH);
        this.beltUtilSlot = readFrom.getInt(SLOT_BELT_UTIL);
    }

    public int getEquipmentAC(){
        return 0;
    }

    public void equipArmor(Item armorPiece){
        //if(ItemStatTable.TYPE_ARMOR.equals(armorPiece.getItemType())
    }

    @Override
    public String getSignifier() {
        return SIGNIFIER;
    }

    @Override
    public Object[] getInsertSqlValues() {
        return getSlotsByValue();
    }

    @Override
    public String[] getSqlColumnHeaders() {
        return HEADERS.toArray(new String[0]);
    }
}
