package world.playerInterface.commands;

import client.Client;
import client.ClientRegistry;
import world.WorldModel;
import world.entity.Attack;
import world.entity.Entity;
import world.entity.StatContainer;
import world.entity.equipment.EquipmentContainer;
import world.item.Item;
import world.item.ItemType;
import world.item.armor.ArmorSlot;
import world.item.weapon.Weapon;
import world.notification.Notification;
import world.notification.NotificationService;
import world.notification.NotificationSubscriber;
import world.room.RoomNotificationScope;

import java.util.List;

import static world.playerInterface.ColorTheme.*;

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
            StatContainer stats = getSourceEntity().getStats();
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

    private void notifyTarget(String message, Entity target) {
        Client targetClient = registry.getClient(target.getID());
        if (targetClient != null) {
            targetClient.sendMessage(message);
        }
    }

    private void notifyRoom(String message, String... excludedEntityIDs) {
        List<Entity> inRoom = getWorldModel().getEntityCollection().getEntitiesInRoom(getSourceEntity().getRoomName(), getSourceEntity().getDatabaseName(), excludedEntityIDs);
        for (Entity ent : inRoom) {
            Client attachedClient = registry.getClient(ent.getID());
            if (attachedClient != null) {
                attachedClient.sendMessage(message);
            }
        }
    }

    private void singleAttack(Weapon selectWeapon, Entity target, StatContainer stats, int bonus, double staminaMultiplier) {
        double rawStamina = selectWeapon.getStaminaUsage(stats.getStrength(), stats.getDexterity()) * staminaMultiplier;
        int staminaNeeded = (int) Math.ceil(rawStamina);
        if (staminaNeeded > getSourceEntity().getPools().getStamina())
            getSourceClient().sendMessage("You are " + getMessageInColor("exhausted", STAMINA_COLOR) + " and cannot wield the " + selectWeapon.getDisplayableName());
        else {
            int roll = selectWeapon.rollHit(
                    stats.getStrength(),
                    stats.getDexterity(),
                    stats.getIntelligence(),
                    stats.getWisdom());
            roll = roll - target.getEquipment().getEquipmentAC();
            roll = roll + bonus;

            int damage = selectWeapon.rollDamage(stats.getStrength(), stats.getDexterity(), stats.getIntelligence(), stats.getWisdom());

            Attack attack = new Attack()
                    .setAttemptedDamage(damage)
                    .setBaseRoll(roll)
                    .setAggressor(getSourceEntity())
                    .setAttackWeapon(selectWeapon)
                    .setDefender(target);

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

    public class AttackNotification extends Notification {
        private Attack attack;

        AttackNotification(Attack attack, ClientRegistry registry) {
            super(registry);
            this.attack = attack;
        }

        @Override
        public String getAsMessage(NotificationSubscriber viewer) {
            Entity viewerEntity = (Entity) viewer;

            Entity attackEntity = attack.getAggressor();
            Entity defenceEntity = attack.getDefender();
            Weapon attackWeapon = attack.getAttackWeapon();
            int netRoll = attack.getBaseRoll();
            int damage = attack.getDamageDealt();

            String hitType = attack.getDidDeflect() ? "glancing" : "direct";


            if (viewer.getID().equals(attackEntity.getID())) {
                if (netRoll >= 0)
                    return String.format("You score a %s hit(%d) on " + getEntityColored(defenceEntity, attackEntity, getWorldModel()) + " with your " + getItemColored(attackWeapon) + " for " + getMessageInColor("%d damage", OUTGOING_DAMAGE), hitType, netRoll, damage);
                else if (attack.getDidDodge())
                    return String.format(getEntityColored(defenceEntity, attackEntity, getWorldModel()) + " dodges(%d) your " + getItemColored(attackWeapon), netRoll);
                else
                    return String.format("You miss(%d) " + getEntityColored(defenceEntity, attackEntity, getWorldModel()) + " with your " + getItemColored(attackWeapon), netRoll);
            } else if (viewer.getID().equals(defenceEntity.getID())) {
                if (netRoll >= 0)
                    return String.format(getEntityColored(attackEntity, defenceEntity, getWorldModel()) + " attacks you. You are hit(%d) with a %s blow with %s " + getItemColored(attackWeapon) + " for " + getMessageInColor("%d damage", INCOMING_DAMAGE), netRoll, hitType, attackEntity.getPossessivePronoun(), damage);
                else if (attack.getDidDodge())
                    return String.format(getMessageInColor(" You dodge(%d) " + getEntityColored(attackEntity, defenceEntity, getWorldModel()) + "'s " + getItemColored(attackWeapon), WARNING), netRoll, attackEntity.getPossessivePronoun());
                else
                    return String.format(getMessageInColor(getEntityColored(attackEntity, defenceEntity, getWorldModel()) + " misses(%d) you with %s " + getItemColored(attackWeapon), WARNING), netRoll, attackEntity.getPossessivePronoun());
            } else {
                return getMessageInColor(getEntityColored(attackEntity, viewerEntity, getWorldModel()) + " is attacking " +
                        getEntityColored(defenceEntity, viewerEntity, getWorldModel()), INFORMATIVE);
            }
        }

        public Attack getAttack(){
            return attack;
        }
    }
}
