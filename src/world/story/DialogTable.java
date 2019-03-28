package world.story;

import database.DatabaseManager;
import world.entity.EntityTable;

import java.util.*;

/**
 * Contains the schema for a database table used to contain dialog options
 * @author Logan Earl
 */
public class DialogTable implements DatabaseManager.DatabaseTable {
    /**The name of the table used to store dialog entries*/
    public static final String TABLE_NAME = "dialog";

    /**The unique identifier of the dialog option*/
    public static final String DIALOG_ID = "dialogID";
    /**The text to be said in the dialog*/
    public static final String TEXT_TO_SAY = "textToSay";
    /**The gesture the entity should make while speaking. Must be parsable as a gesture command*/
    public static final String GESTURE_COMMAND = "gestureCommand";
    /**The name of the arc that this dialog option is part of*/
    public static final String ARC_NAME = StoryArcTable.ARC_NAME;
    /**The minimum required story number for the associated arc to have to enable this dialog option*/
    public static final String MIN_STORY_NUM = "minStoryName";
    /**The maximum required story number for the associated arc to have to enable this dialog option*/
    public static final String MAX_STORY_NUM = "maxStoryNum";
    /**The next dialog to be made as soon as this one completes. Will wait the DIALOG_DELAY before making it*/
    public static final String NEXT_DIALOG = "nextDialogID";
    /**The wait in milliseconds before the next dialog is started*/
    public static final String DIALOG_DELAY = "dialogDelay";
    /**The quest that is offered after this dialog completes*/
    public static final String OFFERS_QUEST_ID = "offersQuestID";
    /**The number to be assigned to the associated arc after this dialog is complete. nullable*/
    public static final String ARC_NUM_ON_COMPLETION = "arcNumOnCompletion";

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final List<String> CONSTRAINTS = new ArrayList<>();

    public DialogTable(){
        TABLE_DEFINITION.put(DIALOG_ID, "INT PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(TEXT_TO_SAY, "TEXT");
        TABLE_DEFINITION.put(GESTURE_COMMAND, "VARCHAR(32)");
        TABLE_DEFINITION.put(ARC_NAME, "VARCHAR(32)");
        TABLE_DEFINITION.put(MIN_STORY_NUM, "INT");
        TABLE_DEFINITION.put(MAX_STORY_NUM, "INT");
        TABLE_DEFINITION.put(NEXT_DIALOG,"INT");
        TABLE_DEFINITION.put(DIALOG_DELAY, "INT");
        TABLE_DEFINITION.put(OFFERS_QUEST_ID , "INT");
        TABLE_DEFINITION.put(ARC_NUM_ON_COMPLETION, "INT");

        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ARC_NAME, StoryArcTable.TABLE_NAME, StoryArcTable.ARC_NAME));
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                NEXT_DIALOG, DialogTable.TABLE_NAME, DialogTable.DIALOG_ID));
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                OFFERS_QUEST_ID, QuestTable.TABLE_NAME, QuestTable.QUEST_ID));
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
    public List getConstraints() {
        return CONSTRAINTS;
    }
}
