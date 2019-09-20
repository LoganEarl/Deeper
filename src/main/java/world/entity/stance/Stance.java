package main.java.world.entity.stance;

import main.java.world.entity.Attack;
import main.java.world.entity.StatContainer;
import main.java.world.entity.skill.Skill;

public abstract class Stance implements Attack.AttackModifier {
    private long lastUpdateTime = 0;
    private double carryoverHP = 0, carryOverMP = 0, carryOverStam = 0, carryOverBurn = 0;

    @Override
    public Attack modifyAttack(Attack in) {
        return in;
    }

    public abstract int getIPGained(int baseIP);

    public abstract double getFlatHpPerSec();

    public abstract double getFlatMpPerSec();

    public abstract double getFlatStamPerSec();

    public abstract double getFlatBurnPerSec();

    public abstract double getBaseHpPerToughPerSec();

    public abstract double getBaseStamPerFitPerSec();

    public abstract double getMpPerIntPerSec();

    public abstract double getMpPerWisPerSec();

    public abstract double getBurnPerWisPerSec();

    public abstract double getBurnPerIntPerSec() ;

    public abstract Skill getRequiredSkill();

    public abstract boolean isLearnable();

    public final RegenPacket receiveNextRegenPacket(StatContainer stats, long curTime){
        if(lastUpdateTime == 0) lastUpdateTime = curTime;

        double elapsedSecs = (curTime - lastUpdateTime)/1000.0;

        double hp = (stats.getToughness() * getBaseHpPerToughPerSec() + getFlatHpPerSec()) * elapsedSecs;
        double stam = (stats.getFitness() * getBaseStamPerFitPerSec() + getFlatStamPerSec()) * elapsedSecs;
        double mp = (stats.getIntelligence() * getMpPerIntPerSec() + stats.getWisdom() * getMpPerWisPerSec() + getFlatMpPerSec()) * elapsedSecs;
        double burn = (stats.getWisdom() * getBurnPerWisPerSec() + stats.getIntelligence() * getBurnPerIntPerSec() + getFlatBurnPerSec()) * elapsedSecs;

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
