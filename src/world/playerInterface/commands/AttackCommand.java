package world.playerInterface.commands;

import client.Client;
import client.ClientRegistry;
import world.entity.Entity;
import world.entity.EquipmentContainer;
import world.entity.StatContainer;
import world.item.Item;
import world.item.ItemType;
import world.item.armor.ArmorSlot;
import world.item.weapon.Weapon;

import java.util.List;

public class AttackCommand extends EntityCommand {
    private String targetID;
    private ClientRegistry registry;

    private int cooldownMs = 0;

    private boolean complete = false;

    public AttackCommand(String target, Client sourceClient, ClientRegistry registry) {
        super(sourceClient);
        this.registry = registry;
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
            getSourceClient().sendMessage("There is nothing named " + targetID + " nearby");
        else{
            EquipmentContainer equipment = getSourceEntity().getEquipment();
            StatContainer stats = getSourceEntity().getStats();
            Item rightHand = equipment.getEquippedItem(ArmorSlot.rightHand);
            Item leftHand = equipment.getEquippedItem(ArmorSlot.leftHand);

            boolean rightHandCanAttack = rightHand != null && rightHand.getItemType() == ItemType.weapon;
            boolean leftHandCanAttack = leftHand != null && leftHand.getItemType() == ItemType.weapon;
            boolean hasFists = rightHand == null && leftHand == null;

            if(hasFists){
                getSourceClient().sendMessage("You have no weapon to attack with");
            }else if(rightHandCanAttack && !leftHandCanAttack){
                Weapon rightHandWeapon = (Weapon) rightHand;
                singleAttack(rightHandWeapon,target,stats,0);
                cooldownMs = (int)Math.ceil(rightHandWeapon.getAttackSpeed() * 1000);
            }else if(leftHandCanAttack && !rightHandCanAttack){
                Weapon leftHandWeapon = (Weapon) leftHand;
                singleAttack(leftHandWeapon,target,stats,0);
                cooldownMs = (int)Math.ceil(leftHandWeapon.getAttackSpeed() * 1000);
            }else if(rightHandCanAttack && leftHandCanAttack){
                Weapon rightHandWeapon = (Weapon) rightHand;
                Weapon leftHandWeapon = (Weapon) leftHand;
                singleAttack(rightHandWeapon,target,stats,-10);
                singleAttack(leftHandWeapon,target,stats,-20);
                float maxWeaponSpeed = rightHandWeapon.getAttackSpeed() > leftHandWeapon.getAttackSpeed()?
                        rightHandWeapon.getAttackSpeed(): leftHandWeapon.getAttackSpeed();
                cooldownMs = (int)Math.ceil(maxWeaponSpeed * 1500);
            }else if(!rightHandCanAttack && !leftHandCanAttack){
                getSourceClient().sendMessage("You have no weapon to attack with");
            }
        }

        complete = true;
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

    private void singleAttack(Weapon selectWeapon, Entity target, StatContainer stats, int bonus){
        int roll = selectWeapon.rollHit(
                stats.getStrength(),
                stats.getDexterity(),
                stats.getIntelligence(),
                stats.getWisdom());
        roll = roll - target.getEquipment().getEquipmentAC();
        roll = roll + bonus;
        notifyRoom(String.format("%s the %s is fighting %s the %s",
                getSourceEntity().getDisplayName(), getSourceEntity().getRace().getDisplayName(),
                target.getDisplayName(), target.getRace().getDisplayName()),
                getSourceEntity().getID(), target.getID());
        if(roll >= 0) {
            int damage = selectWeapon.rollDamage( stats.getStrength(), stats.getDexterity(), stats.getIntelligence(), stats.getWisdom());
            target.getPools().damage(damage);
            getSourceClient().sendMessage(String.format("You score a hit on %s with your %s(+%d) for %d damage",
                    target.getDisplayName(),selectWeapon.getDisplayableName(), roll, damage));
            notifyTarget(String.format("%s the %s attacks you with a %s for %d damage",
                    getSourceEntity().getDisplayName(), getSourceEntity().getRace().getDisplayName(), selectWeapon.getDisplayableName(), damage),
                    target);
        }else{
            getSourceClient().sendMessage(String.format("You miss %s with your %s(%d)",
                    target.getDisplayName(),selectWeapon.getDisplayableName(), roll));
            notifyTarget(String.format("%s the %s misses you with his %s",
                    getSourceEntity().getDisplayName(), getSourceEntity().getRace().getDisplayName(), selectWeapon.getDisplayableName()),
                    target);
        }
    }

    @Override
    protected void setBalance() {
        if(cooldownMs > 0)
            getSourceEntity().setBalanceTime(cooldownMs,getSourceClient());
    }
}
