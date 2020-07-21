package main.java.world.item.armor;

//TODO set up a table for this with foreign keys
public enum ArmorSlot {
    head, chest, legs, feet, hands, leftHand, leftSheath, rightHand, rightSheath, back, beltPouch, beltUtil;

    public static final String TABLE_NAME = "armorSlot";
}
