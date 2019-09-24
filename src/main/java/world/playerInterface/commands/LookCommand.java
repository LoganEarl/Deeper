package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.world.WorldModel;
import main.java.world.WorldUtils;
import main.java.world.entity.Entity;
import main.java.world.item.Item;
import main.java.world.item.ItemType;
import main.java.world.item.container.Container;
import main.java.world.room.Room;

import java.util.List;
import java.util.Locale;

import static main.java.world.playerInterface.ColorTheme.*;

/**
 * command used to get item and room descriptions. Can also be used to look into unlocked containers.
 * @author Logan Earl
 */
public class LookCommand extends EntityCommand {

    private Client fromClient;
    private String target;
    private boolean lookInto;

    private boolean complete = false;

    /**
     * Sole constructor
     * @param target the item/entity/container that is being inspected.
     * @param lookInto true to look inside the selected thing. Does not apply to rooms
     * @param fromClient the main.java.client doing the looking.
     */
    public LookCommand(String target, boolean lookInto, Client fromClient, WorldModel model){
        super(fromClient, model);

        this.fromClient = fromClient;
        this.target = target;
        this.lookInto = lookInto;
    }

    @Override
    public void executeEntityCommand() {
        String response;

        if(target == null || target.isEmpty())
            response = describeRoom();
        else if(lookInto){
            response = lookInContainer(target);
        }else{
            response = getMessageInColor("Not yet implemented",FAILURE);
        }

        fromClient.sendMessage(response);

        complete = true;
    }

    @Override
    protected boolean requiresBalance() {
        return true;
    }

    @Override
    protected void setBalance() {
        if(lookInto)
            super.setBalance();
    }

    private String lookInContainer(String target){
        String response;
        Item rawItem = Item.getFromEntityContext(target,getSourceEntity(), getWorldModel().getItemFactory());
        if(rawItem != null){
            if(rawItem.getItemType() == ItemType.container && rawItem instanceof Container) {
                Container container = (Container)rawItem;
                response = readContainer(container);
            }else{
                response = getMessageInColor("You peer closely at the " + getItemColored(rawItem) + ". ",INFORMATIVE) + getMessageInColor("It has no openings you can see.",FAILURE);
            }
        }else{
            response = getMessageInColor("You cannot find a " + target + " to look into",FAILURE);
        }

        return response;
    }

    private String readContainer(Container container){
        if(container.getIsLocked()){
            return getMessageInColor("It is locked. You are unable to discover it's contents",FAILURE);
        }else {
            List<Item> storedItems = container.getStoredItems();
            if(storedItems.size() == 0)
                return "It is empty";
            if(storedItems.size() == 1)
                return "It contains a " + storedItems.get(0).getDisplayableName();
            StringBuilder contents = new StringBuilder("The ");
            contents.append(container.getDisplayableName()).append(" contains the following:\n");
            for(Item i: storedItems)
                contents.append(i.getDisplayableName()).append("\n");
            return contents.toString();
        }
    }

    private String describeRoom(){
        String roomName;
        String roomDesc;
        String waysDesc;
        String creaturesString;
        String itemsString;

        roomName = getSourceEntity().getRoomName();
        Room r = Room.getRoomByRoomName(roomName,getSourceEntity().getDatabaseName());
        if(r == null){
            System.out.println("Failed to get room info for look command sourced by entityID:" + getSourceEntity().getID() + " in database " + getSourceEntity().getDatabaseName());
            return getMessageInColor("An error has occurred. Unable to get room info for you at this time",FAILURE);
        }
        roomDesc = r.getRoomDescription();
        waysDesc = getWays(r);
        creaturesString = getCreatureString(r);
        itemsString = getItemsString(r);

        return String.format(Locale.US,
                "%s\n\n%s\n\n%s\n" +
                        "%s\n\n%s", roomName, roomDesc,waysDesc, creaturesString, itemsString);
    }

    private String getWays(Room r){
        String[] directions = r.getAvailableDirections();
        if(directions.length == 0)
            return getMessageInColor("There is no way out",WARNING);
        if(directions.length == 1)
            return "There is a way to the " + getMessageInColor(directions[0],INFORMATIVE);
        return "There are ways to the " + getMessageInColor(WorldUtils.commaSeparate(directions),INFORMATIVE);
    }

    private String getCreatureString(Room r){
        List<Entity> nearEntities = getWorldModel().getEntityCollection().getEntitiesInRoom(r.getRoomName(), r.getDatabaseName(), getSourceEntity().getID());
        if(nearEntities.size() == 0)
            return getMessageInColor("You are alone",INFORMATIVE);
        else{
            StringBuilder creatureStringBuilder = new StringBuilder();
            boolean first = true;
            for(Entity e:nearEntities) {
                if(first)
                    first = false;
                else
                    creatureStringBuilder.append("\n");

                creatureStringBuilder.append(getEntityColored(e,getSourceEntity(),getWorldModel())).append(getMessageInColor(" is nearby",INFORMATIVE));
            }
           return creatureStringBuilder.toString();
        }
    }

    private String getItemsString(Room r){
        List<Item> nearItems = Item.getItemsInRoom(r.getRoomName(), getWorldModel().getItemFactory(),r.getDatabaseName());
        if(nearItems.size() == 0)
            return "";
        else{
            StringBuilder itemStringBuilder = new StringBuilder();
            boolean first = true;
            for(Item i : nearItems){
                if(first)
                    first = false;
                else
                    itemStringBuilder.append("\n");
                itemStringBuilder.append("There is a ").append(getItemColored(i)).append(" nearby");
                if(i instanceof Container){
                    itemStringBuilder.append(". It is ").append(getMessageInColor((((Container)i).getIsLocked()? "locked": "unlocked"),INFORMATIVE));
                }
            }
            return itemStringBuilder.toString();
        }
    }

    @Override
    public boolean entityCommandIsComplete() {
        return complete;
    }
}
