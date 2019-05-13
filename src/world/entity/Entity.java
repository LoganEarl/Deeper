package world.entity;

import database.DatabaseManager;
import world.meta.World;
import world.room.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import static world.entity.EntityTable.*;

public class Entity implements DatabaseManager.DatabaseEntry {

    private String entityID;
    private String displayName;
    private int hp;
    private int maxHP;
    private int mp;
    private int maxMP;
    private int stamina;
    private int maxStamina;

    private int strength;
    private int dexterity;
    private int intelligence;
    private int wisdom;

    private String controllerType;
    private String roomName;

    private String databaseName;

    private static final String GET_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE %s=?",TABLE_NAME,ENTITY_ID);
    private static final String CREATE_SQL = String.format(Locale.US,"INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            TABLE_NAME,ENTITY_ID,DISPLAY_NAME, HP,MAX_HP,MP,MAX_MP,STAMINA,MAX_STAMINA,STR,DEX,INT,WIS,CONTROLLER_TYPE,ROOM_NAME);
    private static final String DELETE_SQL = String.format(Locale.US,"DELETE FROM %s WHERE %s=?", TABLE_NAME,ENTITY_ID);
    private static final String UPDATE_SQL = String.format(Locale.US,"UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=? WHERE %s=?",
            TABLE_NAME, DISPLAY_NAME, HP,MAX_HP,MP,MAX_MP,STAMINA,MAX_STAMINA,STR,DEX,INT,WIS,CONTROLLER_TYPE,ROOM_NAME,ENTITY_ID);

    private Entity(ResultSet readEntry, String databaseName) throws SQLException {
        entityID = readEntry.getString(ENTITY_ID);
        displayName = readEntry.getString(DISPLAY_NAME);

        hp = readEntry.getInt(HP);
        maxHP = readEntry.getInt(MAX_HP);
        mp = readEntry.getInt(MP);
        maxMP = readEntry.getInt(MAX_MP);
        stamina = readEntry.getInt(STAMINA);
        maxStamina = readEntry.getInt(MAX_STAMINA);
        strength = readEntry.getInt(STR);
        dexterity = readEntry.getInt(DEX);
        intelligence = readEntry.getInt(INT);
        wisdom = readEntry.getInt(WIS);

        controllerType = readEntry.getString(CONTROLLER_TYPE);
        roomName = readEntry.getString(ROOM_NAME);

        this.databaseName = databaseName;
    }

    public static Entity getEntityByEntityID(String entityID, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL = null;
        Entity toReturn;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1,entityID);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next())
                    toReturn = new Entity(accountSet,databaseName);
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
        Entity entity = getEntityByEntityID(entityID,databaseName);
        if(entity == null){
            return DatabaseManager.executeStatement(CREATE_SQL,databaseName,
                    entityID,displayName, hp,maxHP,mp,maxMP,stamina,maxStamina,strength,dexterity, intelligence,wisdom,controllerType,roomName) > 0;
        }else{
            return updateInDatabase(databaseName);
        }
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        return DatabaseManager.executeStatement(DELETE_SQL,databaseName, entityID) > 0;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        return DatabaseManager.executeStatement(UPDATE_SQL,databaseName,
                displayName, hp,maxHP,mp,maxMP,stamina,maxStamina,strength,dexterity, intelligence,wisdom,controllerType,roomName) > 0;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return getEntityByEntityID(entityID,databaseName) != null;
    }

    public static final int CODE_TRANSFER_COMPLETE = 0;
    public static final int CODE_ALREADY_EXISTS_AT_DESTINATION = -1;
    public static final int CODE_TRANSFER_FAILED = -2;


    public int transferToWorld(World newWorld){
        if(newWorld == null)
            throw new IllegalArgumentException("cannot transfer to a null world");

        if(existsInDatabase(databaseName) && !removeFromDatabase(databaseName)) {
            updateInDatabase(databaseName);
            return CODE_TRANSFER_FAILED;
        }

        if(existsInDatabase(newWorld.getDatabaseName()))
            return CODE_ALREADY_EXISTS_AT_DESTINATION;
        if(!saveToDatabase(newWorld.getDatabaseName()))
            return CODE_TRANSFER_FAILED;
        this.databaseName = newWorld.getDatabaseName();

    }

    public String getID(){
        return entityID;
    }
}
