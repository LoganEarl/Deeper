package world.entity.diplomacy;


import world.diplomacy.Faction;
import world.entity.Entity;
import world.entity.EntityTable;
import world.entity.stance.Stance;

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

    @Override
    public void registerStance(Stance toRegister) {

    }

    public Faction getFaction(){
        return null;
    }
}