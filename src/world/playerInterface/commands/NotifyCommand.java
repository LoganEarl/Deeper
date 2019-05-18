package world.playerInterface.commands;

import network.SimulationManager;
import network.WebServer;
import world.entity.Entity;
import world.playerInterface.PlayerManagementService;
import world.playerInterface.messages.ClientLookMessage;

public class NotifyCommand implements SimulationManager.Command, WebServer.ServerMessage  {
    public NotifyCommand(ClientLookMessage sourceMessage, Entity observer, PlayerManagementService service){
        //TODO dont make the message here
    }

    @Override
    public void execute() {
        //TODO make and send it here
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }
}
