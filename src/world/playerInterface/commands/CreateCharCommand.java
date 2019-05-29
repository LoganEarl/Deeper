package world.playerInterface.commands;

import network.SimulationManager;
import network.WebServer;
import world.entity.Entity;
import world.playerInterface.PlayerManagementService;
import world.playerInterface.messages.ClientCreateCharacterMessage;

public class CreateCharCommand implements SimulationManager.Command, PlayerManagementService.MessageContext {
    private long lastUpdateTime = System.currentTimeMillis();
    private static final long EXPIRATION_TIME = 600000; //10 minutes

    private int stage = 0;

    private Entity.EntityBuilder builder = new Entity.EntityBuilder();
    private ClientCreateCharacterMessage message;
    private PlayerManagementService service;

    private static final int STAGE_DISPLAY_NAME = 1;
    private static final int STAGE_RACE = 2;
    private static final int STAGE_STATS = 3;
    private static final int STAGE_COMPLETE = 4;
    private static final int STAGE_NO_USERNAME = -1;

    public CreateCharCommand(String userName, ClientCreateCharacterMessage message, PlayerManagementService service){
        this.service = service;
        this.message = message;

        if(userName == null)
            stage = STAGE_NO_USERNAME;
        else {
            service.addClientMessageContext(message.getClient(),this);
            builder.setID(userName);
        }
    }

    @Override
    public void execute() {
        if(stage == STAGE_NO_USERNAME){
            service.sendMessage("You must be logged in to create a character", message.getClient());
            service.removeMessageContextOfClient(message.getClient(),this);
            stage = STAGE_COMPLETE;
        }
    }

    @Override
    public boolean isComplete() {
        return stage == STAGE_COMPLETE || getTimeToExpire() < 0;
    }

    @Override
    public long getTimeToExpire() {
        return EXPIRATION_TIME - (System.currentTimeMillis() - lastUpdateTime);
    }

    //TODO client input comes in here
    @Override
    public boolean registerMessage(Entity fromEntity, boolean isLoggedIn, WebServer.ClientMessage message) {
        if(isLoggedIn) {
            lastUpdateTime = System.currentTimeMillis();


            return true;
        }
        return false;
    }
}
