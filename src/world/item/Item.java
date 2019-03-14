package world.item;

import database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import static world.item.ItemInstanceTable.*;
//TODO finish this. lots of work this
public class Item implements DatabaseManager.DatabaseEntry {
    private static final String SAVE_SQL = String.format(Locale.US,"INSERT INTO %s (%s %s %s %s %s) VALUES (? ? ? ? ?)",
            TABLE_NAME, ITEM_ID, ENTITY_ID, ROOM_NAME, ITEM_NAME, DISPLAY_NAME);
    private static final String DELETE_SQL = String.format(Locale.US,"DELETE FROM %s WHERE %s=?",
            TABLE_NAME, ITEM_ID);
    private static final String UPDATE_SQL = String.format(Locale.US,"UPDATE %s SET %s=?, %s=?, %s=?, %s=? WHERE %s=?",
            TABLE_NAME, ENTITY_ID, ROOM_NAME, ITEM_ID, DISPLAY_NAME, ITEM_ID);
    private static final String GET_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE %s=?",
            TABLE_NAME, ITEM_ID);
    private static final String GET_NAME_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE (%s=? AND %s=?)",
            TABLE_NAME, ITEM_NAME, ROOM_NAME);
    private static final String GET_OF_CONTAINER_ID_SQL = String.format(Locale.US, "SELECT * FROM (%s, %s) WHERE %s=?",
            ContainedItemTable.TABLE_NAME, ItemInstanceTable.TABLE_NAME, ContainedItemTable.CONTAINER_ID);
    private static final String GET_OF_CONTAINER_NAME_SQL = String.format(Locale.US, "SELECT * FROM (%s, %s, %s) WHERE %s=?",
            ContainedItemTable.TABLE_NAME, ItemInstanceTable.TABLE_NAME, ContainerInstanceTable.TABLE_NAME, ContainerInstanceTable.CONTAINER_NAME);

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

    public static Item getItemByNameAndRoom(String itemName, String roomName, String databaseName){
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

    public static Item getItemByItemID(int itemID, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL = null;
        Item toReturn;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_SQL);
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

    @Override
    public boolean saveToDatabase(String databaseName) {
        return false;
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        return false;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        return false;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return false;
    }
}
