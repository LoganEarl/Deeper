package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.world.WorldModel;
import main.java.world.entity.Entity;
import main.java.world.entity.skill.Skill;
import main.java.world.item.Item;
import main.java.world.item.ItemType;
import main.java.world.item.container.Container;
import main.java.world.playerInterface.ColorTheme;
import main.java.world.room.Domain;
import main.java.world.room.Room;
import main.java.world.room.RoomConnection;
import main.java.world.room.RoomDiscoveryToken;

import java.awt.*;
import java.util.*;
import java.util.List;

import static main.java.world.playerInterface.ColorTheme.*;

/**
 * command used to get item and room descriptions. Can also be used to look into unlocked containers.
 *
 * @author Logan Earl
 */
public class LookCommand extends EntityCommand {
    private final Client fromClient;
    private final String target;
    private final boolean lookInto;
    private final boolean extendedDetails;

    private boolean complete = false;

    public LookCommand(boolean extendedDetails, Client fromClient, WorldModel model) {
        this("",false,extendedDetails,fromClient,model);
    }

    /**
     *
     * @param target     the item/entity/container that is being inspected.
     * @param lookInto   true to look inside the selected thing. Does not apply to rooms
     * @param fromClient the main.java.client doing the looking.
     */
    public LookCommand(String target, boolean lookInto, boolean extendedDetails, Client fromClient, WorldModel model) {
        super(fromClient, model);

        this.fromClient = fromClient;
        this.target = target;
        this.lookInto = lookInto;
        this.extendedDetails = extendedDetails;
    }

    @Override
    public void executeEntityCommand() {
        String response;

        if (target == null || target.isEmpty())
            response = describeRoom(extendedDetails);
        else if (lookInto) {
            response = lookInContainer(target);
        } else {
            //TODO look at entity
            response = getMessageInColor("Not yet implemented", FAILURE);
        }

        fromClient.sendMessage(response);
        complete = true;
    }

    @Override
    protected boolean requiresBalance() {
        return true;
    }

    @Override
    protected void setBalance() {
        if (lookInto)
            super.setBalance();
    }

    private String lookInContainer(String target) {
        String response;
        Item rawItem = Item.getFromEntityContext(target, getSourceEntity(), getWorldModel().getItemFactory());
        if (rawItem != null) {
            if (rawItem.getItemType() == ItemType.container && rawItem instanceof Container) {
                Container container = (Container) rawItem;
                response = readContainer(container);
            } else {
                response = getMessageInColor("You peer closely at the " + getItemColored(rawItem) + ". ", INFORMATIVE) + getMessageInColor("It has no openings you can see.", FAILURE);
            }
        } else {
            response = getMessageInColor("You cannot find a " + target + " to look into", FAILURE);
        }

        return response;
    }

    private String readContainer(Container container) {
        if (container.getIsLocked()) {
            return getMessageInColor("It is locked. You are unable to discover it's contents", FAILURE);
        } else {
            List<Item> storedItems = container.getStoredItems();
            if (storedItems.size() == 0)
                return "It is empty";
            if (storedItems.size() == 1)
                return "It contains a " + storedItems.get(0).getDisplayableName();
            StringBuilder contents = new StringBuilder("The ");
            contents.append(container.getDisplayableName()).append(" contains the following:\n");
            for (Item i : storedItems)
                contents.append(i.getDisplayableName()).append("\n");
            return contents.toString();
        }
    }

    private String describeRoom(boolean extendedDomainDetails) {
        String roomName;
        String roomDesc;
        String waysDesc;
        String creaturesString;
        String itemsString;

        roomName = getSourceEntity().getRoomName();
        Room r = Room.getByRoomName(roomName, getSourceEntity().getDatabaseName());
        if (r == null) {
            System.out.println("Failed to get room info for look command sourced by entityID:" + getSourceEntity().getID() + " in database " + getSourceEntity().getDatabaseName());
            return getMessageInColor("An error has occurred. Unable to get room info for you at this time", FAILURE);
        }
        roomDesc = r.getRoomDescription();
        waysDesc = getWays(r, extendedDomainDetails);
        creaturesString = getCreatureString(r);
        itemsString = getItemsString(r);

        return String.format(Locale.US,
                             "%s\n\n%s\n\n%s\n" +
                                     "%s\n\n%s", roomName, roomDesc, waysDesc, creaturesString, itemsString);
    }

    private void rollToDetectHiddenWays(Room currentRoom) {
        List<RoomConnection> detectionCandidates = currentRoom.getOutgoingConnectionsFromPOV(getSourceEntity(), RoomDiscoveryToken.DetectionStatus.unencountered);
        for (RoomConnection connection : detectionCandidates) {
            RoomDiscoveryToken token = RoomDiscoveryToken.getToken(getSourceEntity().getID(), connection.getConnectionID(), connection.getDatabaseName());
            long retryTimestamp = connection.getDetectCooldownSeconds() * 1000 + token.getLastUpdate();
            long time = System.currentTimeMillis();
            if (time > retryTimestamp) {
                //roll perception
                int result = getSourceEntity().getSkills().performSkillCheck(Skill.perception1, connection.getDetectDifficulty() - 10);
                if (result >= 0) {
                    getSourceClient().sendMessage(ColorTheme.getMessageInColor("You noticed a hidden passageway! (Perception:" + result + ")", SUCCESS));
                    token.update(RoomDiscoveryToken.DetectionStatus.known, time);
                } else {
                    token.hide(0);
                }
            }
        }
    }

