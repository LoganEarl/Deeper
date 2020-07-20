package main.java.world.entity.stat;

import main.java.world.entity.Entity;
import main.java.world.trait.Trait;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static main.java.world.entity.EntityTable.*;

public class EntityStatContainer implements Entity.SqlExtender {
    private static final Random RND = new Random(System.currentTimeMillis());

    public static final String SIGNIFIER = "stats";

    private StatValueContainer baseStats;
    private Entity boundEntity;

    public EntityStatContainer(Entity entity) {
        this(new StatValueContainer(0, 0, 0, 0, 0, 0), entity);
    }

    public EntityStatContainer(StatValueContainer baseStats, Entity entity) {
        this.baseStats = baseStats;
        this.boundEntity = entity;
    }

    public EntityStatContainer(ResultSet readEntry, Entity entity) throws SQLException {
        this(new StatValueContainer(
                     readEntry.getInt(STR),
                     readEntry.getInt(DEX),
                     readEntry.getInt(INT),
                     readEntry.getInt(WIS),
                     readEntry.getInt(TOUGH),
                     readEntry.getInt(FIT)
             ), entity);
    }

    /**
     * performs a stat check on the specified stat and difficulty
     *
     * @param stat               the column name of the stat to roll for.
     * @param difficultyModifier the amount to add or subtract from the result
     * @return the net stat, with 0 and up being a success
     * @throws IllegalArgumentException if you pass in a column name for stat that is not recognized
     */
    public int preformStatCheck(StatValueContainer.Stat stat, int difficultyModifier) {
        int baseStat = baseStats.getStat(stat);

        return baseStat - RND.nextInt(100) + difficultyModifier;
    }

    public double getWeightSoftLimit() {
        //15 to 100 kg based on str
        return baseStats.getStrength() / 100.0 * 85 + 15;
    }

    public double getWeightHardLimit() {
        //40 to 400 kg based on str
        return baseStats.getStrength() / 100.0 * 360 + 40;
    }

    public static int calculateMaxHP(StatValueContainer stats) {
        return stats.getToughness() * 2 + (stats.getStrength() + stats.getDexterity()) / 2;
    }

    public static int calculateMaxStamina(StatValueContainer stats) {
        return stats.getFitness() * 2 + (stats.getStrength() + stats.getDexterity()) / 2;
    }

    public static int calculateMaxMP(StatValueContainer stats) {
        return stats.getIntelligence() * 2 + stats.getWisdom();
    }

    public static int calculateMaxBurnout(StatValueContainer stats) {
        return stats.getWisdom() * 2 + stats.getIntelligence();
    }

    @Override
    public String getSignifier() {
        return SIGNIFIER;
    }

    @Override
    public Object[] getInsertSqlValues() {
        return new Object[]{baseStats.getStrength(), baseStats.getDexterity(), baseStats.getIntelligence(), baseStats.getWisdom(), baseStats.getFitness(), baseStats.getToughness()};
    }

    @Override
    public String[] getSqlColumnHeaders() {
        return StatValueContainer.Stat.SQL_HEADERS;
    }

    @Override
    public String toString() {
        StatValueContainer augmentedStats = getAugmentedValues();
        return String.format(Locale.US, "[STR:%d(%d)] [DEX:%d(%d)] [INT:%d(%d)] [WIS:%d(%d)] [FIT:%d(%d)] [TOUGH:%d(%d)]",
                             augmentedStats.getStrength(), baseStats.getStrength(),
                             augmentedStats.getDexterity(), baseStats.getDexterity(),
                             augmentedStats.getIntelligence(), baseStats.getIntelligence(),
                             augmentedStats.getWisdom(), baseStats.getWisdom(),
                             augmentedStats.getFitness(), baseStats.getFitness(),
                             augmentedStats.getToughness(), baseStats.getToughness());
    }

    public StatValueContainer getBaseValues() {
        return baseStats;
    }

    public StatValueContainer getAugmentedValues() {
        StatValueContainer sum = new StatValueContainer();
        baseStats.addValuesTo(sum);
        for (Trait t : boundEntity.getTransitiveTraits())
            t.getStatModifiers().addValuesTo(sum);
        return sum;
    }
}
