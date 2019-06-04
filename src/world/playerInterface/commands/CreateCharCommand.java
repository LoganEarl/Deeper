package world.playerInterface.commands;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.MessagePipeline;
import world.WorldUtils;
import world.entity.Entity;
import world.entity.EntityTable;
import world.entity.Race;
import world.meta.World;

import java.util.Arrays;
import java.util.Locale;

public class CreateCharCommand implements CommandExecutor.Command, MessagePipeline.MessageContext {
    private long lastUpdateTime = System.currentTimeMillis();
    private static final long EXPIRATION_TIME = 600000; //10 minutes

    private int stage = STAGE_START;

    private ClientRegistry registry;
    private Client sourceClient;
    private MessagePipeline pipeline;
    private CommandExecutor executor;

    private Entity.EntityBuilder builder = new Entity.EntityBuilder();
    private String[] newMessageArgs = null;

    private static final int STAGE_START = 0;
    private static final int STAGE_DISPLAY_NAME = 1;
    private static final int STAGE_RACE = 2;
    private static final int STAGE_STATS = 3;
    private static final int STAGE_VERIFY = 4;
    private static final int STAGE_COMPLETE = 5;
    private static final int STAGE_NO_USERNAME = -1;
    private static final int STAGE_EXISTING_CHARACTER = -2;
    private boolean refreshFlag = true;

    private String userName;
    private String selectedName;
    private Race selectedRace;

    private int[] allocations = {0,0,0,0};
    private String[] allocationNames = {"str","dex","int","wis"};
    private int pointsAvailable = 40;

    public CreateCharCommand(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline pipeline){
        this.registry = registry;
        this.sourceClient = sourceClient;
        this.pipeline = pipeline;
        this.executor = executor;

        if(sourceClient.getStatus() == Client.ClientStatus.ACTIVE)
            userName = sourceClient.getUserName();

        if(userName == null) {
            stage = STAGE_NO_USERNAME;
        }else{
            if(WorldUtils.getEntityOfClient(sourceClient) != null) {
                stage = STAGE_EXISTING_CHARACTER;
            }
            this.pipeline.addMessageContext(sourceClient,this);
            builder.setID(userName);
            builder.setDatabaseName(World.getHubWorld().getDatabaseName());
            builder.setRoomName(World.getHubWorld().getEntryRoomName());
            builder.setControllerType(EntityTable.CONTROLLER_TYPE_PLAYER);
        }
    }

    @Override
    public void execute() {
        if(refreshFlag) {
            refreshFlag = false;
            checkForUsername();
            checkForExistingCharacter();
            checkForStart();
            checkForDisplayName();
            checkForRace();
            checkForStats();
            checkForVerification();
        }
    }

    private void checkForUsername(){
        if (stage == STAGE_NO_USERNAME) {
            registry.sendMessage("You must be logged in to create a character", sourceClient);
            pipeline.removeMessageContext(sourceClient, this);
            stage = STAGE_COMPLETE;
        }
    }

