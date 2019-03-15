package world.story;

import database.DatabaseManager;

import java.util.Map;

//TODO stubbed out
public class StoryArcTable implements DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "storyArc";
    public static final String ARC_NAME = "arcName";

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return null;
    }
}
