package world.entity;

import world.entity.skill.Skill;

public class BaseStance {
    private static final double BASE_HP_PER_TOUGH_PER_SEC = 0.02;
    private static final double BASE_STAM_PER_FIT_PER_SEC = 0.02;
    private static final double MP_PER_INT_PER_SEC = 0.013;
    private static final double MP_PER_WIS_PER_SEC = 0.007;
    private static final double BURN_PER_WIS_PER_SEC = 0.013;
    private static final double BURN_PER_INT_PER_SEC = 0.007;

    private long lastUpdateTime = 0;
    private double carryoverHP = 0, carryOverMP = 0, carryOverStam = 0, carryOverBurn = 0;

    public int getDamageDealt(int baseDamage){
        return baseDamage;
    }

    protected double getBaseHpPerToughPerSec(){
        return BASE_HP_PER_TOUGH_PER_SEC;
    }

    protected double getBaseStamPerFitPerSec(){
        return BASE_STAM_PER_FIT_PER_SEC;
    }

    protected double getMpPerIntPerSec() {
        return MP_PER_INT_PER_SEC;
    }

    protected double getMpPerWisPerSec() {
        return MP_PER_WIS_PER_SEC;
    }

    protected double getBurnPerWisPerSec() {
        return BURN_PER_WIS_PER_SEC;
    }

    protected double getBurnPerIntPerSec() {
        return BURN_PER_INT_PER_SEC;
    }

    public Skill getRequiredSkill(){
        return null;
    }

    public final RegenPacket receiveNextRegenPacket(StatContainer stats, long curTime){
        if(lastUpdateTime == 0) lastUpdateTime = curTime;

        double elapsedSecs = (curTime - lastUpdateTime)/1000.0;

        double hp = stats.getToughness() * elapsedSecs * getBaseHpPerToughPerSec();
        double stam = stats.getFitness() * elapsedSecs * getBaseStamPerFitPerSec();
        double mp = (stats.getIntelligence() * getMpPerIntPerSec() + stats.getWisdom() * getMpPerWisPerSec()) * elapsedSecs;
        double burn = (stats.getWisdom() * getBurnPerWisPerSec() + stats.getIntelligence() * getBurnPerIntPerSec()) * elapsedSecs;

        int calculatedHP = (int)(hp + carryoverHP);
        int calculatedStam = (int)(stam + carryOverStam);
        int calculatedMP = (int)(mp + carryOverMP);
        int calculatedBurn = (int)(burn + carryOverBurn);

        RegenPacket regenPacket = new RegenPacket(calculatedHP,calculatedStam,calculatedMP,calculatedBurn);

        carryoverHP = (hp + carryoverHP) - calculatedHP;
        carryOverMP = (mp + carryOverMP) - calculatedMP;
        carryOverStam = (stam + carryOverStam) - calculatedStam;
        carryOverBurn = (burn + carryOverBurn) - calculatedBurn;

        lastUpdateTime = curTime;

        return regenPacket;
    }

    @Override
    public final boolean equals(Object o){
        if(this == o) return true;
        if(o == null) return false;
        return o.getClass().equals(getClass());
    }

    public final class RegenPacket{
        private int hp, stamina, mp, burnout;

        RegenPacket(int hp, int stamina, int mp, int burnout){
            this.hp = hp;
            this.stamina = stamina;
            this.mp = mp;
            this.burnout = burnout;
        }

        public int getHp() {
            return hp;
        }

        public int getStamina() {
            return stamina;
        }

        public int getMp() {
            return mp;
        }

        public int getBurnout() {
            return burnout;
        }
    }
}
