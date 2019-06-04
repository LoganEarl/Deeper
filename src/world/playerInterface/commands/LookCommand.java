package world.playerInterface.commands;

import client.Client;
import world.WorldUtils;
import world.entity.Entity;
import world.entity.Race;
import world.item.Container;
import world.item.Item;
import world.room.Room;

import java.util.List;
import java.util.Locale;

/**
 * command used to get item and room descriptions. Can also be used to look into unlocked containers.
 * @author Logan Earl
 */
public class LookCommand extends EntityCommand {
    private Entity fromEntity;
    private Client fromClient;
    private String target;
    private boolean lookInto;

    private boolean complete = false;

    /**
     * Sole constructor
     * @param target the item/entity/container that is being inspected.
     * @param lookInto true to look inside the selected thing. Does not apply to rooms
     * @param fromClient the client doing the looking.
     */
    public LookCommand(String target, boolean lookInto, Client fromClient){
        super(fromClient);
        this.fromEntity = WorldUtils.getEntityOfClient(fromClient);
        this.fromClient = fromClient;
        this.target = target;
        this.lookInto = lookInto;
    }

    @Override
    public void executeEntityCommand() {
        if(fromEntity == null){
            complete = true;
            System.out.println("Failed to execute a look command for a null entity");
        }

        String response;

        if(target == null || target.isEmpty())
            response = describeRoom();
        else
            response = "Not yet implemented";
        fromClient.sendMessage(response);

        complete = true;
    }

    private String describeRoom(){
        String roomName;
        String roomDesc;
        String creaturesString;
        String containersString;
        String itemsString;

        roomName = fromEntity.getRoomName();
        Room r = Room.getRoomByRoomName(roomName,fromEntity.getDatabaseName());
        if(r == null){
            System.out.println("Failed to get room info for look command sourced by entityID:" + fromEntity.getID() + " in database " + fromEntity.getDatabaseName());
            return "An error has occurred. Unable to get room info for you at this time";
        }
        roomDesc = r.getRoomDescription();
        creaturesString = getCreatureString(r);
        containersString = getContainerString(r);
        itemsString = getItemsString(r);

        return String.format(Locale.US,
                "%s\n\n%s\n\n" +
                        "%s\n\n%s%s", roomName, roomDesc, creaturesString, containersString, itemsString);
    }

    private String getCreatureString(Room r){
        List<Entity> nearEntities = Entity.getEntitiesInRoom(r.getRoomName(), r.getDatabaseName(), fromEntity.getID());
        if(nearEntities.size() == 0)
            return "There are no other entities here";
        else{
            StringBuilder creatureStringBuilder = new StringBuilder();
            boolean first = true;
            for(Entity e:nearEntities) {
                if(first)
                    first = false;
                else
                    creatureStringBuilder.append("\n");

                creatureStringBuilder.append(e.getDisplayName());
                Race entityRace = e.getRace();
                if (entityRace != null)
                    creatureStringBuilder.append(" the ").append(entityRace.getDisplayName()).append(" is nearby");
            }
           return creatureStringBuilder.toString();
        }
    }

    private String getContainerString(Room r){
        List<Container> nearContainers = Container.getContainersInRoom(r.getRoomName(), r.getDatabaseName());
        if(nearContainers.size() == 0)
            return "";
        else{
            StringBuilder containerStringBuilder = new StringBuilder();
            boolean first = true;
            for(Container c: nearContainers){
                if(first)
                    first = false;
                else
                    containerStringBuilder.append("\n");
                containerStringBuilder.
                        append("There is a ")
                        .append(c.getContainerName())
                        .append(" nearby, it is ")
                        .append(c.getIsLocked()? "locked": "unlocked");
            }
            return containerStringBuilder.toString();
        }
    }

    private String getItemsString(Room r){
        List<Item> nearItems = Item.getItemsInRoom(r.getRoomName(),r.getDatabaseName());
        if(nearItems.size() == 0)
            return "";
        else{
            StringBuilder itemStringBuilder = new StringBuilder();
            boolean first = false;
            for(Item i : nearItems){
                if(first)
                    first = false;
                else
                    itemStringBuilder.append("\n");
                itemStringBuilder.append("There is a ").append(i.getDisplayableName()).append(" nearby");
            }
            return itemStringBuilder.toString();
        }
    }

    @Override
    public boolean isComplete() {
        return complete;
    }
}
