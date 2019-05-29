package world.story;

import database.DatabaseManager;

import java.util.*;

/**
 * Contains the SQL schema for creating a table for storing quest information. Quests are instanced, so this does not have any way to link a quest to a player, only
 * to get general info on the quest.
 * @author Logan Earl
 */
public class QuestTable implements DatabaseManager.DatabaseTable {
    /**The name of the table used to store quests*/
    public static final String TABLE_NAME = "quest";

    /**The unique identifier of the quest*/
    public static final String QUEST_ID = "questID";
    /**The displayed name of the quest*/
    public static final String QUEST_NAME = "name";
    /**The extended description of the quest's task and how to accomplish it*/
    public static final String DESCRIPTION = "description";
    /**The type of quest. Must be one of the TYPE_* constants defined below*/
    public static final String TYPE = "type";
    /**A slot used for special arguments to be passed to the quest. Refer to the type of quest specified for more info*/
    public static final String ARGS = "args";
    /**The time limit in seconds that the quest must be completed by once started*/
    public static final String TIME_LIMIT = "timeLimit";
    /**The story arc that the quest is part of*/
    public static final String ARC_NAME = StoryArcTable.ARC_NAME;
    /**Story arcs are advanced or declined using a number. This represents the number the arc will be set to if the quest is completed successfully*/
    public static final String ARC_COMPLETION_NUM = "arcCompletionNum";
    /**Story arcs are advanced or declined using a number. This represents the number the arc will be set to if the quest is failed*/
    public static final String ARC_FAILURE_NUM = "arcFailureNum";

    //TODO flesh this out more. Figure out how to manage quest objects and plan accordingly
    public static final String TYPE_FETCH = "fetch";
    public static final String TYPE_KILL = "kill";
    public static final String TYPE_EQUIP = "equip";
    public static final String TYPE_SPEAK = "speak";
    public static final String TYPE_LEVEL = "level";
    public static final String TYPE_DEFEND = "defend";

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final List<String> CONSTRAINTS = new ArrayList<>();

    public QuestTable(){
        TABLE_DEFINITION.put(QUEST_ID, "INT PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(QUEST_NAME, "VARCHAR(32)");
        TABLE_DEFINITION.put(DESCRIPTION, "TEXT");
        TABLE_DEFINITION.put(TYPE, "VARCHAR(32)");
        TABLE_DEFINITION.put(ARGS, "TEXT");
        TABLE_DEFINITION.put(TIME_LIMIT, "INT");
        TABLE_DEFINITION.put(ARC_NAME, "VARCHAR(32)");
        TABLE_DEFINITION.put(ARC_COMPLETION_NUM, "INT");
        TABLE_DEFINITION.put(ARC_FAILURE_NUM,"INT");

        CONSTRAINTS.add(String.format(Locale.US,"VARCHAR(32), FOREIGN KEY (%s) REFERENCES %s(%s)",
                ARC_NAME, StoryArcTable.TABLE_NAME, StoryArcTable.ARC_NAME));
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return TABLE_DEFINITION;
    }

    @Override
    public List<String> getConstraints() {
        return CONSTRAINTS;
    }
}
