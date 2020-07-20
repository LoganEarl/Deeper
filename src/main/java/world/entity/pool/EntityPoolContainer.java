package main.java.world.entity.pool;

import main.java.world.entity.Entity;
import main.java.world.entity.stat.EntityStatContainer;
import main.java.world.entity.stance.BaseStance;
import main.java.world.entity.stat.StatValueContainer;
import main.java.world.trait.Trait;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import static main.java.world.entity.EntityTable.*;

public class EntityPoolContainer implements Entity.SqlExtender {
    private PoolValueContainer currentValues;
    private PoolValueContainer maxValues;
    private Entity boundEntity;

    public static final String SIGNIFIER = "pools";

    private static final String[] HEADERS = new String[]{HP, MAX_HP, MP, MAX_MP, STAMINA, MAX_STAMINA, BURNOUT, MAX_BURNOUT};

    public EntityPoolContainer(Entity entity) {
        this(0, 0, 0, 0, 0, 0, 0, 0, entity);
    }

    public EntityPoolContainer(int hp, int maxHP, int mp, int maxMP, int stamina, int maxStamina, int burnout, int maxBurnout, Entity boundEntity) {
        currentValues = new PoolValueContainer(hp, mp, stamina, burnout);
        maxValues = new PoolValueContainer(maxHP, maxMP, maxStamina, maxBurnout);
        this.boundEntity = boundEntity;
    }

    public EntityPoolContainer(ResultSet readEntry, Entity entity) throws SQLException {
        this(readEntry.getInt(HP), readEntry.getInt(MAX_HP),
             readEntry.getInt(MP), readEntry.getInt(MAX_MP),
             readEntry.getInt(STAMINA), readEntry.getInt(MAX_STAMINA),
             readEntry.getInt(BURNOUT), readEntry.getInt(MAX_BURNOUT), entity);
    }

    public void expendStamina(int stamina) {
        int newStamina = currentValues.getStamina()-stamina;
        if(newStamina < 0) newStamina = 0;
        currentValues.setStamina(newStamina);
    }

    public void damage(int damage) {
        damage(damage, null, 0);
    }

    public void damage(int damage, Entity aggressor, int hitRoll) {
        currentValues.addHp(-1 * damage);
        if (currentValues.getHp() <= 0) {
            //TODO enter dying state
        } else if (currentValues.getHp() < maxValues.getHp() / -4) {
            //TODO enter dead state
        }
    }

    @Override
    public String getSignifier() {
        return SIGNIFIER;
    }

    @Override
    public Object[] getInsertSqlValues() {
        return new Object[]{
                currentValues.getHp(), maxValues.getHp(),
                currentValues.getMp(), maxValues.getMp(),
                currentValues.getStamina(), maxValues.getStamina(),
                currentValues.getBurnout(), maxValues.getBurnout()};
    }

    @Override
    public String[] getSqlColumnHeaders() {
        return HEADERS;
    }


    public void regenPools(BaseStance.RegenPacket regenPacket) {
        if (!isDead()) {
            currentValues.addHp(regenPacket.getHp());
            currentValues.addMp(regenPacket.getMp());
            currentValues.addStamina(regenPacket.getStamina());
            currentValues.addBurn(regenPacket.getBurnout());
            rectifyPools();
        }
    }

    private void rectifyPools() {
        currentValues.clamp(maxValues);
    }

    /**
     * Recalculates the maximum hp/mp/stamina/burnout values based on the entity's stats
     */
    public void calculatePoolMaxes(StatValueContainer stats) {
        Set<Trait> traits = boundEntity.getTransitiveTraits();
        maxValues.setHp(traits.stream().mapToInt(trait -> trait.getPoolModifiers().getHp()).sum() + calculateBaseHp(stats));
        maxValues.setStamina(traits.stream().mapToInt(trait -> trait.getPoolModifiers().getStamina()).sum() + calculateBaseStamina(stats));
        maxValues.setMp(traits.stream().mapToInt(trait -> trait.getPoolModifiers().getMp()).sum() + calculateBaseMp(stats));
        maxValues.setBurnout(traits.stream().mapToInt(trait -> trait.getPoolModifiers().getBurnout()).sum() + calculateBaseBurnout(stats));
    }

    public static int calculateBaseHp(StatValueContainer stats){
        return (stats.getStrength() / 2 + stats.getDexterity() / 2 + (int) (stats.getToughness() * 1.5) * 10);
    }

    public static int calculateBaseStamina(StatValueContainer stats){
        return (stats.getDexterity() / 2 + stats.getStrength() / 2 + (int) (stats.getFitness() * 1.5) * 10);
    }

    public static int calculateBaseMp(StatValueContainer stats){
        return (stats.getIntelligence() * 2 + stats.getWisdom()) * 10;
    }

    public static int calculateBaseBurnout(StatValueContainer stats){
        return (stats.getWisdom() * 2 + stats.getIntelligence()) * 10;
    }

    public void fill() {
        currentValues = new PoolValueContainer(maxValues);
    }

    public boolean isDying() {
        return currentValues.getHp() <= 0 && currentValues.getHp() > maxValues.getHp()/ -4;
    }

    public boolean isDead() {
        return currentValues.getHp() <= maxValues.getHp() / -4;
    }

    public PoolValueContainer getCurrentValues(){
        return currentValues;
    }

    public PoolValueContainer getMaxValues(){
        return maxValues;
    }
}
