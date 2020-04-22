package main.java.world.room;

import main.java.database.DatabaseManager;
import main.java.world.entity.Entity;
import main.java.world.trait.Effect;
import main.java.world.trait.EffectArchetype;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static main.java.world.room.RoomConnectionTable.*;

public class RoomConnection implements DatabaseManager.DatabaseEntry {
    private String connectionID;
    private String displayName;

    private String databaseName;

    //skill check info
    private String successMessage;
    private String failureMessage;
    private String sourceRoomName;
    private String destRoomName;
    private int traverseDifficulty;
    private String traverseSkillName;
    private int detectDifficulty;
    private EffectArchetype failureEffect;
    private EffectArchetype successEffect;
    private String failureRoomName;
    private List<Domain> failureDestinationDomains;

    private List<Domain> detectDomains;
    private String detectWord;
    private int staminaCost;
    private int keyCode;
    private List<Domain> sourceDomains;
    private List<Domain> destinationDomains;

    private RoomConnection(ResultSet readEntry, String databaseName) throws SQLException {
        connectionID = readEntry.getString(CONNECTION_ID);
        displayName = readEntry.getString(NAME);
        successMessage = readEntry.getString(SUCCESS_MESSAGE);
        failureMessage = readEntry.getString(FAILURE_MESSAGE);
        sourceRoomName = readEntry.getString(SOURCE_ROOM_NAME);
        destRoomName = readEntry.getString(DEST_ROOM_NAME);
        traverseDifficulty = readEntry.getInt(TRAVERSE_DIFFICULTY);
        traverseSkillName = readEntry.getString(TRAVERSE_SKILL_NAME);
        detectDifficulty = readEntry.getInt(DETECT_DIFFICULTY);


        String raw = "";
        try {
            raw = readEntry.getString(FAILURE_EFFECT_NAME);
            failureEffect = raw != null? EffectArchetype.valueOf(raw): null;
        }catch (IllegalArgumentException e){
            System.out.printf("Failed to load failure effect for room connection:%s:%s\n",connectionID,raw);
            failureEffect = null;
        }

        try {
            raw = readEntry.getString(SUCCESS_EFFECT_NAME);
            successEffect = raw != null? EffectArchetype.valueOf(raw): null;
        }catch (IllegalArgumentException e){
            System.out.printf("Failed to load success effect for room connection:%s:%s\n",connectionID,raw);
            failureEffect = null;
        }

        this.databaseName = databaseName;
    }

    public boolean isVisibleTo(Entity entity){
        if(!entity.getDatabaseName().equals(databaseName))
            return false;
        if(detectDifficulty == 0)
            return true;

        return RoomConnectionDiscoveryTable.connectionIsVisible(connectionID, entity.getID(), databaseName);
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
