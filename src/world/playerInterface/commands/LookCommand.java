package world.playerInterface.commands;

import network.SimulationManager;
import network.WebServer;
import world.entity.Entity;
import world.item.Item;
import world.playerInterface.PlayerManagementService;
import world.playerInterface.messages.ClientLookMessage;
import world.room.Room;

import java.util.List;
import java.util.Locale;

public class LookCommand implements SimulationManager.Command, WebServer.ServerMessage  {
    private Entity fromEntity;
    private ClientLookMessage sourceMessage;

    private boolean complete = false;
    private String messageToSend = "";

    public LookCommand(ClientLookMessage sourceMessage, Entity observer, PlayerManagementService service){
        fromEntity = observer;
        this.sourceMessage = sourceMessage;
    }

    @Override
    public void execute() {
        if(sourceMessage == null || fromEntity == null){
            complete = true;
            System.out.println("Failed to execute a look command for a null entity or null message");
        }

        if(sourceMessage.getTarget() == null || sourceMessage.getTarget().isEmpty())
            describeRoom();
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

        List<Entity> nearEntities = Entity.getEntitiesInRoom(roomName, r.getDatabaseName(), fromEntity.getID());
        if(nearEntities.size() == 0)
            creaturesString = "There are no other entities here";
        else if(nearEntities.size() == 1)
            creaturesString = "";
        return String.format(Locale.US,
                "%s\n\n%s\n\n" +
                        "%s\n%s");
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }
}
