package world.entity.progression;

import client.commands.PromptCommand;
import world.entity.Entity;
import world.entity.stance.BaseStance;

import java.sql.ResultSet;
import java.sql.SQLException;

import static world.entity.EntityTable.XP;

public class ProgressionContainer implements Entity.SqlExtender {
    public static final String SIGNIFIER = "progression";

    private int xp = 0;

    private static final String[] HEADERS = new String[]{XP};
    private BaseStance stance;

    public ProgressionContainer(){
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
    public void registerStance(BaseStance toRegister) {
        this.stance = toRegister;
    }
}
