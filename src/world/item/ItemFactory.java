package world.item;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class ItemFactory {
    private static ItemFactory soleInstance;

    public static synchronized ItemFactory getInstance(){
        if(soleInstance == null)
            soleInstance = new ItemFactory();
        return soleInstance;
    }

    private Map<ItemType, ItemParser> parsers = new HashMap<>();

    private ItemFactory(){
    }

    public Item parseFromResultSet(ResultSet resultSet, String databaseName){
        if(resultSet != null) {
            ItemType type = ItemType.extractFromResultSet(resultSet);
            if (parsers.containsKey(type)) {
                try {
                    return parsers.get(type).parseFromResultSet(resultSet, databaseName);
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
        Item parseFromResultSet(ResultSet fromEntry, String databaseName) throws Exception;
    }
}
