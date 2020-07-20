package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.client.ClientRegistry;
import main.java.world.WorldModel;
import main.java.world.entity.Attack;
import main.java.world.entity.Entity;
import main.java.world.entity.equipment.EquipmentContainer;
import main.java.world.entity.stat.StatValueContainer;
import main.java.world.item.Item;
import main.java.world.item.ItemType;
import main.java.world.item.armor.ArmorSlot;
import main.java.world.item.weapon.Weapon;
import main.java.world.notification.ConcreteNotification;
import main.java.world.notification.NotificationService;
import main.java.world.room.RoomNotificationScope;

import static main.java.world.playerInterface.ColorTheme.*;

public class AttackCommand extends EntityCommand {
    private String targetID;
    private ClientRegistry registry;
    private NotificationService service;

    private int cooldownMs = 0;

    private boolean complete = false;
    private int staminaUsed = 0;

    public AttackCommand(String target, Client sourceClient, WorldModel worldModel) {
        super(sourceClient, worldModel);
        this.registry = worldModel.getRegistry();
        this.service = worldModel.getNotificationService();
        targetID = target;
    }

    @Override
    protected boolean requiresBalance() {
        return true;
    }

    @Override
    protected boolean entityCommandIsComplete() {
        return complete;
    }

    @Override
    protected void executeEntityCommand() {
        Entity target = getWorldModel().getEntityCollection().getEntityByDisplayName(targetID, getSourceEntity().getRoomName(), getSourceEntity().getDatabaseName());

        if (target == null)
            target = getWorldModel().getEntityCollection().getEntityByEntityID(targetID, getSourceEntity().getDatabaseName());
        if (target == null)
            getSourceClient().sendMessage("There is " + getMessageInColor("nothing named " + targetID + " nearby", FAILURE));
        else {
            EquipmentContainer equipment = getSourceEntity().getEquipment();
            StatValueContainer stats = getSourceEntity().getStats().getAugmentedValues();
            Item rightHand = equipment.getEquippedItem(ArmorSlot.rightHand);
            Item leftHand = equipment.getEquippedItem(ArmorSlot.leftHand);

            boolean rightHandCanAttack = rightHand != null && rightHand.getItemType() == ItemType.weapon;
            boolean leftHandCanAttack = leftHand != null && leftHand.getItemType() == ItemType.weapon;
            boolean hasFists = rightHand == null && leftHand == null;

            if (hasFists) {
                getSourceClient().sendMessage("You have " + getMessageInColor("no weapon", FAILURE) + " to attack with");
            } else if (rightHandCanAttack && !leftHandCanAttack) {
                Weapon rightHandWeapon = (Weapon) rightHand;
                singleAttack(rightHandWeapon, target, stats, 0, 1);
                cooldownMs = (int) Math.ceil(rightHandWeapon.getAttackSpeed() * 1000);
            } else if (leftHandCanAttack && !rightHandCanAttack) {
                Weapon leftHandWeapon = (Weapon) leftHand;
                singleAttack(leftHandWeapon, target, stats, 0, 1);
                cooldownMs = (int) Math.ceil(leftHandWeapon.getAttackSpeed() * 1000);
            } else if (rightHandCanAttack && leftHandCanAttack) {
                Weapon rightHandWeapon = (Weapon) rightHand;
                Weapon leftHandWeapon = (Weapon) leftHand;
                singleAttack(rightHandWeapon, target, stats, -10, 1.1);
                singleAttack(leftHandWeapon, target, stats, -20, 1.1);
                float maxWeaponSpeed = rightHandWeapon.getAttackSpeed() > leftHandWeapon.getAttackSpeed() ?
                        rightHandWeapon.getAttackSpeed() : leftHandWeapon.getAttackSpeed();
                cooldownMs = (int) Math.ceil(maxWeaponSpeed * 1500);
            } else if (!rightHandCanAttack && !leftHandCanAttack) {
                getSourceClient().sendMessage("You have " + getMessageInColor("no weapon", FAILURE) + " to attack with");
            }
        }

        complete = true;
    }

    @Override
    protected int getStaminaUsed() {
        return staminaUsed;
    }

