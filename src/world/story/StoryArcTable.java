package world.story;

import database.DatabaseManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Contains the database schema for the table that holds the story arcs.
 * @author Logan Earl
 */
public class StoryArcTable implements DatabaseManager.DatabaseTable {
    /**The name of the table used to store story arc information*/
    public static final String TABLE_NAME = "storyArc";

    /**The name of the story arc. Used as the unique identifier of the story*/
    public static final String ARC_NAME = "arcName";
    /**A number used to show the progression of the story*/
    public static final String CUR_STORY_NUMBER = "curStoryNumber";
    /**The story number used to represent a successful completion of the arc*/
    public static final String STORY_SUCCESS_NUMBER = "storySuccessNumber";
    /**The story number used to represent failure of the arc*/
    public static final String STORY_FAILURE_NUMBER = "storyFailureNumber";
    /**Flag that is used to denote the main story. Only one arc should have a value of true here*/
    public static final String IS_PRIMARY_STORY = "isPrimaryStory";

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    public StoryArcTable(){
        TABLE_DEFINITION.put(ARC_NAME, "VARCHAR(32) PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(CUR_STORY_NUMBER, "INT");
        TABLE_DEFINITION.put(STORY_SUCCESS_NUMBER, "INT");
        TABLE_DEFINITION.put(STORY_FAILURE_NUMBER, "INT");
        TABLE_DEFINITION.put(IS_PRIMARY_STORY,"INT");
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
    public Set<String> getConstraints() {
        return null;
    }
}
