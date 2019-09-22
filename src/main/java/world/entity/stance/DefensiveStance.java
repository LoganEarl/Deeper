package main.java.world.entity.stance;

import main.java.world.entity.Attack;
import main.java.world.entity.skill.Skill;

public class DefensiveStance extends Stance{
    private static final double BASE_HP_PER_TOUGH_PER_SEC = 0.044;
    private static final double BASE_STAM_PER_FIT_PER_SEC = 0.036;
    private static final double MP_PER_INT_PER_SEC = 0.026;
    private static final double MP_PER_WIS_PER_SEC = 0.014;
    private static final double BURN_PER_WIS_PER_SEC = 0.026;
    private static final double BURN_PER_INT_PER_SEC = 0.014;

    @Override
    public Attack modifyAttack(Attack in) {



        return in;
    }

    @Override
    public int getIPGained(int baseIP) {
        return baseIP;
    }

    @Override
    public double getFlatHpPerSec() {
        return 0;
    }

    @Override
    public double getFlatMpPerSec() {
        return 0;
    }

    @Override
    public double getFlatStamPerSec() {
        return 0;
    }

    @Override
    public double getFlatBurnPerSec() {
        return 0;
    }

    @Override
    public double getBaseHpPerToughPerSec() {
        return BASE_HP_PER_TOUGH_PER_SEC;
    }

    @Override
    public double getBaseStamPerFitPerSec() {
        return BASE_STAM_PER_FIT_PER_SEC;
    }

    @Override
    public double getMpPerIntPerSec() {
        return MP_PER_INT_PER_SEC;
    }

    @Override
    public double getMpPerWisPerSec() {
        return MP_PER_WIS_PER_SEC;
    }

    @Override
    public double getBurnPerWisPerSec() {
        return BURN_PER_WIS_PER_SEC;
    }

    @Override
    public double getBurnPerIntPerSec() {
        return BURN_PER_INT_PER_SEC;
    }

    @Override
    public Skill getRequiredSkill() {
        return Skill.deflect1;
    }

    @Override
    public boolean isLearnable() {
        return true;
    }
}