    private void singleAttack(Weapon selectWeapon, Entity target, StatValueContainer stats, int bonus, double staminaMultiplier) {
        double rawStamina = selectWeapon.getStaminaUsage(stats.getStrength(), stats.getDexterity()) * staminaMultiplier;
        int staminaNeeded = (int) Math.ceil(rawStamina);
        if (staminaNeeded > getSourceEntity().getPools().getCurrentValues().getStamina())
            getSourceClient().sendMessage("You are " + getMessageInColor("exhausted", STAMINA_COLOR) + " and cannot wield the " + selectWeapon.getDisplayableName());
        else {
            Attack attack = getSourceEntity().produceAttackWithWeapon(selectWeapon, bonus);

            attack
                    .setAggressor(getSourceEntity())
                    .setAttackWeapon(selectWeapon)
                    .setDamageType(selectWeapon.getDamageType())
                    .setDefender(target);

            attack = getSourceEntity().modifyOutgoingAttack(attack);
            attack = target.modifyIncomingAttack(attack);

            attack = target.receiveAttack(attack);

            AttackNotification notification = new AttackNotification(attack, registry);
            service.notify(notification, new RoomNotificationScope(getSourceEntity().getRoomName(), getSourceEntity().getDatabaseName()));
            staminaUsed += staminaNeeded;
        }
    }

    @Override
    protected void setBalance() {
        if (cooldownMs > 0)
            getSourceEntity().setBalanceTime(cooldownMs, getSourceClient());
    }

    public class AttackNotification extends ConcreteNotification {
        private Attack attack;

        AttackNotification(Attack attack, ClientRegistry registry) {
            super(registry);
            this.attack = attack;
        }

        @Override
        public String getAsMessage(Entity viewer) {
            Entity attackEntity = attack.getAggressor();
            Entity defenceEntity = attack.getDefender();
            Weapon attackWeapon = attack.getAttackWeapon();
            int netRoll = attack.getBaseRoll();
            int damage = attack.getDamageDealt();

            String hitType = attack.getDidDeflect() ? "glancing" : "direct";

            if (viewer.equals(attackEntity)) {
                if (attack.getDidDodge())
                    return String.format(getEntityColored(defenceEntity, attackEntity, getWorldModel()) + " dodges(%d) your " + getItemColored(attackWeapon), netRoll);
                else if (netRoll >= 0)
                    return String.format("You score a %s hit(%d) on " + getEntityColored(defenceEntity, attackEntity, getWorldModel()) + " with your " + getItemColored(attackWeapon) + " for " + getMessageInColor("%d damage", OUTGOING_DAMAGE), hitType, netRoll, damage);
                else
                    return String.format("You miss(%d) " + getEntityColored(defenceEntity, attackEntity, getWorldModel()) + " with your " + getItemColored(attackWeapon), netRoll);
            } else if (viewer.equals(defenceEntity)) {
                if (attack.getDidDodge())
                    return String.format(getMessageInColor(" You dodge(%d) " + getEntityColored(attackEntity, defenceEntity, getWorldModel()) + "'s " + getItemColored(attackWeapon), WARNING), netRoll, attackEntity.getPossessivePronoun());
                else if (netRoll >= 0)
                    return String.format(getEntityColored(attackEntity, defenceEntity, getWorldModel()) + " attacks you. You are hit(%d) with a %s blow with %s " + getItemColored(attackWeapon) + " for " + getMessageInColor("%d damage", INCOMING_DAMAGE), netRoll, hitType, attackEntity.getPossessivePronoun(), damage);
                else
                    return String.format(getMessageInColor(getEntityColored(attackEntity, defenceEntity, getWorldModel()) + " misses(%d) you with %s " + getItemColored(attackWeapon), WARNING), netRoll, attackEntity.getPossessivePronoun());
            } else {
                return getMessageInColor(getEntityColored(attackEntity, viewer, getWorldModel()) + " is attacking " +
                        getEntityColored(defenceEntity, viewer, getWorldModel()), INFORMATIVE);
            }
        }

        public Attack getAttack() {
            return attack;
        }
    }
}
