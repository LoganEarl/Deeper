package main.java.world;

import main.java.client.ClientRegistry;
import main.java.network.CommandExecutor;
import main.java.network.messaging.MessagePipeline;
import main.java.world.diplomacy.DiplomacyManager;
import main.java.world.entity.EntityCollection;
import main.java.world.entity.pool.EntityPoolRecalcCommand;
import main.java.world.entity.pool.EntityRegenCommand;
import main.java.world.item.ItemCollection;
import main.java.world.item.ItemFactory;
import main.java.world.item.armor.Armor;
import main.java.world.item.container.Container;
import main.java.world.item.misc.MiscItem;
import main.java.world.item.weapon.Weapon;
import main.java.world.notification.NotificationService;
import main.java.world.playerInterface.messages.*;

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
        itemFactory.addParser(Armor.factory());

        diplomacyManager = new DiplomacyManager();

        itemCollection = new ItemCollection(itemFactory);
        entityCollection = new EntityCollection(this);
    }

    public void startDefaultTasks(){
        executor.scheduleCommand(new EntityRegenCommand(registry, notificationService));
        executor.scheduleCommand(new EntityPoolRecalcCommand());
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
        messagePipeline.loadMessage(ClientEquipMessage.class);
        messagePipeline.loadMessage(ClientUnequipMessage.class);
        messagePipeline.loadMessage(ClientDropMessage.class);
        messagePipeline.loadMessage(ClientGrabMessage.class);
        messagePipeline.loadMessage(ClientPutMessage.class);
        messagePipeline.loadMessage(ClientInventoryMessage.class);
        messagePipeline.loadMessage(ClientAttackMessage.class);
        messagePipeline.loadMessage(ClientPoolsMessage.class);
        messagePipeline.loadMessage(ClientSkillsMessage.class);
        messagePipeline.loadMessage(ClientStabilizeMessage.class);
        messagePipeline.loadMessage(ClientLearnMessage.class);
        messagePipeline.loadMessage(ClientEvadeMessage.class);
        messagePipeline.loadMessage(ClientDeflectMessage.class);
        messagePipeline.loadMessage(ClientStatsMessage.class);
        messagePipeline.loadMessage(ClientSearchMessage.class);
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
