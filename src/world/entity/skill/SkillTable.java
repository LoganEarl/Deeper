package world.entity.skill;

import database.DatabaseManager;
import world.entity.Entity;
import world.entity.EntityTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class SkillTable implements DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "skills";

    public static final String ENTITY_ID = EntityTable.ENTITY_ID;
    public static final String SKILL_NAME = "skillName";
    public static final String SKILL_LEVEL = "skillLevel";

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final Set<String> CONSTRAINTS = new HashSet<>(2);

    private static final String GET_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE %s=?",TABLE_NAME,ENTITY_ID);
    private static final String LEARN_SQL = String.format(Locale.US, "REPLACE INTO %s(%s, %s, %s) VALUES (?, ?, ?)",TABLE_NAME,ENTITY_ID,SKILL_NAME, SKILL_LEVEL);
    private static final String FORGET_SQL = String.format(Locale.US,"DELETE FROM %s WHERE (%s=? AND %s=?)",TABLE_NAME,ENTITY_ID,SKILL_NAME);

    public SkillTable(){
        TABLE_DEFINITION.put(ENTITY_ID, "VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(SKILL_NAME, "VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(SKILL_LEVEL, "INT NOT NULL");

        CONSTRAINTS.add(String.format(Locale.US,"PRIMARY KEY (%s, %s)",ENTITY_ID,SKILL_NAME));
    }

    public static boolean learnSkill(Entity sourceEntity, Skill toLearn, int level){
        return DatabaseManager.executeStatement(LEARN_SQL,sourceEntity.getDatabaseName(), sourceEntity.getID(),toLearn.getSavableName(), level) > 0;
    }

    public static boolean forgetSkill(Entity sourceEntity, Skill toForget){
        return DatabaseManager.executeStatement(FORGET_SQL,sourceEntity.getDatabaseName(),sourceEntity.getID(), toForget.getSavableName()) > 0;
    }

    public static Skill[] getEntitySkills(Entity sourceEntity){
        Connection c = DatabaseManager.getDatabaseConnection(sourceEntity.getDatabaseName());
        PreparedStatement getSQL = null;
        List<Skill> skills;
        if(c == null)
            skills = Collections.emptyList();
        else{
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1,sourceEntity.getID());
                ResultSet accountSet = getSQL.executeQuery();

                skills = new ArrayList<>();
                while(accountSet.next()){
                    Skill skill = Skill.getSkill(accountSet.getString(SKILL_NAME), accountSet.getInt(SKILL_LEVEL));
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
