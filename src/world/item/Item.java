package world.item;

import database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import static world.item.ItemInstanceTable.*;
//TODO finish this. lots of work this. It needs to be able to read its stats
public class Item implements DatabaseManager.DatabaseEntry {
    private static final String CREATE_SQL = String.format(Locale.US,"INSERT INTO %s (%s %s %s %s %s) VALUES (? ? ? ? ?)",
            TABLE_NAME, ITEM_ID, ENTITY_ID, ROOM_NAME, ITEM_NAME, DISPLAY_NAME);
    private static final String DELETE_SQL = String.format(Locale.US,"DELETE FROM %s WHERE %s=?",
            TABLE_NAME, ITEM_ID);
    private static final String UPDATE_SQL = String.format(Locale.US,"UPDATE %s SET %s=?, %s=?, %s=?, %s=? WHERE %s=?",
            TABLE_NAME, ENTITY_ID, ROOM_NAME, ITEM_ID, DISPLAY_NAME, ITEM_ID);
    private static final String GET_ID_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE %s=?",
            TABLE_NAME, ITEM_ID);
    private static final String GET_NAME_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE (%s=? AND %s=?)",
            TABLE_NAME, ITEM_NAME, ROOM_NAME);
    private static final String GET_OF_CONTAINER_ID_SQL = String.format(Locale.US, "SELECT * FROM (%s, %s) WHERE %s=?",
            ContainedItemTable.TABLE_NAME, ItemInstanceTable.TABLE_NAME, ContainedItemTable.CONTAINER_ID);

    private int itemID;
    private String entityID;
    private String roomName;
    private String itemName;
    private String displayName;

    private Item(ResultSet entry) throws SQLException{
        itemID = entry.getInt(ITEM_ID);
        entityID = entry.getString(ENTITY_ID);
        roomName = entry.getString(ROOM_NAME);
        itemName = entry.getString(ITEM_NAME);
        displayName = entry.getString(DISPLAY_NAME);
    }

    public static Item getItemByNameRoom(String itemName, String roomName, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL = null;
        Item toReturn;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_NAME_SQL);
                getSQL.setString(1,itemName);
                getSQL.setString(2,roomName);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next())
                    toReturn = new Item(accountSet);
                else
                    toReturn = null;
                getSQL.close();
                c.close();
            }catch (SQLException e){
                toReturn = null;
            }
        }
        return toReturn;
    }


    public static Item getItemByID(int itemID, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        Item toReturn;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_ID_SQL);
                getSQL.setInt(1,itemID);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next())
                    toReturn = new Item(accountSet);
                else
                    toReturn = null;
                getSQL.close();
                c.close();
            }catch (SQLException e){
                toReturn = null;
            }
        }
        return toReturn;
    }

    public static List<Item> getItemsOfContainerID(int containerID, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        List<Item> foundItems = new LinkedList<>();
        if(c == null)
            return Collections.emptyList();
        else{
            try {
                getSQL = c.prepareStatement(GET_OF_CONTAINER_ID_SQL);
                getSQL.setInt(1,containerID);
                ResultSet accountSet = getSQL.executeQuery();
                while(accountSet.next()) {
                    foundItems.add(new Item(accountSet));
                }
                getSQL.close();
                c.close();
            }catch (SQLException e){
                foundItems = Collections.emptyList();
            }
        }
        return foundItems;
    }

    @Override
    public boolean saveToDatabase(String databaseName) {
        Item item = getItemByID(itemID,databaseName);
        if(item == null)
            return DatabaseManager.executeStatement(CREATE_SQL, databaseName,
                    itemID, entityID, roomName, itemName, displayName) > 0;
        else
            return updateInDatabase(databaseName);
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        return DatabaseManager.executeStatement(DELETE_SQL,databaseName,itemID) > 0;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        return DatabaseManager.executeStatement(UPDATE_SQL,databaseName,
                entityID, roomName, itemID, displayName) > 0;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return getItemByID(itemID,databaseName) != null;
    }
}
