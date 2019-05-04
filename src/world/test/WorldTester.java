package world.test;

import client.AccountTable;
import database.DatabaseManager;
import world.entity.EntityTable;
import world.item.*;
import world.meta.World;
import world.room.Room;
import world.room.RoomTable;
import world.story.StoryArcTable;

import java.util.LinkedList;
import java.util.List;

public class WorldTester {
    public static void main(String[] args){
        World w = World.createWorldFromTemplate("testTemplate");
        if(w == null){
            System.out.println("Failed to initialize world for testing");
            return;
        }

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
        DatabaseManager.createNewWorldDatabase(w.getDatabaseName());
        DatabaseManager.createWorldTables(w.getDatabaseName(), tables);

        Room r = Room.getRoomByRoomName("The Origin", w.getDatabaseName());
        Container c = Container.getContainerByContainerID(1,w.getDatabaseName());
        if(r != null && c != null) {

            displayRoomContents(r,w);
            displayContainerContents(c);

            System.out.println("Attempting to move items in room into container");
            List<Item> inRoom = Item.getItemsInRoom(r.getRoomName(),w.getDatabaseName());

            for(Item i: inRoom){
                if(c.tryStoreItem(i)){
                    System.out.printf("Stored the %s in the %s\n", i.getDisplayableName(), c.getContainerName());
                }else{
                    System.out.printf("Could not store the the %s in the %s\n", i.getDisplayableName(), c.getContainerName());
                }
            }

            displayRoomContents(r,w);
            displayContainerContents(c);

            System.out.println("Attempting to unlock the container with the key");
            Item i = Item.getItemByID(3,w.getDatabaseName());
            if(c.setLockedWithItem(i,false))
                System.out.println("Unlocked!");
            else
                System.out.println("Failed to unlock");

            System.out.println("Attempting to move items in room into container");
            inRoom = Item.getItemsInRoom(r.getRoomName(),w.getDatabaseName());

            for(Item j: inRoom){
                if(c.tryStoreItem(i)){
                    System.out.printf("Stored the %s in the %s\n", j.getDisplayableName(), c.getContainerName());
                }else{
                    System.out.printf("Could not store the the %s in the %s\n", j.getDisplayableName(), c.getContainerName());
                }
            }

            displayRoomContents(r,w);
            displayContainerContents(c);

            System.out.println("Attempting to lock the container with the key");
            i = Item.getItemByID(3,w.getDatabaseName());
            if(c.setLockedWithItem(i,true))
                System.out.println("Locked!");
            else
                System.out.println("Failed to lock");
        }
    }

    private static void displayRoomContents(Room r, World w){
        System.out.println("In Room");
        List<Item> inRoom = Item.getItemsInRoom(r.getRoomName(), w.getDatabaseName());
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
