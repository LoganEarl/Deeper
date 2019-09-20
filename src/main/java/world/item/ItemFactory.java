package main.java.world.item;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class ItemFactory {
    private Map<ItemType, ItemParser> parsers = new HashMap<>();

    public Item parseFromResultSet(ResultSet resultSet, String databaseName){
        if(resultSet != null) {
            ItemType type = ItemType.extractFromResultSet(resultSet);
            if (parsers.containsKey(type)) {
                try {
                    Item i = parsers.get(type).parseFromResultSet(resultSet, this, databaseName);
                    i.initStats();
                    return i;
                } catch (Exception e) {
                    System.out.println("Unable to parse item of type " + type.toString());
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void addParser(ItemParser parser){
        if(parser != null)
            parsers.put(parser.getAssociatedType(),parser);
    }

    public interface ItemParser{
        ItemType getAssociatedType();
        Item parseFromResultSet(ResultSet fromEntry, ItemFactory sourceFactory, String databaseName) throws Exception;
    }
}
