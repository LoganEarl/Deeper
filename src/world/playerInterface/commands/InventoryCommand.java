package world.playerInterface.commands;

import client.Client;
import world.entity.Entity;
import world.item.Item;
import world.item.ItemType;
import world.item.armor.ArmorSlot;
import world.item.container.Container;

import java.util.Locale;

import static world.item.armor.ArmorSlot.*;

public class InventoryCommand extends EntityCommand {
    private boolean complete = false;

    public InventoryCommand(Client sourceClient) {
        super(sourceClient);
    }

    @Override
    protected boolean requiresBalance() {
        return false;
    }

    @Override
    protected boolean entityCommandIsComplete() {
        return complete;
    }

    @Override
    protected void executeEntityCommand() {
        getSourceClient().sendMessage(getEntityDescription());
        complete = true;
    }

    private String getEntityDescription(){
        ArmorSlot[] loopOrder = {rightHand,rightSheath,leftHand,leftSheath,head,chest,legs,feet,hands,beltPouch,beltUtil};
        Entity entity = getSourceEntity();

        StringBuilder desc = new StringBuilder(
                String.format(Locale.US, "%s ths %s\n", entity.getDisplayName(), entity.getRace().getDisplayName()));

        for(ArmorSlot slot: loopOrder){
            String itemName;
            String itemType;
            double itemWeight;

            Item equipped = entity.getEquipment().getEquippedItem(slot);
            if(equipped == null){
                itemName = "Empty";
                itemType = "";
                itemWeight = 0.0;
            }else{
                itemName = equipped.getDisplayableName();
                itemType = equipped.getItemType().name();
                itemWeight = equipped.getWeight();
            }

            desc.append(String.format(Locale.US,"\t%14.14s  %16.16s  %10.10s  %2.2fkgs\n",
                    slot.name(),itemName,itemType,itemWeight));
        }
        desc.append(String.format(Locale.US, "Total equipment weight: %.2fkgs out of %.2f/%.2fkgs",
                entity.getEquipment().getEquipmentWeight(), entity.getStats().getWeightSoftLimit(), entity.getStats().getWeightHardLimit()));

        return desc.toString();
    }
}
