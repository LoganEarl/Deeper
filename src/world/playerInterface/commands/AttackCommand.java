package world.playerInterface.commands;

import client.Client;
import client.ClientRegistry;
import world.entity.Entity;
import world.entity.equipment.EquipmentContainer;
import world.entity.StatContainer;
import world.item.Item;
import world.item.ItemType;
import world.item.armor.ArmorSlot;
import world.item.weapon.Weapon;
import world.notification.Notification;
import world.notification.NotificationService;
import world.notification.NotificationSubscriber;
import world.room.RoomNotificationScope;

import static world.playerInterface.ColorTheme.*;

import java.util.List;

public class AttackCommand extends EntityCommand {
    private String targetID;
    private ClientRegistry registry;
    private NotificationService service;

    private int cooldownMs = 0;

    private boolean complete = false;
    private int staminaUsed = 0;

    public AttackCommand(String target, Client sourceClient, ClientRegistry registry, NotificationService service) {
        super(sourceClient);
        this.registry = registry;
        this.service = service;
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
        Entity target = Entity.getEntityByDisplayName(targetID, getSourceEntity().getRoomName(),getSourceEntity().getDatabaseName());

        if(target == null)
            target = Entity.getEntityByEntityID(targetID,getSourceEntity().getDatabaseName());
        if(target == null)
            getSourceClient().sendMessage("There is " + getMessageInColor("nothing named " + targetID + " nearby",FAILURE));
        else{
            EquipmentContainer equipment = getSourceEntity().getEquipment();
            StatContainer stats = getSourceEntity().getStats();
            Item rightHand = equipment.getEquippedItem(ArmorSlot.rightHand);
            Item leftHand = equipment.getEquippedItem(ArmorSlot.leftHand);

            boolean rightHandCanAttack = rightHand != null && rightHand.getItemType() == ItemType.weapon;
            boolean leftHandCanAttack = leftHand != null && leftHand.getItemType() == ItemType.weapon;
            boolean hasFists = rightHand == null && leftHand == null;

            if(hasFists){
                getSourceClient().sendMessage("You have " + getMessageInColor("no weapon",FAILURE) + " to attack with");
            }else if(rightHandCanAttack && !leftHandCanAttack){
                Weapon rightHandWeapon = (Weapon) rightHand;
                singleAttack(rightHandWeapon,target,stats,0, 1);
                cooldownMs = (int)Math.ceil(rightHandWeapon.getAttackSpeed() * 1000);
            }else if(leftHandCanAttack && !rightHandCanAttack){
                Weapon leftHandWeapon = (Weapon) leftHand;
                singleAttack(leftHandWeapon,target,stats,0,1);
                cooldownMs = (int)Math.ceil(leftHandWeapon.getAttackSpeed() * 1000);
            }else if(rightHandCanAttack && leftHandCanAttack){
                Weapon rightHandWeapon = (Weapon) rightHand;
                Weapon leftHandWeapon = (Weapon) leftHand;
                singleAttack(rightHandWeapon,target,stats,-10,1.1);
                singleAttack(leftHandWeapon,target,stats,-20, 1.1);
                float maxWeaponSpeed = rightHandWeapon.getAttackSpeed() > leftHandWeapon.getAttackSpeed()?
                        rightHandWeapon.getAttackSpeed(): leftHandWeapon.getAttackSpeed();
                cooldownMs = (int)Math.ceil(maxWeaponSpeed * 1500);
            }else if(!rightHandCanAttack && !leftHandCanAttack){
                getSourceClient().sendMessage("You have " + getMessageInColor("no weapon",FAILURE) + " to attack with");
            }
        }

        complete = true;
    }

    @Override
    protected int getStaminaUsed() {
        return staminaUsed;
    }

    private void notifyTarget(String message, Entity target){
        Client targetClient = registry.getClientWithUsername(target.getID());
        if(targetClient != null){
            targetClient.sendMessage(message);
        }
    }

    private void notifyRoom(String message, String... excludedEntityIDs){
        List<Entity> inRoom = Entity.getEntitiesInRoom(getSourceEntity().getRoomName(), getSourceEntity().getDatabaseName(), excludedEntityIDs);
        for(Entity ent: inRoom){
            Client attachedClient = registry.getClientWithUsername(ent.getID());
            if(attachedClient != null){
                attachedClient.sendMessage(message);
            }
        }
    }

