package world.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

import static world.entity.EntityTable.*;

public class PoolContainer implements Entity.SqlExtender{
    private int hp;
    private int maxHP;
    private int mp;
    private int maxMP;
    private int stamina;
    private int maxStamina;
    private int burnout;
    private int maxBurnout;

    public static final String SIGNIFIER = "pools";

    private static final String[] HEADERS = new String[]{HP, MAX_HP, MP,MAX_MP, STAMINA,MAX_STAMINA,BURNOUT,MAX_BURNOUT};

    public PoolContainer(int hp, int maxHP, int mp, int maxMP, int stamina, int maxStamina, int burnout, int maxBurnout) {
        this.hp = hp;
        this.maxHP = maxHP;
        this.mp = mp;
        this.maxMP = maxMP;
        this.stamina = stamina;
        this.maxStamina = maxStamina;
        this.burnout = burnout;
        this.maxBurnout = maxBurnout;
    }

    public PoolContainer(ResultSet readEntry) throws SQLException {
        hp = readEntry.getInt(HP);
        maxHP = readEntry.getInt(MAX_HP);
        mp = readEntry.getInt(MP);
        maxMP = readEntry.getInt(MAX_MP);
        stamina = readEntry.getInt(STAMINA);
        maxStamina = readEntry.getInt(MAX_STAMINA);
        burnout = readEntry.getInt(BURNOUT);
        maxBurnout = readEntry.getInt(MAX_BURNOUT);
    }

    public void damage(int damage){
        this.hp -= damage;
        if(hp <= 0){
            //TODO enter dying state
        }else if(hp< maxHP/-4){
            //TODO enter dead stats
        }
    }

    @Override
    public String getSignifier() {
        return SIGNIFIER;
    }

    @Override
    public Object[] getInsertSqlValues() {
        return new Object[]{hp,maxHP,mp,maxMP,stamina,maxStamina,burnout,maxBurnout};
    }

    @Override
    public String[] getSqlColumnHeaders() {
        return HEADERS;
    }

    /**Recalculates the maximum hp/mp/stamina/burnout values based on the entity's stats*/
    public void calculatePoolMaxes(StatContainer stats){
        maxHP = stats.getStrength() / 2 + stats.getDexterity() / 2 + (int)(stats.getToughness() * 1.5);
        maxStamina = stats.getDexterity() / 2 + stats.getStrength() / 2 + (int)(stats.getFitness() * 1.5);
        maxMP = stats.getIntelligence() * 2 + stats.getWisdom();
        maxBurnout = stats.getWisdom() * 2 + stats.getIntelligence();
    }

    public boolean isDying(){
        return hp <= 0 && hp > getMaxHP()/-4;
    }

    public boolean isDead(){
        return hp <= getMaxHP()/-4;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public int getMaxMP() {
        return maxMP;
    }

    public void setMaxMP(int maxMP) {
        this.maxMP = maxMP;
    }

    public int getStamina() {
        return stamina;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }

    public int getMaxStamina() {
        return maxStamina;
    }

    public void setMaxStamina(int maxStamina) {
        this.maxStamina = maxStamina;
    }

    public int getBurnout() {
        return burnout;
    }

    public void setBurnout(int burnout) {
        this.burnout = burnout;
    }

    public int getMaxBurnout() {
        return maxBurnout;
    }

    public void setMaxBurnout(int maxBurnout) {
        this.maxBurnout = maxBurnout;
    }
}
