package world.playerInterface.commands;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.MessagePipeline;
import world.WorldModel;
import world.WorldUtils;
import world.entity.Entity;
import world.entity.EntityTable;
import world.entity.race.Race;
import world.meta.World;
import world.notification.NotificationService;

import java.util.Arrays;
import java.util.Locale;

/**
 * will go through the process of creating a new character. If the player already has a character, it will given them the option
 * of deleting the old one. Can be used on a client that is not logged in, although it just tells them to log in and then quits.
 * Serves as a consuming {@link network.messaging.MessagePipeline.MessageContext} until the character creation sequence is done.
 * So the player will not be able to do anything else while creating a character. The command will timeout after 10 minutes has passed
 * @author Logan Earl
 */
public class CreateCharCommand implements CommandExecutor.Command, MessagePipeline.MessageContext {
    private long lastUpdateTime = System.currentTimeMillis();
    private static final long EXPIRATION_TIME = 600000; //10 minutes

    private int stage = STAGE_START;

    private WorldModel model;
    private Client sourceClient;
    private MessagePipeline pipeline;
    private ClientRegistry registry;

    private Entity.EntityBuilder builder;
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

    private int[] allocations = {0,0,0,0,0,0};
    private String[] allocationNames = {"str","dex","int","wis", "fit", "tough"};
    private int pointsAvailable = 20;

    /**
     * cole constructor
     * @param sourceClient the client attempting to make a new character
     * @param pipeline the messaging pipeline the client's commands are parsed through. Used to establish a custom
     *                 MessageContext
     * @see network.messaging.MessagePipeline.MessageContext
     */
    public CreateCharCommand(Client sourceClient, MessagePipeline pipeline, WorldModel model){
        this.builder =  new Entity.EntityBuilder(model);
        this.sourceClient = sourceClient;
        this.pipeline = pipeline;

        this.model = model;
        this.registry = model.getRegistry();

        if(sourceClient.getStatus() == Client.ClientStatus.ACTIVE)
            userName = sourceClient.getUserName();

        if(userName == null) {
            stage = STAGE_NO_USERNAME;
        }else{
            if(WorldUtils.getEntityOfClient(sourceClient, model) != null) {
                stage = STAGE_EXISTING_CHARACTER;
            }
            this.pipeline.addMessageContext(sourceClient,this);
            builder.setID(userName);
            builder.setDatabaseName(World.getHubWorld().getDatabaseName());
            builder.setRoomName(World.getHubWorld().getEntryRoomName());
            builder.setControllerType(EntityTable.CONTROLLER_TYPE_PLAYER);
        }
    }

    /**Run periodically. Quits instantly if the user has not done anything recently. If they have, it will parse and respond to the message here so as to be thread safe*/
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
            model.getRegistry().sendMessage("You must be logged in to create a character", sourceClient);
            stage = STAGE_COMPLETE;
        }
    }

    private void checkForExistingCharacter(){
        if(stage == STAGE_EXISTING_CHARACTER){
            if(newMessageArgs == null){
                registry.sendMessage("You already have a character associated to that account. Do you want to delete that character and start fresh? [yes/no]", sourceClient);
            }else if(newMessageArgs.length == 1 && newMessageArgs[0].equals("yes")){
                Entity preexisting = WorldUtils.getEntityOfClient(sourceClient, model);
                if(preexisting != null) {
                    this.model.getNotificationService().unsubscribe(preexisting);
                    if (World.deleteEntity(preexisting)) {
                        preexisting.removeFromDatabase(preexisting.getDatabaseName());
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
            }else if(newMessageArgs.length == 1 && (selectedRace = Race.getFromID(newMessageArgs[0],World.getHubWorld().getDatabaseName())) != null){
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
                        " stat points available to allocate. Allocate points with: [add/subtract] [number of points] [from/to] [str/dex/int/wis/fit/tough]. Use [done] when complete.", sourceClient);
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
                builder.setDexterity(allocations[1] + selectedRace.getBaseDex());
                builder.setIntelligence(allocations[2] + selectedRace.getBaseInt());
                builder.setWisdom(allocations[3] + selectedRace.getBaseWis());
                builder.setFitness(allocations[4] + selectedRace.getBaseFit());
                builder.setToughness(allocations[5] + selectedRace.getBaseTough());
                stage = STAGE_VERIFY;
                newMessageArgs = null;
            }else{
                registry.sendMessage("I do not understand. Allocate points with: {add/subtract} [number of points] {from/to} {str/dex/int/wis/fit/tough}. Use 'done' when complete.",sourceClient);
            }
        }
    }

    private void checkForVerification(){
        if(stage == STAGE_VERIFY){
            if(newMessageArgs == null){
                registry.sendMessage(String.format(Locale.US, "Name: %s\nRace: %s\nStats: %s\n\nIs this correct? [yes/no]",selectedName,selectedRace.getDisplayName(),getCurrentStats()),sourceClient);
            }else if(newMessageArgs.length == 1 && newMessageArgs[0].equals("yes")){
                registry.sendMessage("Very well, your character creation is now complete. Welcome " + selectedName + " to the Simulacrum!", sourceClient);
                Entity newPlayer = builder.build();
                newPlayer.transferToWorld(World.getHubWorld());
                model.getNotificationService().subscribe(newPlayer);
                model.getExecutor().scheduleCommand(new LookCommand("",false, sourceClient, model));
                stage = STAGE_COMPLETE;
            }
        }
    }

    private String getCurrentStats(){
        return String.format(Locale.US, "[STR %d] [DEX %d] [INT %d] [WIS %d] [FIT %d] [TOUGH %d]",
                selectedRace.getBaseStr() + allocations[0],
                selectedRace.getBaseDex() + allocations[1],
                selectedRace.getBaseInt() + allocations[2],
                selectedRace.getBaseWis() + allocations[3],
                selectedRace.getBaseFit() + allocations[4],
                selectedRace.getBaseTough() + allocations[5]);
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
        boolean complete = stage == STAGE_COMPLETE || getTimeToExpire() < 0;
        if(complete) pipeline.removeMessageContext(sourceClient,this);
        return complete;
    }

    @Override
    public long getTimeToExpire() {
        return EXPIRATION_TIME - (System.currentTimeMillis() - lastUpdateTime);
    }

    /**
     * registers messages from the chosen client. This class serves as a MessageContext during character creation so
     * messages from the client come here before parsing. If our client is talking to us before logging in we do not
     * consume the result and ignore it.
     * @param sourceClient the client that sent the message
     * @param messageArgs  the message that was sent
     * @return true if the result was processed here and should be consumed. False to release it to other contexts
     */
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
