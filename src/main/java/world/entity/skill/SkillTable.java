package main.java.world.entity.skill;

import main.java.database.DatabaseManager;
import main.java.world.entity.Entity;
import main.java.world.entity.EntityTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class SkillTable implements DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "skills";

    public static final String SKILL_CONTAINER_ID = "id";
    public static final String SKILL_NAME = "skillName";
    public static final String SKILL_LEVEL = "skillLevel";

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final Set<String> CONSTRAINTS = new HashSet<>(2);

    private static final String GET_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE %s=?",TABLE_NAME,SKILL_CONTAINER_ID);
    private static final String LEARN_SQL = String.format(Locale.US, "REPLACE INTO %s(%s, %s, %s) VALUES (?, ?, ?)",TABLE_NAME,SKILL_CONTAINER_ID,SKILL_NAME, SKILL_LEVEL);
    private static final String FORGET_SQL = String.format(Locale.US,"DELETE FROM %s WHERE (%s=? AND %s=?)",TABLE_NAME,SKILL_CONTAINER_ID,SKILL_NAME);

    public SkillTable(){
        TABLE_DEFINITION.put(SKILL_CONTAINER_ID, "VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(SKILL_NAME, "VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(SKILL_LEVEL, "INT NOT NULL");

        CONSTRAINTS.add(String.format(Locale.US,"PRIMARY KEY (%s, %s)",SKILL_CONTAINER_ID,SKILL_NAME));
    }

    public static boolean learnSkill(String containerID, Skill toLearn, int level, String databaseName){
        return DatabaseManager.executeStatement(LEARN_SQL,databaseName, containerID,toLearn.getSavableName(), level) > 0;
    }

    public static boolean forgetSkill(String containerID, Skill toForget, String databaseName){
        return DatabaseManager.executeStatement(FORGET_SQL,databaseName,containerID, toForget.getSavableName()) > 0;
    }

    public static Skill[] getSkillsByContainerID(String skillContainerID, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL = null;
        List<Skill> skills;
        if(c == null)
            skills = Collections.emptyList();
        else{
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1,skillContainerID);
                ResultSet skillSet = getSQL.executeQuery();

                skills = new ArrayList<>();
                while(skillSet.next()){
                    Skill skill = Skill.getSkill(skillSet.getString(SKILL_NAME), skillSet.getInt(SKILL_LEVEL));
                    if(skill != null)
                        skills.add(skill);
                }

                getSQL.close();
            }catch (Exception e){
                skills = Collections.emptyList();
            }
        }
        return skills.toArray(new Skill[0]);
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
        return CONSTRAINTS;
    }
}
