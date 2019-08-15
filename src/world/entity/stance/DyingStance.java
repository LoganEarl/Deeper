package world.entity.stance;

import world.entity.Entity;
import world.entity.skill.Skill;

public class DyingStance extends Stance {
    @Override
    public double getFlatHpPerSec() {
        return -1;
    }

    @Override
    public double getFlatMpPerSec() {
        return -5;
    }

    @Override
    public double getFlatStamPerSec() {
        return -5;
    }

    @Override
    public double getFlatBurnPerSec() {
        return -5;
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
    public int getDamageDealt(int baseDamage, Entity aggressor, int hitRoll) {
        return 0;
    }

    @Override
    public int getXPGained(int baseXP) {
        return baseXP;
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
