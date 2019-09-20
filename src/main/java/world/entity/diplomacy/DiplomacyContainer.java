package main.java.world.entity.diplomacy;


import main.java.world.diplomacy.Faction;
import main.java.world.entity.Entity;
import main.java.world.entity.EntityTable;

public class DiplomacyContainer implements Entity.SqlExtender {
    public static final String SIGNIFIER = "diplomacy";

    //TODO remove this
    private int factionID = 0;

    @Override
    public String getSignifier() {
        return SIGNIFIER;
    }

    @Override
    public Object[] getInsertSqlValues() {
        return new Object[]{0};
    }

    @Override
    public String[] getSqlColumnHeaders() {
        return new String[]{EntityTable.FACTION_ID};
    }


    public Faction getFaction(){
        return null;
    }
}