    private void checkForExistingCharacter(){
        if(stage == STAGE_EXISTING_CHARACTER){
            if(newMessageArgs == null){
                registry.sendMessage("You already have a character associated to that account. Do you want to delete that character and start fresh? [yes/no]", sourceClient);
            }else if(newMessageArgs.length == 1 && newMessageArgs[0].equals("yes")){
                Entity preexisting = WorldUtils.getEntityOfClient(sourceClient);
                if(preexisting != null) {
                    if (World.deleteEntity(preexisting)) {
                        registry.sendMessage("Your old character has been deleted", sourceClient);
                        newMessageArgs = null;
                        stage = STAGE_START;
                    }else{
                        registry.sendMessage("Whoops. There is something preventing me from deleting your old character. Please contact an admin for help.", sourceClient);
                        newMessageArgs = null;
                        stage = STAGE_COMPLETE;
                    }
                }
            }else if(newMessageArgs.length == 1 && newMessageArgs[0].equals("no")){
                registry.sendMessage("Okay, your account has been preserved. Carry on I guess.", sourceClient);
                pipeline.removeMessageContext(sourceClient,this);
                newMessageArgs = null;
                stage = STAGE_COMPLETE;
            }else{
                registry.sendMessage("Im sorry I cannot understand that. \nDo you want to delete that character and start fresh? [yes/no]",sourceClient);
                newMessageArgs = null;
            }
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
                registry.sendMessage("Please enter the name you wish to go by", sourceClient);
            } else if(newMessageArgs.length == 1 &&
                    newMessageArgs[0].length() > 3 &&
                    newMessageArgs[0].chars().allMatch(Character::isAlphabetic)){
                builder.setDisplayName(newMessageArgs[0]);
                selectedName = newMessageArgs[0];
                registry.sendMessage("Very well, you will be known as " + newMessageArgs[0] + " from now on.", sourceClient);
                stage = STAGE_RACE;
                newMessageArgs = null;
            }else{
                registry.sendMessage("That name will not do. Please choose a name with 4 or more letters, made up of only letters", sourceClient);
            }
        }
    }

    private void checkForRace(){
        if(stage == STAGE_RACE) {
            if(newMessageArgs == null){
                registry.sendMessage("Please choose a race from the following...\n\n" +
                        Race.getPlayableRaceDescriptions(),sourceClient);
            }else if(newMessageArgs.length == 1 && (selectedRace = Race.getFromID(newMessageArgs[0])) != null){
                stage = STAGE_STATS;
                newMessageArgs = null;
            }else{
                registry.sendMessage("I did not understand that, Please choose a race from the following...\n\n" + Race.getPlayableRaceDescriptions(), sourceClient);
            }
        }
    }

    private void checkForStats(){
        if(stage == STAGE_STATS){
            int attributeIndex;

            if(newMessageArgs == null){
                registry.sendMessage("You have " + pointsAvailable +
                        " stat points available to allocate. Allocate points with: [add/subtract] [number of points] [from/to] [str/dex/int/wis]. Use [done] when complete.", sourceClient);
            }else if(newMessageArgs.length == 4 &&
                    (newMessageArgs[0].equals("add") || newMessageArgs[0].equals("subtract")) &&
                    isInteger(newMessageArgs[1]) &&
                    (newMessageArgs[2].equals("from") || newMessageArgs[2].equals("to")) &&
                    (attributeIndex = Arrays.asList(allocationNames).indexOf(newMessageArgs[3])) != -1){
                int points = Integer.parseInt(newMessageArgs[1]);
                if(newMessageArgs[0].equals("subtract")) points *= -1;
                if(pointsAvailable >= points && allocations[attributeIndex] + points >= 0){
                    allocations[attributeIndex] += points;
                    pointsAvailable -= points;
                    registry.sendMessage("You have " + pointsAvailable + " points remaining. Your stats are as follows\n" + getCurrentStats(),sourceClient);
                }else if(pointsAvailable < points)
                    registry.sendMessage("You do not have enough points for this allocation", sourceClient);
                else
                    registry.sendMessage("You cannot allocate less points to an attribute than your race's base stats",sourceClient);
            }else if(newMessageArgs.length == 1 && newMessageArgs[0].equals("done")){
                builder.setStrength(allocations[0] + selectedRace.getBaseStr());
                stage = STAGE_VERIFY;
                newMessageArgs = null;
            }else{
                registry.sendMessage("I do not understand. Allocate points with: [add/subtract] [number of points] [from/to] [str/dex/int/wis]. Use [done] when complete.",sourceClient);
            }
        }
    }

    private void checkForVerification(){
        if(stage == STAGE_VERIFY){
            if(newMessageArgs == null){
                registry.sendMessage(String.format(Locale.US, "Name: %s\nRace: %s\nStats: %s\n\nIs this correct? [yes/no]",selectedName,selectedRace.getDisplayName(),getCurrentStats()),sourceClient);
            }else if(newMessageArgs.length == 1 && newMessageArgs[0].equals("yes")){
                registry.sendMessage("Very well, your character creation is now complete. Welcome " + selectedName + " to the Simulacrum!", sourceClient);
                pipeline.removeMessageContext(sourceClient,this);
                Entity newPlayer = builder.build();
                newPlayer.transferToWorld(World.getHubWorld());
                executor.scheduleCommand(new LookCommand("",false, sourceClient,registry));
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
