package world.story;

import database.DatabaseManager;
import world.entity.EntityTable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Contains the table schema for a table relating players and quests. Used to store what quests are active for a player and what the status of each quest is for that player
 * @author Logan Earl
 */
public class EntityQuestStatusTable implements DatabaseManager.DatabaseTable {
    /**The name of the sql table used to store quest info*/
    public static final String TABLE_NAME = "entityQuestStatus";

    /**The identifier of the entity doing the quest*/
    public static final String ENTITY_ID = EntityTable.ENTITY_ID;
    /**The identifier of the quest being done by the entity*/
    public static final String QUEST_ID = QuestTable.QUEST_ID;
    /**The current status of the quest. Must be one of the STATUS_* constants defined below.*/
    public static final String CURRENT_STATUS = "status";

    /**Quest status denoting that the quest has been given to the player*/
    public static final String STATUS_GIVEN = "given";
    /**Quest status denoting that the quest has been completed but the player has not returned for the reward.*/
    public static final String STATUS_COMPLETED = "completed";
    /**Quest status denoting that the quest has been completed and the player has gotten the reward. */
    public static final String REWARDED = "rewarded";

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    public final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    public EntityQuestStatusTable(){
        TABLE_DEFINITION.put(ENTITY_ID, String.format(Locale.US,"VARCHAR(32), FOREIGN KEY (%s) REFERENCES %s(%s)",
                ENTITY_ID, EntityTable.TABLE_NAME, EntityTable.ENTITY_ID));
        TABLE_DEFINITION.put(QUEST_ID, String.format(Locale.US,"INT, FOREIGN KEY (%s) REFERENCES %s(%s)",
                QUEST_ID, QuestTable.TABLE_NAME, QuestTable.QUEST_ID));
        TABLE_DEFINITION.put(CURRENT_STATUS,String.format(Locale.US,"VARCHAR(16), PRIMARY KEY (%s,%s)",ENTITY_ID,QUEST_ID));
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return TABLE_DEFINITION;
    }
}
