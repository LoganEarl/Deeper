package main.java.world.entity.stance;

import main.java.world.entity.Attack;
import main.java.world.entity.Entity;
import main.java.world.entity.skill.Skill;

public class BaseStance extends Stance{
    private static final double BASE_HP_PER_TOUGH_PER_SEC = 0.04;
    private static final double BASE_STAM_PER_FIT_PER_SEC = 0.04;
    private static final double MP_PER_INT_PER_SEC = 0.026;
    private static final double MP_PER_WIS_PER_SEC = 0.014;
    private static final double BURN_PER_WIS_PER_SEC = 0.026;
    private static final double BURN_PER_INT_PER_SEC = 0.014;

    public int onDamageIncoming(int baseDamage, Entity aggressor, int hitRoll){
        return baseDamage;
    }

    public int getIPGained(int baseIP){
        return baseIP;
    }

    public double getFlatHpPerSec(){
        return 0;
    }

    public double getFlatMpPerSec(){
        return 0;
    }

    public double getFlatStamPerSec(){
        return 0;
    }

    public double getFlatBurnPerSec(){
        return 0;
    }

    public double getBaseHpPerToughPerSec(){
        return BASE_HP_PER_TOUGH_PER_SEC;
    }

    public double getBaseStamPerFitPerSec(){
        return BASE_STAM_PER_FIT_PER_SEC;
    }

    public double getMpPerIntPerSec() {
        return MP_PER_INT_PER_SEC;
    }

    public double getMpPerWisPerSec() {
        return MP_PER_WIS_PER_SEC;
    }

    public double getBurnPerWisPerSec() {
        return BURN_PER_WIS_PER_SEC;
    }

    public double getBurnPerIntPerSec() {
        return BURN_PER_INT_PER_SEC;
    }

    public Skill getRequiredSkill(){
        return null;
    }

    @Override
    public Attack modifyAttack(Attack in) {
        return in.setDamageDealt(in.getAttemptedDamage());
    }

    public boolean isLearnable(){
        return true;
    }
}
