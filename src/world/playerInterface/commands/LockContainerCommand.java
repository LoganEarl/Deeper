package world.playerInterface.commands;

import client.Client;
import world.item.Item;
import world.item.ItemType;
import world.item.container.Container;

public class LockContainerCommand extends EntityCommand {
    private boolean complete = false;

    private String containerIDName, itemIDName;
    private boolean desiredLockState;

    public LockContainerCommand(String containerIDName, String itemIDName, boolean desiredLockState, Client sourceClient) {
        super(sourceClient);

        this.desiredLockState = desiredLockState;
        this.containerIDName = containerIDName;
        this.itemIDName = itemIDName;
    }

    @Override
    protected boolean entityCommandIsComplete() {
        return complete;
    }

    @Override
    protected void executeEntityCommand() {
        String lock = desiredLockState? "lock":"unlock";

        Container targetContainer = parseContainer(containerIDName);
        Item targetKey = parseKey(itemIDName);
        if(targetContainer != null && targetKey != null){
            if(targetContainer.getIsLocked() == desiredLockState)
                getSourceClient().sendMessage("The " + targetContainer.getDisplayableName() + " is already " + lock + "ed");
            else if(targetContainer.getLockNumber() != targetKey.getLockNumber())
                getSourceClient().sendMessage("You cannot seem to fit the " + targetKey.getDisplayableName() + " into the lock");
            else if(targetContainer.setLockedWithItem(targetKey,desiredLockState)){
                getSourceClient().sendMessage("You slide the " + targetKey.getDisplayableName() + " into the lock and it " + lock + "s with a turn and a click");
            }else{
                getSourceClient().sendMessage("You are unable to " + lock + " the " + targetContainer.getDisplayableName());
            }
        }

        complete = true;
    }

    @Override
    protected boolean requiresBalance() {
        return true;
    }

    private Container parseContainer(String identifier) {
        Item rawContainer = Item.getFromEntityContext(identifier, getSourceEntity());

        if (rawContainer != null) {
            if (rawContainer.getItemType().equals(ItemType.container) && rawContainer instanceof Container && rawContainer.getLockNumber() > 0)
                return (Container) rawContainer;
            else {
                getSourceClient().sendMessage("The " + identifier + " is not a container or cannot be locked");
                return null;
            }
        } else {
            getSourceClient().sendMessage("You cannot find the " + identifier + " here");
            return null;
        }
    }

    private Item parseKey(String identifier){
        Item rawItem = Item.getFromEntityContext(identifier, getSourceEntity());

        if(rawItem != null){
            if(rawItem.getLockNumber() > 0)
                return rawItem;
            else{
                getSourceClient().sendMessage("The " + identifier + " is not a key");
                return null;
            }
        }else {
            getSourceClient().sendMessage("You cannot find the " + identifier + " here");
            return null;
        }
    }


}
