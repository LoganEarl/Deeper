package world.playerInterface.commands;

import client.Account;
import client.Client;
import network.CommandExecutor;
import world.WorldModel;
import world.WorldUtils;
import world.entity.Entity;
import world.meta.World;

public class TransferEntityCommand extends EntityCommand {
    private String entityID;
    private String worldID;
    private boolean complete = false;
    private CommandExecutor executor;

    public TransferEntityCommand(String entityID, String worldID, Client sourceClient, WorldModel model) {
        super(sourceClient, model);

        this.entityID = entityID;
        this.worldID = worldID;
        this.executor = model.getExecutor();
    }

    @Override
    protected boolean entityCommandIsComplete() {
        return complete;
    }

    @Override
    protected void executeEntityCommand() {
        if(!WorldUtils.isAuthorized(getSourceClient(),Account.AccountType.ADMIN)) {
            getSourceClient().sendMessage(WorldUtils.getRefusedFlavorText());
        }else {
            Entity toTransfer = getWorldModel().getEntityCollection().getPlayableEntityByID(entityID);
            if (toTransfer == null)
                toTransfer = getWorldModel().getEntityCollection().getEntityByDisplayName(entityID, getSourceEntity().getRoomName(), getSourceEntity().getDisplayName());
            World transferTo;
            try {
                transferTo = World.getWorldByWorldID(Integer.parseInt(worldID));
            } catch (NumberFormatException ignored) {
                transferTo = null;
            }
            if (World.getLimboWorld().getName().toLowerCase().equals(worldID.toLowerCase()))
                transferTo = World.getLimboWorld();
            if (World.getHubWorld().getName().toLowerCase().equals(worldID.toLowerCase()))
                transferTo = World.getHubWorld();

            if (toTransfer != null) {
                if (transferTo != null) {
                    int result = toTransfer.transferToWorld(transferTo);
                    if (result == Entity.CODE_ALREADY_EXISTS_AT_DESTINATION)
                        getSourceClient().sendMessage("Unable to move you there. It looks like an entity with the same account as you already exists there");
                    else if (result != Entity.CODE_TRANSFER_COMPLETE)
                        getSourceClient().sendMessage("Unable to move that entity there. Code: " + result);
                    else {
                        getSourceClient().sendMessage("You feel the space around you twist and warp. With resonate tearing sound, your vision twists and contorts. As it stops, you look around");
                        executor.scheduleCommand(new LookCommand("", false, getSourceClient(), getWorldModel()));
                    }
                } else {
                    getSourceClient().sendMessage("I don't see a world with ID: " + worldID);
                }
            } else
                getSourceClient().sendMessage("I don't see an player here with ID: " + entityID);
        }
        complete = true;
    }

    @Override
    protected boolean requiresBalance() {
        return true;
    }
}
