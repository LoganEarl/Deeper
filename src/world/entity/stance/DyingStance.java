package world.entity.stance;

import world.entity.Entity;
import world.entity.skill.Skill;

public class DyingStance extends Stance {
    private static final double LOSS_RATE = -.8;

    @Override
    public double getFlatHpPerSec() {
        return LOSS_RATE;
    }

    @Override
    public double getFlatMpPerSec() {
        return LOSS_RATE;
    }

    @Override
    public double getFlatStamPerSec() {
        return LOSS_RATE;
    }

    @Override
    public double getFlatBurnPerSec() {
        return LOSS_RATE;
    }

    @Override
    public double getBaseHpPerToughPerSec() {
        return 0;
    }

    @Override
    public double getBaseStamPerFitPerSec() {
        return 0;
    }

    @Override
    public double getMpPerIntPerSec() {
        return 0;
    }

    @Override
    public double getMpPerWisPerSec() {
        return 0;
    }

    @Override
    public double getBurnPerWisPerSec() {
        return 0;
    }

    @Override
    public double getBurnPerIntPerSec() {
        return 0;
    }

    @Override
    public int onDamageIncoming(int baseDamage, Entity aggressor, int hitRoll) {
        return 0;
    }

    @Override
    public int getIPGained(int baseIP) {
        return baseIP;
    }

    @Override
    public Skill getRequiredSkill() {
        return null;
    }

    @Override
    public boolean isLearnable() {
        return false;
    }
}
