package world.item.container;

import world.item.Item;
import world.item.ItemInstanceTable;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * Holds the data for a container. Containers can hold any item as long as the item does not
 * exceed the storage capabilities of the container. Containers are constrained by a maximum volume,
 * a maximum weight, and a maximum number of items. A given container can have any/all of these
 * constraints. Or none if you want to make a bag of holding or something
 * @author Logan Earl
 */
public class Container extends Item {
    private ContainerState state;

    public enum ContainerState{
        locked, unlocked
    }

    public Container(ResultSet entry, String databaseName) throws Exception {
        super(entry,databaseName);

        try{
            state = ContainerState.valueOf(entry.getString(ItemInstanceTable.STATE));
        }catch (Exception e){
            state = ContainerState.unlocked;
        }
    }

    @Override
    protected Map<String, String> getDerivedStats() {
        return ContainerStatTable.getStatsForContainer(getItemName(),getDatabaseName());
    }

    /**
     * shortcut to Item.getItemsOfContainerID().
     * @return all items stored in this container
     * @see Item
     */
    public List<Item> getStoredItems(){
        return Item.getItemsOfContainerID(getItemID(),getDatabaseName());
    }

    public boolean getIsLocked(){
        return this.state == ContainerState.locked;
    }

    /**
     * gets if this container can store the given item without exceeding its storage constraints
     * @param i the item to check against the storage constraints
     * @return true if the item fits in the container
     */
    public boolean canHoldItem(Item i){
        double totalKgs = 0, totalLiters = 0;
        List<Item> heldItems = getStoredItems();
        for(Item stored : heldItems){
            totalKgs += stored.getWeight();
            totalLiters += stored.getVolume();
        }
        return (getMaxItems() != ContainerStatTable.CODE_NOT_USED && getMaxItems() < heldItems.size()+1) ||
                (getMaxKilograms() != ContainerStatTable.CODE_NOT_USED && getMaxKilograms() < totalKgs + i.getWeight()) ||
                (getMaxLiters() != ContainerStatTable.CODE_NOT_USED && getMaxLiters() < totalLiters + i.getVolume());
    }

    /**
     * attempts to set the lock state of this container with the given item. The lockNumbers of the items are compared. If they match and the container is lockable, the container lock state will be set successfully. Otherwise not. If the state changed, the database is updated with the new lock state.
     * @param i the item used to
     * @param wantToBeLocked true if the container should be locked
     * @return true if the container could be updated. false if not a lockable container, the item was not a key, or the lock numbers did not match
     */
    public boolean setLockedWithItem(Item i, boolean wantToBeLocked){
        if(!getIsLockable() || i.getLockNumber() == 0)
            return false;
        if(getLockNumber() == i.getLockNumber()){
            if(wantToBeLocked)
                state = ContainerState.locked;
            else
                state = ContainerState.unlocked;
            updateInDatabase(getDatabaseName());
            return true;
        }
        return false;
    }

    /**
     * will attempt to store the given item in this container. Will fail to do id this container is locked or cannot hold the item due to container constraints.
     * @param toStore the item to store in the container
     * @return true if the item was stored successfully
     */
    public boolean tryStoreItem(Item toStore){
        if(state == ContainerState.locked || !canHoldItem(toStore))
            return false;
        toStore.setContainerID(getItemID());
        return true;
    }

    public double getMaxKilograms(){
        initStats();
        return getCastDouble(ContainerStatTable.MAX_KGS);
    }

    public double getMaxLiters(){
        initStats();
        return getCastDouble(ContainerStatTable.MAX_LITERS);
    }

    public int getMaxItems(){
        initStats();
        return getCastInt(ContainerStatTable.MAX_NUMBER);
    }

    public int getLockDifficulty(){
        initStats();
        return getCastInt(ContainerStatTable.LOCK_DIFFICULTY);
    }

    public boolean getIsLockable(){
        initStats();
        return getLockDifficulty() != ContainerStatTable.CODE_NOT_USED;
    }
}
