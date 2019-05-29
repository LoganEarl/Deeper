package world.playerInterface.commands;

import network.SimulationManager;
import network.WebServer;
import world.entity.Entity;
import world.playerInterface.PlayerManagementService;

public class CreateCharCommand implements SimulationManager.Command, PlayerManagementService.MessageContext {
    private long lastUpdateTime = System.currentTimeMillis();

    //TODO create a staging system for character creation and work through it linerarly.


    //TODO server clock is here
    @Override
    public void execute() {
        //TODO make sure to do expensive stuff here
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public long getTimeToExpire() {
        return 0;
    }

    //TODO client input comes in here
    @Override
    public boolean registerMessage(Entity fromEntity, boolean isLoggedIn, WebServer.ClientMessage message) {
        //TODO short stuff here. Add to entity n chit
        return false;
    }
}
