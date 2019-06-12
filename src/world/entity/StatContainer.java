package world.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

import static world.entity.EntityTable.*;

public class StatContainer implements Entity.SqlExtender {
    private int strength;
    private int dexterity;
    private int intelligence;
    private int wisdom;

    public static final String SIGNIFIER = "stats";
    private static final String[] HEADERS = new String[]{STR,DEX,INT,WIS};

    public StatContainer(int strength, int dexterity, int intelligence, int wisdom) {
        this.strength = strength;
        this.dexterity = dexterity;
        this.intelligence = intelligence;
        this.wisdom = wisdom;
    }

    public StatContainer(ResultSet readEntry) throws SQLException {
        strength = readEntry.getInt(STR);
        dexterity = readEntry.getInt(DEX);
        intelligence = readEntry.getInt(INT);
        wisdom = readEntry.getInt(WIS);
    }

    @Override
    public String getSignifier() {
        return SIGNIFIER;
    }

    @Override
    public Object[] getInsertSqlValues() {
        return new Object[]{strength,dexterity,intelligence,wisdom};
    }

    @Override
    public String[] getSqlColumnHeaders() {
        return HEADERS;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getWisdom() {
        return wisdom;
    }

    public void setWisdom(int wisdom) {
        this.wisdom = wisdom;
    }
}
