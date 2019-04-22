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
        if(r != null) {
            System.out.println("In Room");
            List<Item> inRoom = Item.getItemsInRoom(r.getRoomName(), DB_NAME);
            for(Item i : inRoom){
                System.out.println(i.getDisplayableName());
            }

            System.out.println("In Container");
            Container container = Container.getContainerByContainerID(1,DB_NAME);
            if(container == null){
                System.out.println("Failed to find container with ID:1");
                return;
            }

            List<Item> inContainer = container.getStoredItems();
            for(Item i : inContainer){
                System.out.println(i.getDisplayableName());
            }

            System.out.println("Attempting to move items in room into container");

            for(Item i: inRoom){
                if(container.tryStoreItem(i)){
                    System.out.printf("Stored the %s in the %s\n", i.getDisplayableName(), container.getContainerName());
                }else{
                    System.out.printf("Could not store the the %s in the %s\n", i.getDisplayableName(), container.getContainerName());
                }
            }

            System.out.println("In Room");
            inRoom = Item.getItemsInRoom(r.getRoomName(), DB_NAME);
            for(Item i : inRoom){
                System.out.println(i.getDisplayableName());
            }

            System.out.println("In Container");
            inContainer = container.getStoredItems();
            for(Item i : inContainer){
                System.out.println(i.getDisplayableName());
            }

        }
    }
}
