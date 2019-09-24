package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.client.ClientRegistry;
import main.java.world.WorldModel;
import main.java.world.entity.Entity;
import main.java.world.item.Item;
import main.java.world.item.ItemType;
import main.java.world.item.armor.Armor;
import main.java.world.notification.ConcreteNotification;

import static main.java.world.playerInterface.ColorTheme.*;

public class EquipCommand extends EntityCommand {
    private String itemName;
    private boolean complete = false;
    private boolean shouldPutOn;

    public EquipCommand(String itemName, boolean shouldPutOn, Client sourceClient, WorldModel model) {
        super(sourceClient, model);
        this.itemName = itemName;
        this.shouldPutOn = shouldPutOn;
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
        Item item = getSourceEntity().getEquipment().getEquippedItem(itemName);
        if (item == null) {
            try {
                int itemId = Integer.parseInt(itemName);
                item = getSourceEntity().getEquipment().getEquippedItem(itemId);
            } catch (NumberFormatException ignored) {}
        }

        if (item == null) {
            String state = shouldPutOn ? "holding" : "wearing";
            getSourceClient().sendMessage(getMessageInColor("You are not " + state + " a " + itemName, FAILURE));
        } else if (item.getItemType() != ItemType.armor)
            getSourceClient().sendMessage(getMessageInColor("The " + getItemColored(item) + " is not a piece of armor.", FAILURE));
        else if (!shouldPutOn && getSourceEntity().getEquipment().getSlotOfItem(item) == null)
            getSourceClient().sendMessage(getMessageInColor("You are not wearing the " + getItemColored(item), FAILURE));
        else if(!shouldPutOn && getSourceEntity().getEquipment().getFreeHand() == null)
            getSourceClient().sendMessage(getMessageInColor("Your hands are full", FAILURE));
        else {
            Armor armor = (Armor) item;
            boolean success;
            if (shouldPutOn)
                success = getSourceEntity().getEquipment().equipArmor(armor);
            else
                success = getSourceEntity().getEquipment().unequipArmor(armor) != null;

            if (success)
                notifyEntityRoom(new EquipNotification(armor, shouldPutOn, getSourceEntity(), getWorldModel().getRegistry()));
            else
                getSourceClient().sendMessage(getMessageInColor("You are unable to " + (shouldPutOn?"equip the ":"unequip the ") + getItemColored(item), FAILURE));
        }
        complete = true;
    }
    class EquipNotification extends ConcreteNotification {
        private Armor armor;
        private boolean wasPutOn;
        private Entity sourceEntity;

        public EquipNotification(Armor armor, boolean wasPutOn, Entity sourceEntity, ClientRegistry registry) {
            super(registry);
            this.armor = armor;
            this.wasPutOn = wasPutOn;
            this.sourceEntity = sourceEntity;
        }

        @Override
        public String getAsMessage(Entity viewer) {
            String putsOn = wasPutOn ? "equip" : "remove";

            if (viewer == sourceEntity)
                return getMessageInColor("You " + putsOn + " the " + getItemColored(armor), INFORMATIVE);
            else
                return getMessageInColor(getEntityColored(sourceEntity, viewer, getWorldModel()) + " " + putsOn + "s a " + getItemColored(armor), INFORMATIVE);
        }
    }
}
