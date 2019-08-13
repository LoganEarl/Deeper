package world.item.container;

import world.item.Item;
import world.item.ItemFactory;
import world.item.ItemInstanceTable;
import world.item.ItemType;
import world.meta.World;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * Holds the data for a container. Containers can hold any item as long as the item does not
 * exceed the storage capabilities of the container. Containers are constrained by a maximum volume,
 * a maximum weight, and a maximum number of items. A given container can have any/all of these
 * constraints. Or none if you want to make a bag of holding or something
 *
 * @author Logan Earl
 */
public class Container extends Item {

    public enum ContainerState implements Item.ItemState {
        locked, unlocked
    }

    public Container(ResultSet entry, ItemFactory factory, String databaseName) throws Exception {
        super(entry, factory, databaseName);

        try {
            setState(ContainerState.valueOf(entry.getString(ItemInstanceTable.STATE)));
        } catch (Exception e) {
            setState(ContainerState.unlocked);
        }
    }

    @Override
    public ItemType getItemType() {
        return ItemType.container;
    }

    @Override
    protected Map<String, String> getDerivedStats() {
        return ContainerStatTable.getStatsForContainer(getItemName(), getDatabaseName());
    }

    @Override
    protected boolean compositeStatsExistInWorld(World targetWorld) {
        return ContainerStatTable.existsInWorld(getItemName(),targetWorld);
    }

    @Override
    protected boolean writeCompositeStatsToWorld(World targetWorld) {
        return ContainerStatTable.writeStatsToWorld(getDerivedStats(),targetWorld);
    }

    /**
     * shortcut to Item.getItemsOfContainerID().
     *
     * @return all items stored in this container
     * @see Item
     */
    public List<Item> getStoredItems() {
        return Item.getItemsOfContainerID(getItemID(),getItemFactory(), getDatabaseName());
    }

    public boolean containsItem(Item item) {
        for (Item i : getStoredItems())
            if (i.equals(item))
                return true;
        return false;
    }

    public Item getContainedItem(String identifier) {
        if (identifier == null)
            return null;
        identifier = identifier.toLowerCase();
        for (Item i : getStoredItems()) {
            if (identifier.equals(i.getDisplayableName().toLowerCase()) ||
                    identifier.equals(String.valueOf(i.getItemID()).toLowerCase()) ||
                    identifier.equals(i.getItemName().toLowerCase()))
                return i;
        }
        return null;
    }

    public boolean getIsLocked() {
        return getState() == ContainerState.locked;
    }

    /**
     * gets if this container can store the given item without exceeding its storage constraints
     *
     * @param i the item to check against the storage constraints
     * @return true if the item fits in the container
     */
    public boolean canHoldItem(Item i) {
        double totalKgs = 0, totalLiters = 0;
        List<Item> heldItems = getStoredItems();
        for (Item stored : heldItems) {
            totalKgs += stored.getWeight();
            totalLiters += stored.getVolume();
        }
        return (getMaxItems() != ContainerStatTable.CODE_NOT_USED && getMaxItems() >= heldItems.size() + 1) ||
                (getMaxKilograms() != ContainerStatTable.CODE_NOT_USED && getMaxKilograms() >= totalKgs + i.getWeight()) ||
                (getMaxLiters() != ContainerStatTable.CODE_NOT_USED && getMaxLiters() >= totalLiters + i.getVolume());
    }

    /**
     * attempts to set the lock state of this container with the given item. The lockNumbers of the items are compared. If they match and the container is lockable, the container lock state will be set successfully. Otherwise not. If the state changed, the database is updated with the new lock state.
     *
     * @param i              the item used to
     * @param wantToBeLocked true if the container should be locked
     * @return true if the container could be updated. false if not a lockable container, the item was not a key, or the lock numbers did not match
     */
    public boolean setLockedWithItem(Item i, boolean wantToBeLocked) {
        if (!getIsLockable() || i.getLockNumber() == 0)
            return false;
        if (getLockNumber() == i.getLockNumber()) {
            if (wantToBeLocked)
                setState(ContainerState.locked);
            else
                setState(ContainerState.unlocked);
            updateInDatabase(getDatabaseName());
            return true;
        }
        return false;
    }

    /**
     * will attempt to store the given item in this container. Will fail to do id this container is locked or cannot hold the item due to container constraints.
     *
     * @param toStore the item to store in the container
     * @return true if the item was stored successfully
     */
    public boolean tryStoreItem(Item toStore) {
        if (getState() == ContainerState.locked || !canHoldItem(toStore))
            return false;
        toStore.setContainerID(getItemID());
        return true;
    }

    @Override
    public double getWeight() {
        double totalKgs = 0;
        List<Item> heldItems = getStoredItems();
        for (Item stored : heldItems) {
            totalKgs += stored.getWeight();
        }
        return totalKgs + getIntrinsicWeight();
    }

    public double getMaxKilograms() {
        initStats();
        return getCastDouble(ContainerStatTable.MAX_KGS);
    }

    public double getMaxLiters() {
        initStats();
        return getCastDouble(ContainerStatTable.MAX_LITERS);
    }

    public int getMaxItems() {
        initStats();
        return getCastInt(ContainerStatTable.MAX_NUMBER);
    }

    public int getLockDifficulty() {
        initStats();
        return getCastInt(ContainerStatTable.LOCK_DIFFICULTY);
    }

    public boolean getIsLockable() {
        initStats();
        return getLockDifficulty() != ContainerStatTable.CODE_NOT_USED;
    }

    private static ItemFactory.ItemParser parser = new ItemFactory.ItemParser() {
        @Override
        public ItemType getAssociatedType() {
            return ItemType.container;
        }

        @Override
        public Item parseFromResultSet(ResultSet fromEntry, ItemFactory sourceFactory, String databaseName) throws Exception {
            return new Container(fromEntry, sourceFactory, databaseName);
        }
    };

    public static ItemFactory.ItemParser factory() {
        return parser;
    }
}
