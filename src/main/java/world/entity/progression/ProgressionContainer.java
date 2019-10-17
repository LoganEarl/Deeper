package main.java.world.entity.progression;

import main.java.world.entity.Entity;

import java.sql.ResultSet;
import java.sql.SQLException;

import static main.java.world.entity.EntityTable.IP;
import static main.java.world.entity.EntityTable.TOTAL_IP;

public class ProgressionContainer implements Entity.SqlExtender {
    public static final String SIGNIFIER = "progression";

    private int ip = 0;
    private int totalIp = 0;

    private static final String[] HEADERS = new String[]{IP, TOTAL_IP};

    @SuppressWarnings("FieldCanBeLocal")
    private Entity sourceEntity;

    public ProgressionContainer(Entity sourceEntity){
        this.sourceEntity = sourceEntity;
    }

    public ProgressionContainer(ResultSet readEntry) throws SQLException {
        ip = readEntry.getInt(IP);
        totalIp = readEntry.getInt(TOTAL_IP);
    }

    public int getIP() {
        return ip;
    }

    public int getTotalIP(){
        return totalIp;
    }

    public void addIP(int ip){
        this.ip += ip;
        this.totalIp += ip;
    }

    public int getIPCostForNextStat(int newStatVal, int curStatVal, int racialBaseStat){
        int totalIP = 0;
        for(int statValue = curStatVal + 1; statValue <= newStatVal; statValue++) {
            int nextLevelCost = 10 * (statValue - racialBaseStat);
            if(nextLevelCost < 25) nextLevelCost = 25;
            totalIP += nextLevelCost;
        }

        return totalIP;
    }

    @Override
    public String getSignifier() {
        return SIGNIFIER;
    }

    @Override
    public Object[] getInsertSqlValues() {
        return new Object[]{ip, totalIp};
    }

    @Override
    public String[] getSqlColumnHeaders() {
        return HEADERS;
    }

    public static String getIPBrightnessDescriptor(int ipAmount){
        if(ipAmount <= 800)
            return "dim";
        if(ipAmount <= 1500)
            return "warm";
        if(ipAmount <= 3000)
            return "bright";
        if(ipAmount <= 6000)
            return "radiant";
        if(ipAmount <= 12000)
            return "dazzling";
        if(ipAmount <= 24000)
            return "luminous";
        return "blinding";
    }
}
