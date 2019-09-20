package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.world.WorldModel;
import main.java.world.entity.Entity;
import main.java.world.item.Item;
import main.java.world.item.armor.ArmorSlot;
import main.java.world.playerInterface.ColorTheme;

import java.util.Locale;

import static main.java.world.item.armor.ArmorSlot.*;

public class InventoryCommand extends EntityCommand {
    private boolean complete = false;

    public InventoryCommand(Client sourceClient, WorldModel model) {
        super(sourceClient, model);
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
        ArmorSlot[] loopOrder = {rightHand,leftHand,rightSheath,leftSheath,head,chest,legs,feet,hands,beltPouch,beltUtil};
        Entity entity = getSourceEntity();

        StringBuilder desc = new StringBuilder(
                String.format(Locale.US, "%s the %s\n", entity.getDisplayName(), entity.getRace().getDisplayName()));

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

            desc.append(String.format(Locale.US,"\t%14.14s  " + ColorTheme.getMessageInColor("%16.16s",ColorTheme.ITEM) + "  %10.10s  %2.2fkgs\n",
                    slot.name(),itemName,itemType,itemWeight));
        }
        desc.append(String.format(Locale.US, ColorTheme.getMessageInColor("Total equipment weight: %.2fkgs out of %.2f/%.2fkgs",ColorTheme.INFORMATIVE),
                entity.getEquipment().getEquipmentWeight(), entity.getStats().getWeightSoftLimit(), entity.getStats().getWeightHardLimit()));

        return desc.toString();
    }
}
