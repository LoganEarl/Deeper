package world.playerInterface.commands;

import client.Client;
import client.ClientRegistry;
import world.WorldModel;
import world.entity.Entity;
import world.item.Item;
import world.item.ItemType;
import world.item.container.Container;
import world.notification.Notification;
import world.notification.NotificationScope;
import world.notification.NotificationSubscriber;
import world.room.RoomNotificationScope;

import static world.playerInterface.ColorTheme.*;

public class LockContainerCommand extends EntityCommand {
    private boolean complete = false;

    private String containerIDName, itemIDName;
    private boolean desiredLockState;

    public LockContainerCommand(String containerIDName, String itemIDName, boolean desiredLockState, Client sourceClient, WorldModel model) {
        super(sourceClient, model);

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
        String lock = desiredLockState ? "lock" : "unlock";

        Container targetContainer = parseContainer(containerIDName);
        Item targetKey = parseKey(itemIDName);
        if (targetContainer != null && targetKey != null) {
            if (targetContainer.getIsLocked() == desiredLockState)
                getSourceClient().sendMessage("The " + targetContainer.getDisplayableName() + " is already " + lock + "ed");
            else if (targetContainer.getLockNumber() != targetKey.getLockNumber()){
                Notification notification = new ContainerLockedNotification(getSourceEntity(),targetKey,targetContainer,desiredLockState,false, getWorldModel().getRegistry());
                NotificationScope scope = new RoomNotificationScope(getSourceEntity().getRoomName(), getSourceEntity().getDatabaseName());
                getWorldModel().getNotificationService().notify(notification,scope);
            }else if (targetContainer.setLockedWithItem(targetKey, desiredLockState)) {
                Notification notification = new ContainerLockedNotification(getSourceEntity(),targetKey,targetContainer,desiredLockState,true, getWorldModel().getRegistry());
                NotificationScope scope = new RoomNotificationScope(getSourceEntity().getRoomName(), getSourceEntity().getDatabaseName());
                getWorldModel().getNotificationService().notify(notification,scope);
            } else {
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
        Item rawContainer = Item.getFromEntityContext(identifier, getSourceEntity(), getWorldModel().getItemFactory());

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

    private Item parseKey(String identifier) {
        Item rawItem = Item.getFromEntityContext(identifier, getSourceEntity(), getWorldModel().getItemFactory());

        if (rawItem != null) {
            if (rawItem.getLockNumber() > 0)
                return rawItem;
            else {
                getSourceClient().sendMessage("The " + identifier + " is not a key");
                return null;
            }
        } else {
            getSourceClient().sendMessage("You cannot find the " + identifier + " here");
            return null;
        }
    }

    public class ContainerLockedNotification extends Notification {
        private Entity sourceEntity;
        private Container targetContainer;
        private Item targetKey;
        private boolean wasLocked;
        private boolean wasSuccessful;

        public ContainerLockedNotification(Entity sourceEntity, Item targetKey, Container targetContainer, boolean wasLocked, boolean wasSuccessful, ClientRegistry registry) {
            super(registry);

            this.targetContainer = targetContainer;
            this.sourceEntity = sourceEntity;
            this.targetKey = targetKey;
            this.wasLocked = wasLocked;
            this.wasSuccessful = wasSuccessful;
        }

        @Override
        public String getAsMessage(Entity viewer) {
            String message;
            String lock = desiredLockState ? "lock" : "unlock";
            if (viewer.equals(sourceEntity)) {
                if (wasSuccessful)
                    message = getMessageInColor("You slide the " + getItemColored(targetKey) + " into the lock and it " + lock + "s with a turn and a click", SUCCESS);
                else
                    message = getMessageInColor("You cannot seem to fit the " + getItemColored(targetKey) + " into the lock", FAILURE);
            } else {
                if(wasSuccessful)
                    message = getEntityColored(viewer,sourceEntity,getWorldModel()) + " " + lock + "s a " + getItemColored(targetContainer) + " with a " + getItemColored(targetKey);
                else
                    message = getEntityColored(viewer,sourceEntity,getWorldModel()) + " tries to " + lock + " a " + getItemColored(targetContainer) + " with a " + getItemColored(targetKey);
            }

            return message;
        }
    }
}
