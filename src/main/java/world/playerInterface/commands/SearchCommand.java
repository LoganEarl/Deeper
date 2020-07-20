package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.world.WorldModel;
import main.java.world.diplomacy.DiplomaticRelation;
import main.java.world.diplomacy.Faction;
import main.java.world.entity.Entity;
import main.java.world.entity.skill.Skill;
import main.java.world.meta.World;
import main.java.world.notification.ConcreteNotification;
import main.java.world.playerInterface.ColorTheme;
import main.java.world.room.Room;
import main.java.world.room.RoomConnection;
import main.java.world.room.RoomDiscoveryToken;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static main.java.world.diplomacy.DiplomaticRelation.allied;
import static main.java.world.diplomacy.DiplomaticRelation.friendly;
import static main.java.world.playerInterface.ColorTheme.*;
import static main.java.world.room.RoomDiscoveryToken.DetectionStatus.known;
import static main.java.world.room.RoomDiscoveryToken.DetectionStatus.undetected;

public class SearchCommand extends EntityCommand {
    private boolean complete = false;

    public static int MAX_REPEAT_SEARCH_DEBUFF = -50;

    public SearchCommand(Client sourceClient, WorldModel model) {
        super(sourceClient, model);
    }

    @Override
    protected boolean requiresBalance() {
        return true;
    }

    @Override
    protected boolean entityCommandIsComplete() {
        return complete;
    }

    @Override
    protected void executeEntityCommand() {
        Entity entity = getSourceEntity();
        Room currentRoom = Room.getByRoomName(entity.getRoomName(), entity.getDatabaseName());

        if (currentRoom == null) {
            getSourceClient().sendMessage(getMessageInColor("An error has occurred and you no longer have a location.", ColorTheme.FAILURE));
            World entityWorld = World.getWorldOfEntityID(entity.getID());
            if (entityWorld != null) {
                getSourceClient().sendMessage(getMessageInColor("You are being transferred to the entry point of your world", ColorTheme.INFORMATIVE));
                entity.setRoom(entityWorld.getEntryRoomName());
            }
            entity.saveToDatabase(entity.getDatabaseName());
            return;
        }

        long time = System.currentTimeMillis();

        List<RoomConnection> findableConnections = currentRoom.getOutgoingConnectionsFromPOV(entity, undetected);
        final List<RoomConnection> foundConnections = new ArrayList<>();
        boolean cooldownDebuffActive = false;

        int roll = entity.getSkills().performSkillCheck(Skill.perception1, 0, entity);
        for (RoomConnection connection : findableConnections) {
            RoomDiscoveryToken token = RoomDiscoveryToken.getToken(entity.getID(), connection.getConnectionID(), entity.getDatabaseName());
            int debuff = getRecentSearchDebuff(
                    token.getLastUpdate() / 1000,
                    time / 1000,
                    connection.getDetectCooldownSeconds());

            if (debuff < 0) cooldownDebuffActive = true;

            if (roll + debuff + connection.getDetectDifficulty() >= 0) {
                token.update(known, time);
                RoomConnection inverse = connection.getInverseRoomConnection();
                if(inverse != null)
                    RoomDiscoveryToken.revealConnection(entity.getID(), inverse.getConnectionID(), time, entity.getDatabaseName());
                foundConnections.add(connection);
            } else {
                token.update(undetected, time);
            }
        }

        getSourceClient().sendMessage(getMessageInColor("You search the room for secrets (Perception:" + roll + ")", INFORMATIVE));
        final String a = foundConnections.size() == 1? "a ": "";
        final String plural = foundConnections.size() > 1? "s!":"!";

        if(foundConnections.size() == 0){
            String debuffUncertainty = cooldownDebuffActive? ", however, you were somewhat distracted by frustration at " + getMessageInColor("searching the room again so soon.",WARNING): ".";
            getSourceClient().sendMessage("You are reasonably certain there is " + getMessageInColor("nothing to find in this room", INFORMATIVE) + debuffUncertainty);
        }else{
            getSourceClient().sendMessage(getMessageInColor("You have found " + a + foundConnections.size() + " hidden passageway" + plural, SUCCESS));
        }

        final boolean debuffsActive = cooldownDebuffActive;
        notifyEntityRoom(new ConcreteNotification(getWorldModel().getRegistry()) {
            @Override
            public String getAsMessage(Entity viewer) {
                Faction viewedFaction = entity.getDiplomacy().getFaction();
                DiplomaticRelation relation = getWorldModel().getDiplomacyManager().getRelation(viewedFaction,viewer.getDiplomacy().getFaction());

                boolean notices = relation == allied || relation == friendly;
                if(!notices){
                    int result = entity.getSkills().performSkillCheck(Skill.obscureIntent1,0, entity) - viewer.getSkills().performSkillCheck(Skill.perception1,0, entity);
                    if(result <= 0){
                        getSourceClient().sendMessage(getMessageInColor("You obscured your searching from " + getEntityColored(entity, viewer, getWorldModel()), SUCCESS));
                    }else{
                        getSourceClient().sendMessage(getMessageInColor( getEntityColored(entity, viewer, getWorldModel()) + " has noticed your searching", SUCCESS));
                        notices = true;
                    }
                }

                String message = "";
                if(notices) {
                    message = getEntityColored(entity, viewer, getWorldModel()) + " searches the room. ";
                    if (debuffsActive)
                        message += getMessageInColor(entity.getPronoun(), relation) + " seem annoyed at searching the room again so soon.\n";

                    if (foundConnections.size() > 0) {
                        message += getEntityColored(entity, viewer, getWorldModel()) + " has found " + a + foundConnections.size() + " hidden passage" + plural + "\n";
                        for(RoomConnection connection: foundConnections) {
                            RoomDiscoveryToken.revealConnection(viewer.getID(), connection.getConnectionID(), time, entity.getDatabaseName());
                            RoomConnection inverse = connection.getInverseRoomConnection();
                            if (inverse != null)
                                RoomDiscoveryToken.revealConnection(entity.getID(), inverse.getConnectionID(), time, entity.getDatabaseName());
                        }
                    } else {
                        message += getMessageInColor("The search was in vain",INFORMATIVE);
                    }
                }
                return message;
            }
        },entity.getID());

        if(foundConnections.size() > 0)
            new LookCommand(false,getSourceClient(),getWorldModel()).execute();

        complete = true;
    }

    private static int getRecentSearchDebuff(long lastTimeSeconds, long curTimeSeconds, long cooldownSeconds) {
        double elapsedPercentage = (curTimeSeconds - lastTimeSeconds) / (double) cooldownSeconds;
        if (elapsedPercentage > 1) elapsedPercentage = 1;
        if (elapsedPercentage < 0) elapsedPercentage = 0;
        elapsedPercentage = 1- elapsedPercentage;
        elapsedPercentage = Math.pow(elapsedPercentage - 1, 3) + 1;
        return (int) (elapsedPercentage * MAX_REPEAT_SEARCH_DEBUFF);
    }

    @Override
    protected void setBalance() {
        getSourceEntity().setBalanceTime(4000, getSourceClient());
    }

    @Override
    protected boolean canDoWhenDying() {
        return false;
    }

    @Override
    protected boolean canDoWhenDead() {
        return false;
    }
}
