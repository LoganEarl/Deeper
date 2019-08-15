package world.entity.progression;

import world.entity.Entity;
import world.entity.stance.Stance;

import java.sql.ResultSet;
import java.sql.SQLException;

import static world.entity.EntityTable.XP;

public class ProgressionContainer implements Entity.SqlExtender {
    public static final String SIGNIFIER = "progression";

    private int xp = 0;

    private static final String[] HEADERS = new String[]{XP};
    private Stance stance;

    private Entity sourceEntity;

    public ProgressionContainer(Entity sourceEntity){
        this.sourceEntity = sourceEntity;
    }

    public ProgressionContainer(ResultSet readEntry) throws SQLException {
        xp = readEntry.getInt(XP);
    }

    public int getXP() {
        return xp;
    }

    public void setXP(int xp) {
        this.xp = xp;
    }

    public void addXP(int xp){
        this.xp += stance.getXPGained(xp);
    }

    public int getXPCostForNextStat(int newStatVal, int curStatVal, int racialBaseStat){
        int totalXP = 0;
        for(int statValue = curStatVal + 1; statValue <= newStatVal; statValue++) {
            int nextLevelCost = 10 * (statValue - racialBaseStat);
            if(nextLevelCost < 25) nextLevelCost = 25;
            totalXP += nextLevelCost;
        }

        return totalXP;
    }

    @Override
    public String getSignifier() {
        return SIGNIFIER;
    }

    @Override
    public Object[] getInsertSqlValues() {
        return new Object[]{xp};
    }

    @Override
    public String[] getSqlColumnHeaders() {
        return HEADERS;
    }

    @Override
    public void registerStance(Stance toRegister) {
        this.stance = toRegister;
    }
}
