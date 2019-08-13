package world.playerInterface.commands;

import client.Client;
import world.WorldModel;
import world.entity.Entity;
import world.room.Room;

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
            staminaNeeded = 1;
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
                    sourceEntity.setRoom(travelRoom);
                    sourceEntity.updateInDatabase(sourceEntity.getDatabaseName());
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
}