    private void singleAttack(Weapon selectWeapon, Entity target, StatContainer stats, int bonus, double staminaMultiplier){
        double rawStamina = selectWeapon.getStaminaUsage(stats.getStrength(), stats.getDexterity()) * staminaMultiplier;
        int staminaNeeded = (int)Math.ceil(rawStamina);
        if(staminaNeeded > getSourceEntity().getPools().getStamina())
            getSourceClient().sendMessage("You are " + getMessageInColor("exhausted",STAMINA_COLOR) + " and cannot wield the " + selectWeapon.getDisplayableName());
        else {
            int roll = selectWeapon.rollHit(
                    stats.getStrength(),
                    stats.getDexterity(),
                    stats.getIntelligence(),
                    stats.getWisdom());
            roll = roll - target.getEquipment().getEquipmentAC();
            roll = roll + bonus;

            int damage = 0;
            if (roll >= 0) {
                damage = selectWeapon.rollDamage(stats.getStrength(), stats.getDexterity(), stats.getIntelligence(), stats.getWisdom());
                target.getPools().damage(damage, getSourceEntity(), roll);
            }
            AttackNotification notification = new AttackNotification(getSourceEntity(), target, selectWeapon, roll, damage, registry);
            service.notify(notification, new RoomNotificationScope(getSourceEntity().getRoomName(), getSourceEntity().getDatabaseName()));
            staminaUsed += staminaNeeded;
        }
    }

    @Override
    protected void setBalance() {
        if(cooldownMs > 0)
            getSourceEntity().setBalanceTime(cooldownMs,getSourceClient());
    }

    public class AttackNotification extends Notification {
        private Entity attackEntity;
        private Entity defenceEntity;
        private Weapon attackWeapon;
        private int netRoll;
        private int damage;

        public AttackNotification(Entity attackEntity,Entity defenceEntity,Weapon attackWeapon,int netRoll,int damage,ClientRegistry registry) {
            super(registry);
            this.attackEntity = attackEntity;
            this.defenceEntity = defenceEntity;
            this.attackWeapon = attackWeapon;
            this.netRoll = netRoll;
            this.damage = damage;
        }

        @Override
        public String getAsMessage(NotificationSubscriber viewer) {
            if(viewer.getID().equals(attackEntity.getID()) && viewer.getDatabaseName().equals(attackEntity.getDatabaseName())){
                if(netRoll >= 0)
                    return String.format("You score a hit on %s with your %s(+%d) for " + getMessageInColor("%d damage", OUTGOING_DAMAGE),
                            defenceEntity.getDisplayName(), attackWeapon.getDisplayableName(), netRoll, damage);
                else
                    return String.format("You miss %s with your %s(%d)",
                            defenceEntity.getDisplayName(),attackWeapon.getDisplayableName(), netRoll);
            }else if(viewer.getID().equals(defenceEntity.getID()) && viewer.getDatabaseName().equals(attackEntity.getDatabaseName())){
                if(netRoll >= 0)
                    return String.format("%s the %s attacks you with a %s for " + getMessageInColor("%d damage", INCOMING_DAMAGE),
                            attackEntity.getDisplayName(), attackEntity.getRace().getDisplayName(), attackWeapon.getDisplayableName(), damage);
                else
                    return String.format(getMessageInColor("%s the %s misses", WARNING) + " you with his %s",
                            attackEntity.getDisplayName(), attackEntity.getRace().getDisplayName(), attackWeapon.getDisplayableName());
            }else{
                return String.format("%s the %s is fighting %s the %s",
                        attackEntity.getDisplayName(), attackEntity.getRace().getDisplayName(),
                        defenceEntity.getDisplayName(), defenceEntity.getRace().getDisplayName());
            }

        }

        public Entity getAttackEntity() {
            return attackEntity;
        }

        public Entity getDefenceEntity() {
            return defenceEntity;
        }

        public Weapon getAttackWeapon() {
            return attackWeapon;
        }

        public int getNetRoll() {
            return netRoll;
        }

        public int getDamage() {
            return damage;
        }
    }
}