    private String getWays(Room r, boolean extendedDomainDetails) {
        rollToDetectHiddenWays(r);
        List<RoomConnection> connections = r.getOutgoingConnectionsFromPOV(getSourceEntity(), RoomDiscoveryToken.DetectionStatus.known);
        Collections.sort(connections);

        if (connections.size() == 0)
            return getMessageInColor("There is no way out", WARNING);
        else {
            StringBuilder message = new StringBuilder();
            for (int i = 0; i < connections.size(); i++) {
                RoomConnection connection = connections.get(i);
                String hiddenText = connection.getDetectDifficulty() != null ? " [hidden:" + connection.getDetectDifficulty() + "] " : "";

                String connectionPortion = (String.format(Locale.US, "[%d]%s%s%s%s", i,
                                                          hiddenText,
                                                          extendedDomainDetails ? " " : getDomainTransferInfo(connection, false),
                                                          connection.getDisplayName(),
                                                          extendedDomainDetails ? getDomainTransferInfo(connection, true) : ""));

                Skill requiredSkill = connection.getTraverseSkill();
                if (requiredSkill != null) {
                    int skillBonus = getSourceEntity().getSkills().getSkillBonus(requiredSkill);
                    int statBase = getSourceEntity().getStats().getStat(requiredSkill.getAssociatedStat());

                    Color messageColor = ColorTheme.getColorOfRollDifficulty(skillBonus + statBase, connection.getTraverseDifficulty());
                    connectionPortion = ColorTheme.getMessageInColor(connectionPortion, messageColor);
                }
                message.append(connectionPortion).append("\n");
            }
            return getMessageInColor(message.toString(), INFORMATIVE);
        }
    }

    private String getDomainTransferInfo(RoomConnection connection, boolean extendedDetails) {
        Domain currentDomain = getSourceEntity().getDomain();
        Map<Domain, Domain> domainMapToDisplay = new HashMap<>();
        Domain defaultDestination = connection.getDestinationDomains().get(0);
        connection.getSourceDomains().forEach((domain ->
                domainMapToDisplay.put(domain, connection.getDestinationDomains().contains(domain) ? domain : defaultDestination)));

        if (extendedDetails) {
            StringBuilder builder = new StringBuilder("\nDomains:\n");
            for (Domain domain : connection.getSourceDomains()) {
                builder.append(String.format(Locale.US, "    %s%15s%s => %-15s\n",
                                             domain == currentDomain ? "[" : "",
                                             domain.name(),
                                             domain == currentDomain ? "]" : "",
                                             domainMapToDisplay.get(domain)
                ));
            }
            return builder.toString();
        } else
            return String.format(Locale.US, " [%s => %s] ", currentDomain, domainMapToDisplay.get(currentDomain));
    }

    private String getCreatureString(Room r) {
        List<Entity> nearEntities = getWorldModel().getEntityCollection().getEntitiesInRoom(r.getRoomName(), r.getDatabaseName(), getSourceEntity().getID());
        if (nearEntities.size() == 0)
            return getMessageInColor("You are alone", INFORMATIVE);
        else {
            StringBuilder creatureStringBuilder = new StringBuilder();
            boolean first = true;
            for (Entity e : nearEntities) {
                if (first)
                    first = false;
                else
                    creatureStringBuilder.append("\n");

                creatureStringBuilder.append(getEntityColored(e, getSourceEntity(), getWorldModel())).append(getMessageInColor(" is nearby", INFORMATIVE));
            }
            return creatureStringBuilder.toString();
        }
    }

    private String getItemsString(Room r) {
        List<Item> nearItems = Item.getItemsInRoom(r.getRoomName(), getWorldModel().getItemFactory(), r.getDatabaseName());
        if (nearItems.size() == 0)
            return "";
        else {
            StringBuilder itemStringBuilder = new StringBuilder();
            boolean first = true;
            for (Item i : nearItems) {
                if (first)
                    first = false;
                else
                    itemStringBuilder.append("\n");
                itemStringBuilder.append("There is a ").append(getItemColored(i)).append(" nearby");
                if (i instanceof Container) {
                    itemStringBuilder.append(". It is ").append(getMessageInColor((((Container) i).getIsLocked() ? "locked" : "unlocked"), INFORMATIVE));
                }
            }
            return itemStringBuilder.toString();
        }
    }

    @Override
    public boolean entityCommandIsComplete() {
        return complete;
    }
}
