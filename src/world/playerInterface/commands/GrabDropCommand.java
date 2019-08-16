package world.playerInterface.commands;

import client.Client;
import client.ClientRegistry;
import world.WorldModel;
import world.diplomacy.DiplomaticRelation;
import world.diplomacy.Faction;
import world.entity.Entity;
import world.entity.equipment.EquipmentContainer;
import world.item.Item;
import world.item.ItemType;
import world.item.armor.ArmorSlot;
import world.item.container.Container;
import world.notification.Notification;
import world.notification.NotificationScope;
import world.notification.NotificationSubscriber;
import world.room.RoomNotificationScope;

import static world.playerInterface.ColorTheme.*;

import static world.entity.equipment.EquipmentContainer.*;

public class GrabDropCommand extends EntityCommand {
    private String itemIdentifier;
    private String containerIdentifier;
    private boolean pickUp;
    private boolean complete = false;

    public GrabDropCommand(String itemIdentifier, String containerIdentifier, boolean pickUp, Client sourceClient, WorldModel model) {
        super(sourceClient, model);

        this.itemIdentifier = itemIdentifier;
        this.containerIdentifier = containerIdentifier;
        this.pickUp = pickUp;
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
        if(pickUp)
            pickUp();
        else
            putIn();

        complete = true;
    }

    private void putIn(){
        Item toPutIn = Item.getFromEntityContext(itemIdentifier,getSourceEntity(), getWorldModel().getItemFactory());
//TODO remove the key or item from player inventory too
        if(toPutIn == null || !getSourceEntity().getEquipment().isHoldingItem(toPutIn))
            getSourceClient().sendMessage("You are not holding a " + itemIdentifier);
        else if(containerIdentifier.isEmpty()){
            //drop it to the floor
            int result = getSourceEntity().getEquipment().dropItem(toPutIn);
            if(result == CODE_SUCCESS) {
                getSourceClient().sendMessage("You drop the " + toPutIn.getDisplayableName());
                toPutIn.updateInDatabase(toPutIn.getDatabaseName());
                getSourceEntity().updateInDatabase(getSourceEntity().getDatabaseName());
            }else if(result == CODE_NO_ITEM)
                getSourceClient().sendMessage("You are not holding a " + itemIdentifier);
            else
                getSourceClient().sendMessage("An error has occurred. You are unable to drop a " + itemIdentifier + " (" + result + ")");
        }else{
            //try store in container
            Item toStoreIn = Item.getFromEntityContext(containerIdentifier, getSourceEntity(), getWorldModel().getItemFactory());
            if(toStoreIn == null)
                getSourceClient().sendMessage("There is not a " + containerIdentifier + " nearby");
            else if(toStoreIn.getItemType() != ItemType.container)
                getSourceClient().sendMessage("The " + toStoreIn.getDisplayableName() + " is not a container. You cannot store items in it");
            else if(toStoreIn.getItemID() == toPutIn.getItemID())
                getSourceClient().sendMessage("You attempt to " + toPutIn.getDisplayableName() + " inside of itself. You fail. Honestly what did you expect?");
            else{
                Container container = (Container) toStoreIn;
                ArmorSlot returnSlot = getSourceEntity().getEquipment().getSlotOfItem(toPutIn);
                int result = getSourceEntity().getEquipment().dropItem(toPutIn);
                if(result == CODE_SUCCESS) {
                    if (!container.canHoldItem(toPutIn)) {
                        getSourceClient().sendMessage("The " + container.getDisplayableName() + " cannot hold that item");

                        getSourceEntity().getEquipment().forcePutItemInSlot(toPutIn,returnSlot);
                    }else if (container.tryStoreItem(toPutIn)) {
                        getSourceClient().sendMessage("You put the " +
                                toPutIn.getDisplayableName() + " in the " +
                                container.getDisplayableName());
                        toPutIn.updateInDatabase(toPutIn.getDatabaseName());
                        container.updateInDatabase(container.getDatabaseName());
                        getSourceEntity().updateInDatabase(getSourceEntity().getDatabaseName());
                    } else {
                        getSourceClient().sendMessage("You are unable to put the " +
                                toPutIn.getDisplayableName() + " in the " +
                                container.getDisplayableName());

                        getSourceEntity().getEquipment().forcePutItemInSlot(toPutIn,returnSlot);
                    }
                }else{
                    getSourceClient().sendMessage("You are unable to release the " + toPutIn.getDisplayableName());
                }
            }
        }
    }

