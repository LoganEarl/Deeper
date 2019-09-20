package main.java.world.item.misc;

import main.java.world.item.Item;
import main.java.world.item.ItemFactory;
import main.java.world.item.ItemType;
import main.java.world.meta.World;

import java.sql.ResultSet;
import java.util.Map;

public class MiscItem extends Item {

    public MiscItem(ResultSet entry, ItemFactory factory, String databaseName) throws Exception {
        super(entry, factory, databaseName);
    }

    @Override
    protected boolean compositeStatsExistInWorld(World targetWorld) {
        initStats();
        return MiscItemStatTable.existsInWorld(getItemName(),targetWorld);
    }

    @Override
    protected boolean writeCompositeStatsToWorld(World targetWorld) {
        initStats();
        return MiscItemStatTable.writeStatsToWorld(getDerivedStats(), targetWorld);
    }

    @Override
    protected Map<String, String> getDerivedStats() {
        return MiscItemStatTable.getStatsForMisc(getItemName(),getDatabaseName());
    }

    @Override
    public ItemType getItemType() {
        return ItemType.misc;
    }

    private static ItemFactory.ItemParser parser = new ItemFactory.ItemParser() {
        @Override
        public ItemType getAssociatedType() {
            return ItemType.misc;
        }

        @Override
        public Item parseFromResultSet(ResultSet fromEntry, ItemFactory sourceFactory, String databaseName) throws Exception {
            return new MiscItem(fromEntry, sourceFactory, databaseName);
        }
    };

    public static ItemFactory.ItemParser factory(){
        return parser;
    }
}
