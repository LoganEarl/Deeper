package world.playerInterface.commands;

import network.SimulationManager;
import world.entity.Entity;
import world.entity.Race;
import world.playerInterface.PlayerManagementInterface;
import world.playerInterface.messages.ClientCreateCharacterMessage;
import world.playerInterface.messages.ContextMessage;

public class CreateCharCommand implements SimulationManager.Command, PlayerManagementInterface.MessageContext {
    private long lastUpdateTime = System.currentTimeMillis();
    private static final long EXPIRATION_TIME = 600000; //10 minutes

    private int stage = STAGE_START;

    private Entity.EntityBuilder builder = new Entity.EntityBuilder();
    private ClientCreateCharacterMessage message;
    private PlayerManagementInterface service;
    private String[] newMessageArgs = null;

    private static final int STAGE_START = 0;
    private static final int STAGE_DISPLAY_NAME = 1;
    private static final int STAGE_RACE = 2;
    private static final int STAGE_STATS = 3;
    private static final int STAGE_VERIFY = 4;
    private static final int STAGE_COMPLETE = 5;
    private static final int STAGE_NO_USERNAME = -1;
    private boolean refreshFlag = false;

    public CreateCharCommand(String userName, ClientCreateCharacterMessage message, PlayerManagementInterface service){
        this.service = service;
        this.message = message;

        if(userName == null) {
            stage = STAGE_NO_USERNAME;
            refreshFlag = true;
        }
        else {
            service.addClientMessageContext(message.getClient(),this);
            builder.setID(userName);
        }
    }

    @Override
    public void execute() {
        if(refreshFlag) {
            refreshFlag = false;
            if (stage == STAGE_NO_USERNAME) {
                service.sendMessage("You must be logged in to create a character", message.getClient());
                service.removeMessageContextOfClient(message.getClient(), this);
                stage = STAGE_COMPLETE;
            }
            if(stage == STAGE_START){
                newMessageArgs = null;
                stage = STAGE_DISPLAY_NAME;
            }
            if(stage == STAGE_DISPLAY_NAME) {
                if(newMessageArgs == null) {
                    service.sendMessage("Please enter the name you wish to go by", message.getClient());
                } else if(newMessageArgs.length == 1 &&
                        newMessageArgs[0].length() > 3 &&
                        newMessageArgs[0].chars().allMatch(Character::isAlphabetic)){
                    builder.setDisplayName(newMessageArgs[0]);
                    service.sendMessage("Very well, you will be known as " + newMessageArgs[0] + " from now on.", message.getClient());
                    newMessageArgs = null;
                }else{
                    service.sendMessage("That name will not do. Please choose a name with 4 or more letters, made up of only letters", message.getClient());
                }
            }
            if(stage == STAGE_RACE) {
                Race selectedRace;
                if(newMessageArgs == null){
                    service.sendMessage("Please choose a race from the following...\n\n" +
                            Race.getPlayableRaceDescriptions(), message.getClient());
                }else if(newMessageArgs.length == 1 && (selectedRace = Race.getFromID(newMessageArgs[0])) != null){
                    builder.setStatsToRaceDefaults(selectedRace);
                    stage = STAGE_STATS;
                    newMessageArgs = null;
                }
            }
            if(stage == STAGE_STATS){
                
            }
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
    public boolean registerMessage(Entity fromEntity, boolean isLoggedIn, ContextMessage message) {
        if(isLoggedIn) {
            lastUpdateTime = System.currentTimeMillis();

            newMessageArgs = message.getArgs();
            refreshFlag = true;
            return true;
        }
        return false;
    }
}