    private Container getLocalContainer(String identifier){
        Item container = Item.getFromEntityContext(identifier, getSourceEntity(), getWorldModel().getItemFactory());
        if(container != null && container.getItemType() == ItemType.container)
            return (Container) container;
        return null;
    }

    private void pickUp(){
        Item toPickUp = Item.getFromEntityContext(itemIdentifier,getSourceEntity(), getWorldModel().getItemFactory());
        Container pickupFrom = null;
        if(toPickUp == null && !containerIdentifier.isEmpty() &&
                (pickupFrom = getLocalContainer(containerIdentifier)) != null) {
            toPickUp = pickupFrom.getContainedItem(itemIdentifier);
        }

        int holdCode;

        if(toPickUp == null)
            getSourceClient().sendMessage("There is no " + itemIdentifier + " nearby");
        else if((holdCode = getSourceEntity().getEquipment().canHoldItem(toPickUp, pickupFrom != null)) != EquipmentContainer.CODE_SUCCESS){
            if(holdCode == CODE_CONTAINER_FULL)
                getSourceClient().sendMessage("Your hands are full. You cannot pick up the " + toPickUp.getDisplayableName());
            else if(holdCode == CODE_TOO_HEAVY)
                getSourceClient().sendMessage("The " + toPickUp.getDisplayableName() + " is heavier than you can carry given your strength and equipment");
            else if(holdCode == CODE_NOT_NEAR)
                getSourceClient().sendMessage("There is no " + itemIdentifier + " nearby");
            else
                getSourceClient().sendMessage("An error has occurred. You are unable to pick up that item");
        }else{
            boolean proceed = false;
            if(!containerIdentifier.isEmpty()){
                if(pickupFrom == null)
                    getSourceClient().sendMessage("There is no " + containerIdentifier + " nearby");
                else if(!(pickupFrom).containsItem(toPickUp))
                    getSourceClient().sendMessage("The " + pickupFrom.getDisplayableName() + " does not contain a " + toPickUp.getDisplayableName());
                else {
                    proceed = true;
                }
            }else{
                proceed = true;
            }

            if(proceed) {
                getSourceEntity().getEquipment().holdItem(toPickUp, pickupFrom != null);
                toPickUp.setContainerID(0);
                toPickUp.setRoomName("");
                getSourceEntity().updateInDatabase(getSourceEntity().getDatabaseName());
                toPickUp.updateInDatabase(toPickUp.getDatabaseName());

                Notification pickedUp = new ItemAcquiredNotification(getSourceEntity(),toPickUp,true,pickupFrom,getWorldModel().getRegistry());
                NotificationScope scope = new RoomNotificationScope(getSourceEntity().getRoomName(),getSourceEntity().getDatabaseName());
                getWorldModel().getNotificationService().notify(pickedUp, scope);
            }
        }
    }

    private class ItemAcquiredNotification extends Notification{
        private Entity actor;
        private Item target;
        private Item sourceContainer;
        private boolean wasPickedUp;

        public ItemAcquiredNotification(Entity actor, Item target, boolean wasPickedUp, Item sourceContainer, ClientRegistry registry) {
            super(registry);
            this.actor = actor;
            this.target = target;
            this.wasPickedUp = wasPickedUp;
            this.sourceContainer = sourceContainer;
        }

        @Override
        public String getAsMessage(NotificationSubscriber viewer) {
            String response;
            if(viewer.getID().equals(actor.getID())){
                if(wasPickedUp)
                    response = getMessageInColor("You take the ", INFORMATIVE) + getMessageInColor(target.getDisplayableName(),ITEM);
                else
                    response = getMessageInColor("You drop up the ", INFORMATIVE) + getMessageInColor(target.getDisplayableName(),ITEM);
            }else {
                Faction viewerFaction = ((Entity)viewer).getDiplomacy().getFaction();
                DiplomaticRelation relation = getWorldModel().getDiplomacyManager().getRelation(viewerFaction,actor.getDiplomacy().getFaction());

                if (wasPickedUp) {
                    response = getMessageInColor(actor.getDisplayName(), relation) + getMessageInColor(" takes a ", INFORMATIVE) + getMessageInColor(target.getDisplayableName(), ITEM);
                    if (sourceContainer == null)
                        response += getMessageInColor(" from the ground", INFORMATIVE);
                    else
                        response += getMessageInColor(" from the ", INFORMATIVE) + getMessageInColor(sourceContainer.getDisplayableName(), ITEM);
                } else {
                    response = getMessageInColor(actor.getDisplayName(), relation) + getMessageInColor( " drops up a ", INFORMATIVE) + getMessageInColor(target.getDisplayableName(), ITEM);
                }
            }
            return response;
        }
    }
}
