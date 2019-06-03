package world.playerInterface.commands;

import client.Client;
import network.CommandExecutor;
import network.SimulationManager;
import network.messaging.MessagePipeline;
import world.entity.Entity;
import world.entity.Race;
import world.meta.World;
import world.playerInterface.PlayerManagementInterface;
import world.playerInterface.messages.ClientCreateCharacterMessage;

import java.util.Arrays;
import java.util.Locale;

public class CreateCharCommand implements CommandExecutor.Command, MessagePipeline.MessageContext {
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

    private String selectedName;
    private Race selectedRace;

    private int[] allocations = {0,0,0,0};
    private String[] allocationNames = {"str","dex","int","wis"};
    private int pointsAvailable = 40;

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
            checkForUsername();
            checkForStart();
            checkForDisplayName();
            checkForRace();
            checkForStats();
            checkForVerification();
        }
    }

    private void checkForUsername(){
        if (stage == STAGE_NO_USERNAME) {
            service.sendMessage("You must be logged in to create a character", message.getClient());
            service.removeMessageContextOfClient(message.getClient(), this);
            stage = STAGE_COMPLETE;
        }
    }

    private void checkForStart(){
        if(stage == STAGE_START){
            newMessageArgs = null;
            stage = STAGE_DISPLAY_NAME;
        }
    }

    private void checkForDisplayName(){
        if(stage == STAGE_DISPLAY_NAME) {
            if(newMessageArgs == null) {
                service.sendMessage("Please enter the name you wish to go by", message.getClient());
            } else if(newMessageArgs.length == 1 &&
                    newMessageArgs[0].length() > 3 &&
                    newMessageArgs[0].chars().allMatch(Character::isAlphabetic)){
                builder.setDisplayName(newMessageArgs[0]);
                selectedName = newMessageArgs[0];
                service.sendMessage("Very well, you will be known as " + newMessageArgs[0] + " from now on.", message.getClient());
                newMessageArgs = null;
            }else{
                service.sendMessage("That name will not do. Please choose a name with 4 or more letters, made up of only letters", message.getClient());
            }
        }
    }

    private void checkForRace(){
        if(stage == STAGE_RACE) {
            if(newMessageArgs == null){
                service.sendMessage("Please choose a race from the following...\n\n" +
                        Race.getPlayableRaceDescriptions(), message.getClient());
            }else if(newMessageArgs.length == 1 && (selectedRace = Race.getFromID(newMessageArgs[0])) != null){
                stage = STAGE_STATS;
                newMessageArgs = null;
            }else{
                service.sendMessage("I did not understand that, Please choose a race from the following...\n\n" + Race.getPlayableRaceDescriptions(), message.getClient());
            }
        }
    }

    private void checkForStats(){
        if(stage == STAGE_STATS){
            int attributeIndex;

            if(newMessageArgs == null){
                service.sendMessage("You have " + pointsAvailable +
                        " stat points available to allocate. Allocate points with: [add/subtract] [number of points] [from/to] [str/dex/int/wis]. Use [done] when complete.", message.getClient());
            }else if(newMessageArgs.length == 4 &&
                    (newMessageArgs[0].equals("add") || newMessageArgs[0].equals("subtract")) &&
                    isInteger(newMessageArgs[1]) &&
                    (newMessageArgs[2].equals("from") || newMessageArgs[2].equals("to")) &&
                    (attributeIndex = Arrays.asList(allocationNames).indexOf(newMessageArgs[3])) != -1){
                int points = Integer.parseInt(newMessageArgs[1]);
                if(newMessageArgs[0].equals("subtract")) points *= -1;
                if(pointsAvailable >= points && allocations[attributeIndex] + points >= 0){
                    allocations[attributeIndex] += points;
                    service.sendMessage("You have " + pointsAvailable + " points remaining. Your stats are as follows\n" + getCurrentStats(),message.getClient());
                }else if(pointsAvailable < points)
                    service.sendMessage("You do not have enough points for this allocation", message.getClient());
                else
                    service.sendMessage("You cannot allocate less points to an attribute than your race's base stats",message.getClient());
            }else if(newMessageArgs.length == 1 && newMessageArgs[0].equals("done")){
                builder.setStrength(allocations[0] + selectedRace.getBaseStr());
                stage = STAGE_VERIFY;
                newMessageArgs = null;
            }else{
                service.sendMessage("I do not understand. Allocate points with: [add/subtract] [number of points] [from/to] [str/dex/int/wis]. Use [done] when complete.",message.getClient());
            }
        }
    }

    private void checkForVerification(){
        if(stage == STAGE_VERIFY){
            if(newMessageArgs == null){
                service.sendMessage(String.format(Locale.US, "Name: %s\nRace: %s\nStats: %s\n\nIs this correct? [yes/no]",selectedName,selectedRace.getDisplayName(),getCurrentStats()),message.getClient());
            }else if(newMessageArgs.length == 1 && newMessageArgs[0].equals("yes")){
                service.sendMessage("Very well, your character creation is now complete. Welcome " + selectedName + " to the Simulacrum!", message.getClient());
                Entity newPlayer = builder.build();
                newPlayer.transferToWorld(World.getHubWorld());
                service.registerCommand(new LookCommand(null,newPlayer,service));
            }
        }
    }

    private String getCurrentStats(){
        return String.format(Locale.US, "[STR %d] [DEX %d] [INT %d] [WIS %d]",
                selectedRace.getBaseStr() + allocations[0],
                selectedRace.getBaseDex() + allocations[1],
                selectedRace.getBaseInt() + allocations[2],
                selectedRace.getBaseWis() + allocations[3]);
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch(Exception e) {
            return false;
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
    public boolean registerMessage(Client sourceClient, String[] messageArgs) {
        if(sourceClient.getAssociatedAccount() != null && sourceClient.getStatus() == Client.ClientStatus.ACTIVE) {
            lastUpdateTime = System.currentTimeMillis();

            newMessageArgs = messageArgs;
            refreshFlag = true;
            return true;
        }
        return false;
    }
}
