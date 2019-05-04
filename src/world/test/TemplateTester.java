package world.test;

import client.AccountTable;
import database.DatabaseManager;
import world.entity.EntityTable;
import world.item.ContainerInstanceTable;
import world.item.ContainerStatTable;
import world.item.ItemInstanceTable;
import world.item.ItemStatTable;
import world.meta.World;
import world.meta.WorldMetaTable;
import world.meta.WorldTable;
import world.room.RoomTable;
import world.story.StoryArcTable;

import java.util.LinkedList;
import java.util.List;

public class TemplateTester {
    private static final String TEMPLATE_NAME = "testTemplate";

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
        tables.add(new WorldMetaTable());

        DatabaseManager.createDirectories();
        DatabaseManager.createNewTemplate(TEMPLATE_NAME + ".db");
        DatabaseManager.createTemplateTables(TEMPLATE_NAME + ".db", tables);

        tables.clear();
        tables.add(new WorldTable());

        DatabaseManager.createNewWorldDatabase(World.META_DATABASE_NAME);
        DatabaseManager.createWorldTables(World.META_DATABASE_NAME,tables);

        World test = World.createWorldFromTemplate(TEMPLATE_NAME);
    }
}
