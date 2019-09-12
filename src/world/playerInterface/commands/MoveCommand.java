package world.playerInterface.commands;

import client.Client;
import client.ClientRegistry;
import world.WorldModel;
import world.entity.Entity;
import world.notification.Notification;
import world.notification.NotificationSubscriber;
import world.room.Room;

import java.util.Arrays;
import java.util.List;

import static world.playerInterface.ColorTheme.*;

public class MoveCommand extends EntityCommand {
    private String direction;
    private boolean complete = false;
    private int staminaNeeded;
    private int staminaUsed = 0;

    public MoveCommand(String direction, Client sourceClient, WorldModel model) {
        super(sourceClient, model);
        this.direction = direction;

        //check the entity status first, we don't know if we have one or not until executeEntityCommand() is called
        if(getSourceEntity() != null && !getSourceEntity().getEquipment().isEncumbered())
            staminaNeeded = 0;
        else
            staminaNeeded = 10;
    }

    @Override
    protected void executeEntityCommand() {
        if(Room.directions.indexOf(direction) >= 0){
            Entity sourceEntity = getSourceEntity();
            Room curRoom = Room.getRoomByRoomName(sourceEntity.getRoomName(),sourceEntity.getDatabaseName());
            if(curRoom != null) {
                Room travelRoom = Room.getRoomByRoomName(curRoom.getConnectedRoomID(direction), curRoom.getDatabaseName());
                if(travelRoom != null && travelRoom.getVisibilityCode() == 0){
                    staminaUsed = staminaNeeded;
                    notifyEntityRoom(new TransferRoomNotification(getSourceEntity(), false, direction, getWorldModel().getRegistry()),getSourceEntity().getID());
                    sourceEntity.setRoom(travelRoom);
                    sourceEntity.updateInDatabase(sourceEntity.getDatabaseName());
                    notifyEntityRoom(new TransferRoomNotification(getSourceEntity(), true, direction, getWorldModel().getRegistry()),getSourceEntity().getID());
                    new LookCommand("",false,getSourceClient(),getWorldModel()).execute();
                }else
                    getSourceClient().sendMessage("You cannot travel " + direction);
            }else
                getSourceClient().sendMessage("This is embarrassing. I regret to inform you that you do not currently have a location. Very strange. I suggest getting in contact with an admin. Weird");
        }else
            getSourceClient().sendMessage("I do not understand the direction " + direction + ". Please try again.");
        complete = true;
    }

    @Override
    protected int getRequiredStamina() {
        return staminaNeeded;
    }

    @Override
    protected int getStaminaUsed() {
        return staminaUsed;
    }

    @Override
    protected boolean requiresBalance() {
        return true;
    }

    @Override
    public boolean entityCommandIsComplete() {
        return complete;
    }

    public class TransferRoomNotification extends Notification{
        private Entity sourceEntity;
        private String direction;
        private boolean didEnter;
        private final List<String> directions = Arrays.asList("up","north","east","down","south","west");

        public TransferRoomNotification(Entity sourceEntity, boolean didEnter, String leaveDirection, ClientRegistry registry) {
            super(registry);
            if(didEnter) direction = getOppositeDirection(leaveDirection);
            else direction = leaveDirection;
            this.didEnter = didEnter;
            this.sourceEntity = sourceEntity;
        }

        private String getOppositeDirection(String direction){
            int oldIndex = directions.indexOf(direction);
            if(oldIndex >= 0) {
                int newIndex = (oldIndex + directions.size() / 2) % directions.size();
                return directions.get(newIndex);
            }return "error";
        }

        @Override
        public String getAsMessage(NotificationSubscriber viewer) {
            String arrives = didEnter? " arrives from the ":" leaves to the ";
            return getMessageInColor(getEntityColored(sourceEntity,(Entity)viewer,getWorldModel()) + arrives + direction, INFORMATIVE);
        }
    }
}
