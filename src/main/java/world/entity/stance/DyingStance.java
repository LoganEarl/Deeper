package main.java.world.entity.stance;

import main.java.world.entity.Attack;
import main.java.world.entity.skill.Skill;

public class DyingStance extends Stance {
    private static final double LOSS_RATE = -5;

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

    @Override
    public Attack modifyIncomingAttack(Attack in) {
        in.setAttemptedDamage((int)(in.getAttemptedDamage() * 1.25));
        return in;
    }

    @Override
    public Attack modifyOutgoingAttack(Attack in) {
        in.setAttemptedDamage((int)(in.getAttemptedDamage() * 0.75));
        return in;
    }
}
