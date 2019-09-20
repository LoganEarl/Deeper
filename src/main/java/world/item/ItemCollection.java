package main.java.world.item;

import main.java.world.entity.Entity;

import java.util.List;

public class ItemCollection {
    private ItemFactory factory;

    public ItemCollection(ItemFactory factory){
        this.factory = factory;
    }

    public List<Item> getItemsOfContainerID(int containerID, String databaseName){
        return Item.getItemsOfContainerID(containerID,factory,databaseName);
    }

    public Item getItemByID(int itemID, String databaseName){
        return Item.getItemByID(itemID, factory, databaseName);
    }

    public List<Item> getItemsInRoom(String roomName, String databaseName) {
        return Item.getItemsInRoom(roomName, factory,databaseName);
    }

    public Item getItemByNameRoom(String itemName, String roomName, String databaseName){
        return Item.getItemByNameRoom(itemName,roomName,factory,databaseName);
    }

    public Item getItemFromEntityContext(String identifier, Entity sourceEntity) {
        return Item.getFromEntityContext(identifier,sourceEntity,factory);
    }
}
