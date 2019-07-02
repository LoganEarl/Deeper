package world.item.misc;

import world.item.Item;
import world.item.ItemFactory;
import world.item.ItemType;
import world.item.weapon.Weapon;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.Map;

public class MiscItem extends Item {
    protected MiscItem(ResultSet entry, String databaseName) throws Exception {
        super(entry, databaseName);
    }

    @Override
    protected Map<String, String> getDerivedStats() {
        return Collections.emptyMap();
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
        public Item parseFromResultSet(ResultSet fromEntry, String databaseName) throws Exception {
            return new MiscItem(fromEntry,databaseName);
        }
    };

    public static ItemFactory.ItemParser factory(){
        return parser;
    }
}
