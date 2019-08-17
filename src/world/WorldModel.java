package world;

import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.MessagePipeline;
import world.diplomacy.DiplomacyManager;
import world.entity.EntityCollection;
import world.entity.pool.EntityRegenCommand;
import world.item.ItemCollection;
import world.item.ItemFactory;
import world.item.container.Container;
import world.item.misc.MiscItem;
import world.item.weapon.Weapon;
import world.notification.NotificationService;
import world.playerInterface.messages.*;

public class WorldModel {
    private NotificationService notificationService;
    private CommandExecutor executor;
    private ClientRegistry registry;
    private ItemFactory itemFactory;
    private ItemCollection itemCollection;
    private EntityCollection entityCollection;
    private DiplomacyManager diplomacyManager;

    public WorldModel(CommandExecutor executor, ClientRegistry registry){
        this.executor = executor;
        this.registry = registry;
        notificationService = new NotificationService(registry, executor);
        notificationService.attachToExecutor(executor);

        itemFactory = new ItemFactory();
        itemFactory.addParser(Weapon.factory());
        itemFactory.addParser(MiscItem.factory());
        itemFactory.addParser(Container.factory());

        diplomacyManager = new DiplomacyManager();

        itemCollection = new ItemCollection(itemFactory);
        entityCollection = new EntityCollection(this);
    }

    public void startDefaultTasks(){
        executor.scheduleCommand(new EntityRegenCommand(registry, notificationService));
    }

    public void loadDefaultCommands(MessagePipeline messagePipeline){
        messagePipeline.loadMessage(ClientCreateCharacterMessage.class);
        messagePipeline.loadMessage(ClientLookMessage.class);
        messagePipeline.loadMessage(ClientMoveMessage.class);
        messagePipeline.loadMessage(ClientSayMessage.class);
        messagePipeline.loadMessage(ClientCreateWorldMessage.class);
        messagePipeline.loadMessage(ClientViewWorldMessage.class);
        messagePipeline.loadMessage(ClientTransferEntityMessage.class);
        messagePipeline.loadMessage(ClientLockContainerMessage.class);
        messagePipeline.loadMessage(ClientUnlockContainerMessage.class);

        messagePipeline.loadMessage(ClientDropMessage.class);
        messagePipeline.loadMessage(ClientGrabMessage.class);
        messagePipeline.loadMessage(ClientPutMessage.class);
        messagePipeline.loadMessage(ClientInventoryMessage.class);
        messagePipeline.loadMessage(ClientAttackMessage.class);
        messagePipeline.loadMessage(ClientPoolsMessage.class);
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public ClientRegistry getRegistry() {
        return registry;
    }

    public ItemFactory getItemFactory() {
        return itemFactory;
    }

    public ItemCollection getItemCollection() {
        return itemCollection;
    }

    public EntityCollection getEntityCollection() {
        return entityCollection;
    }

    public DiplomacyManager getDiplomacyManager(){
        return diplomacyManager;
    }
}
