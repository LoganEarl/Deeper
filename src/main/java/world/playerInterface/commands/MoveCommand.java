package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.client.ClientRegistry;
import main.java.world.WorldModel;
import main.java.world.WorldUtils;
import main.java.world.entity.Entity;
import main.java.world.entity.skill.Skill;
import main.java.world.notification.ConcreteNotification;
import main.java.world.playerInterface.ColorTheme;
import main.java.world.playerInterface.MessageSubstitutor;
import main.java.world.room.Domain;
import main.java.world.room.Room;
import main.java.world.room.RoomConnection;
import main.java.world.room.RoomConnection.Direction;

import java.awt.*;

import static main.java.world.playerInterface.ColorTheme.*;

public class MoveCommand extends EntityCommand {
    private final String direction;
    private boolean complete = false;
    private final int staminaNeeded;
    private int staminaUsed = 0;

    public MoveCommand(String direction, Client sourceClient, WorldModel model) {
        super(sourceClient, model);
        this.direction = direction;

        //check the entity status first, we don't know if we have one or not until executeEntityCommand() is called
        if (getSourceEntity() != null && !getSourceEntity().getEquipment().isEncumbered())
            staminaNeeded = 0;
        else
            staminaNeeded = 10;
    }

    @Override
    protected void executeEntityCommand() {
        Entity sourceEntity = getSourceEntity();
        Room curRoom = Room.getByRoomName(sourceEntity.getRoomName(), sourceEntity.getDatabaseName());
        if (curRoom != null) {
            try {
                int parsedIndex = Integer.parseInt(direction);
                RoomConnection roomConnection = curRoom.getOutgoingConnectionByIndex(parsedIndex, getSourceEntity());
                if (roomConnection != null) {
                    travel(roomConnection);
                } else
                    getSourceClient().sendMessage(direction + " is not an option you can choose from. Please try again.");
            } catch (NumberFormatException e) {
                getSourceClient().sendMessage(direction + " is not an option you can choose from. Please try again.");
            }
        } else
            getSourceClient().sendMessage("This is embarrassing. I regret to inform you that you do not currently have a location. Very strange. I suggest getting in contact with an admin. Weird");
        complete = true;
    }

    private void travel(RoomConnection roomConnection) {
        Entity entity = getSourceEntity();
        //TODO find a way to automatically reveal rooms you enter another room through
        //TODO add a way to unlock rooms
        if (roomConnection.getState() == RoomConnection.State.locked) {
            getSourceClient().sendMessage(getMessageInColor("The way is barred", FAILURE));
            return;
        }

        if (roomConnection.getState() == RoomConnection.State.impassible) {
            getSourceClient().sendMessage(getMessageInColor("The way is impassible", FAILURE));
            return;
        }

        Domain currentDomain = entity.getDomain();
        if(!roomConnection.getSourceDomains().contains(currentDomain)){
            String message = "That way is only accessible from the ";
            message += WorldUtils.commaSeparate(roomConnection.getSourceDomains());

            getSourceClient().sendMessage(getMessageInColor(message, FAILURE));
        }

        boolean didMove;
        Direction moveDirection = roomConnection.getDirection();
        boolean didGetCheckMessage = false;
        Room destinationRoom = Room.getByRoomName(roomConnection.getDestRoomName(), roomConnection.getDatabaseName());
        if (roomConnection.getTraverseDifficulty() != 0) {
            //we need to make a skill check
            Skill requiredSkill = roomConnection.getTraverseSkill();

            int result = entity.getSkills().performSkillCheck(requiredSkill, roomConnection.getTraverseDifficulty());

            didGetCheckMessage = true;
            notifyEntityRoom(new TransferSkillCheckNotification(
                    (result >= 0 ? roomConnection.getSuccessMessage() : roomConnection.getFailureMessage()),
                    result,
                    entity,
                    getWorldModel().getRegistry()));

            if (result < 0) {
                //failed to travel
                if(roomConnection.getFailureRoomName() != null){
                    destinationRoom = Room.getByRoomName(roomConnection.getFailureRoomName(),roomConnection.getDatabaseName());
                    moveDirection = roomConnection.getFailureDirection();
                    didMove = true;
                }else
                    didMove = false;
            }else {
                didMove = true;
                moveDirection = roomConnection.getDirection();
            }
        }else {
            didMove = true;
            moveDirection = roomConnection.getDirection();
        }

        if(destinationRoom == null){
            System.out.printf("Entity %s tried to move to nonexistent room with name %s\n", entity.getDatabaseName(), roomConnection.getDestRoomName());
            return;
        }

        staminaUsed = staminaNeeded;
        if(didMove) {
            if(!didGetCheckMessage)
                notifyEntityRoom(new TransferRoomNotification(getSourceEntity(), false, roomConnection, moveDirection, getSourceEntity().getDomain(), getWorldModel().getRegistry()), getSourceEntity().getID());
            entity.setRoom(destinationRoom);
            entity.updateInDatabase(entity.getDatabaseName());
            notifyEntityRoom(new TransferRoomNotification(getSourceEntity(), true, roomConnection, moveDirection, getSourceEntity().getDomain(), getWorldModel().getRegistry()), getSourceEntity().getID());
            new LookCommand("", false,false, getSourceClient(), getWorldModel()).execute();
        }
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

    public class TransferSkillCheckNotification extends ConcreteNotification {
        private final String message;
        private final int result;
        private final Entity sourceEntity;

        public TransferSkillCheckNotification(String message, int result, Entity sourceEntity, ClientRegistry registry) {
            super(registry);
            this.message = message;
            this.result = result;
            this.sourceEntity = sourceEntity;
        }

        @Override
        public String getAsMessage(Entity viewer) {
            Color color;
            String rollIndicator = "";
            if (viewer.equals(sourceEntity)) {
                color = result >= 0 ? SUCCESS : FAILURE;
                rollIndicator = " (" + (result >= 0 ? "+" + result : result) + ")";
            } else
                color = INFORMATIVE;

            return ColorTheme.getMessageInColor(MessageSubstitutor.insertEntities(viewer, message + rollIndicator, getWorldModel(), sourceEntity), color);
        }
    }

    public class TransferRoomNotification extends ConcreteNotification {
        private final Entity sourceEntity;
        private final RoomConnection connectionUsed;
        private final Domain sourceDomain;
        private final Direction directionMoved;
        private final boolean didEnter;

        public TransferRoomNotification(Entity sourceEntity, boolean didEnter, RoomConnection connectionUsed, Direction directionMoved, Domain sourceDomain, ClientRegistry registry) {
            super(registry);
            this.connectionUsed = connectionUsed;
            this.sourceDomain = sourceDomain;
            this.didEnter = didEnter;
            this.sourceEntity = sourceEntity;
            this.directionMoved = directionMoved;
        }

        @Override
        public String getAsMessage(Entity viewer) {
            String arrives = didEnter ? " from " : " to ";
            Direction directionViewed = didEnter? directionMoved.opposite(): directionMoved;
            return getMessageInColor(getEntityColored(sourceEntity, viewer, getWorldModel()) + " " + sourceDomain.getTravelVerb() + arrives + directionViewed.toString(), INFORMATIVE);
        }
    }
}
