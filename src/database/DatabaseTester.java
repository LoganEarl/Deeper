package database;

import client.AccountTable;
import org.sqlite.core.DB;
import world.entity.EntityTable;
import world.item.*;
import world.room.Room;
import world.room.RoomTable;
import world.story.StoryArcTable;

import java.util.LinkedList;
import java.util.List;

public class DatabaseTester {
    private static final String DB_NAME = "testSim.db";

    public static void main(String[] args){
        List<DatabaseManager.DatabaseTable> tables = new LinkedList<>();
        tables.add(new AccountTable());
        tables.add(new ItemStatTable());
        tables.add(new ContainerStatTable());
        tables.add(new RoomTable());
        tables.add(new EntityTable());
        tables.add(new StoryArcTable());

        tables.add(new ItemInstanceTable());
        tables.add(new ContainerInstanceTable());

        DatabaseManager.createDirectories();
        DatabaseManager.createNewDatabase(DB_NAME);
        DatabaseManager.createTables(DB_NAME, tables);

        Room r = Room.getRoomByRoomName("The Origin", DB_NAME);
        Container c = Container.getContainerByContainerID(1,DB_NAME);
        if(r != null && c != null) {

            displayRoomContents(r);
            displayContainerContents(c);

            System.out.println("Attempting to move items in room into container");
            List<Item> inRoom = Item.getItemsInRoom(r.getRoomName(),DB_NAME);

            for(Item i: inRoom){
                if(c.tryStoreItem(i)){
                    System.out.printf("Stored the %s in the %s\n", i.getDisplayableName(), c.getContainerName());
                }else{
                    System.out.printf("Could not store the the %s in the %s\n", i.getDisplayableName(), c.getContainerName());
                }
            }

            displayRoomContents(r);
            displayContainerContents(c);

            System.out.println("Attempting to unlock the container with the key");
            Item i = Item.getItemByID(3,DB_NAME);
            if(c.setLockedWithItem(i,false))
                System.out.println("Unlocked!");
            else
                System.out.println("Failed to unlock");

            System.out.println("Attempting to move items in room into container");
            inRoom = Item.getItemsInRoom(r.getRoomName(),DB_NAME);

            for(Item j: inRoom){
                if(c.tryStoreItem(i)){
                    System.out.printf("Stored the %s in the %s\n", j.getDisplayableName(), c.getContainerName());
                }else{
                    System.out.printf("Could not store the the %s in the %s\n", j.getDisplayableName(), c.getContainerName());
                }
            }

            displayRoomContents(r);
            displayContainerContents(c);

            System.out.println("Attempting to lock the container with the key");
            i = Item.getItemByID(3,DB_NAME);
            if(c.setLockedWithItem(i,true))
                System.out.println("Locked!");
            else
                System.out.println("Failed to lock");
        }
    }

    private static void displayRoomContents(Room r){
        System.out.println("In Room");
        List<Item> inRoom = Item.getItemsInRoom(r.getRoomName(), DB_NAME);
        for(Item i : inRoom){
            System.out.println(i.getDisplayableName());
        }
    }

    private static void displayContainerContents(Container c){
        if(c == null){
            System.out.println("Failed to find container with ID:1");
            return;
        }

        System.out.println("In Container");
        List<Item> inContainer = c.getStoredItems();
        for(Item i : inContainer){
            System.out.println(i.getDisplayableName());
        }
    }
}
