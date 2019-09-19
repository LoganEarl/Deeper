package world.entity.stance;

import world.entity.Attack;
import world.entity.skill.Skill;

public class StabilizedStance extends Stance {
    private static final double BASE_HP_PER_TOUGH_PER_SEC = 0.01;
    private static final double BASE_STAM_PER_FIT_PER_SEC = 0.01;
    private static final double MP_PER_INT_PER_SEC = 0.006;
    private static final double MP_PER_WIS_PER_SEC = 0.002;
    private static final double BURN_PER_WIS_PER_SEC = 0.006;
    private static final double BURN_PER_INT_PER_SEC = 0.004;

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
    public Skill getRequiredSkill() {
        return null;
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
    public boolean isLearnable() {
        return false;
    }

    @Override
    public Attack modifyAttack(Attack in) {
        return in.setDamageDealt((int)(in.getAttemptedDamage() * 1.5));
    }
}
