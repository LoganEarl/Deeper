package main.java.world.entity.stat;

import main.java.world.entity.Entity;
import main.java.world.entity.EntityTable;
import main.java.world.meta.World;

import java.util.HashMap;
import java.util.Map;

public class StatValueContainer {
    public enum Stat {
        STR(EntityTable.STR),
        DEX(EntityTable.DEX),
        INT(EntityTable.INT),
        WIS(EntityTable.WIS),
        TOUGH(EntityTable.TOUGH),
        FIT(EntityTable.FIT);

        private String sqlHeader;
        public static final String[] SQL_HEADERS = {EntityTable.STR,EntityTable.DEX,EntityTable.INT,EntityTable.WIS, EntityTable.FIT, EntityTable.TOUGH};

        Stat(String sqlHeader) {
            this.sqlHeader = sqlHeader;
        }

        public String getSqlHeader() {
            return sqlHeader;
        }

        public static Stat fromSqlHeader(String sqlHeader) {
            for (Stat stat : values())
                if (stat.sqlHeader.equals(sqlHeader))
                    return stat;
            throw new IllegalArgumentException("Unknown header");
        }
    }

    private final Map<Stat, Integer> stats = new HashMap<>();

    public StatValueContainer() {
        this(0,0,0,0,0,0);
    }

    public StatValueContainer(int strength, int dexterity, int intelligence, int wisdom, int toughness, int fitness) {
        stats.put(Stat.STR, strength);
        stats.put(Stat.DEX, dexterity);
        stats.put(Stat.INT, intelligence);
        stats.put(Stat.WIS, wisdom);
        stats.put(Stat.TOUGH, toughness);
        stats.put(Stat.FIT, fitness);
    }

    public int getStat(Stat stat){
        return stats.get(stat);
    }

    public void addValuesTo(StatValueContainer addTo){
        for(Stat stat: Stat.values())
            addTo.stats.put(stat, addTo.stats.get(stat) + this.stats.get(stat));
    }

    public int getStrength() {
        return stats.get(Stat.STR);
    }

    public int getDexterity() {
        return stats.get(Stat.DEX);
    }

    public int getIntelligence() {
        return stats.get(Stat.INT);
    }

    public int getWisdom() {
        return stats.get(Stat.WIS);
    }

    public int getToughness() {
        return stats.get(Stat.TOUGH);
    }

    public int getFitness() {
        return stats.get(Stat.FIT);
    }
}
