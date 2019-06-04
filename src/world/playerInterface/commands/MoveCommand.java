package world.playerInterface.commands;

import client.Client;
import world.entity.Entity;
import world.room.Room;

public class MoveCommand extends EntityCommand {
    private String direction;
    private boolean complete = false;

    public MoveCommand(String direction, Client sourceClient) {
        super(sourceClient);
        this.direction = direction;
    }

    @Override
    protected void executeEntityCommand() {
        if(Room.directions.indexOf(direction) >= 0){
            Entity sourceEntity = getSourceEntity();
            Room curRoom = Room.getRoomByRoomName(sourceEntity.getRoomName(),sourceEntity.getDatabaseName());
            if(curRoom != null) {
                Room travelRoom = Room.getRoomByRoomName(curRoom.getConnectedRoomID(direction), curRoom.getDatabaseName());
                if(travelRoom != null && travelRoom.getVisibilityCode() == 0){
                    sourceEntity.setRoom(travelRoom);
                    sourceEntity.updateInDatabase(sourceEntity.getDatabaseName());
                    new LookCommand("",false,getSourceClient()).execute();
                }else
                    getSourceClient().sendMessage("You cannot travel " + direction);
            }else
                getSourceClient().sendMessage("This is embarrassing. I regret to inform you that you do not currently have a location. Very strange. I suggest getting in contact with an admin. Weird");
        }else
            getSourceClient().sendMessage("I do not understand the direction " + direction + ". Please try again.");
        complete = true;

    }

    @Override
    public boolean isComplete() {
        return complete;
    }
}
