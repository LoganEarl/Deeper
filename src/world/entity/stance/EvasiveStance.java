package world.entity.stance;

import world.entity.Attack;
import world.entity.Entity;
import world.entity.skill.Skill;

import java.util.ArrayList;
import java.util.List;

public class EvasiveStance extends Stance {
    private static final double BASE_HP_PER_TOUGH_PER_SEC = 0.018;
    private static final double BASE_STAM_PER_FIT_PER_SEC = 0.022;
    private static final double MP_PER_INT_PER_SEC = 0.013;
    private static final double MP_PER_WIS_PER_SEC = 0.007;
    private static final double BURN_PER_WIS_PER_SEC = 0.013;
    private static final double BURN_PER_INT_PER_SEC = 0.007;

    private Entity sourceEntity;
    //1 to 10, being the number of attacks per 10 attacks that will result in a dodge attempt
    private double targetEvasivenessPercent;

    private List<Integer> hitHistory = new ArrayList<>(10);

    public EvasiveStance(Entity sourceEntity, int evasiveness) {
        this.sourceEntity = sourceEntity;
        this.targetEvasivenessPercent = evasiveness/10.0;
    }

    @Override
    public Attack modifyAttack(Attack in) {
        if(hitHistory.size() > 10) hitHistory.clear();

        double dodgeCloseness = Math.abs(getRatioIfDodge() - targetEvasivenessPercent);
        double takeCloseness = Math.abs(getRatioIfNoDodge() - targetEvasivenessPercent);

        if(dodgeCloseness <= takeCloseness) {
            hitHistory.add(1);
            int learnLevel = sourceEntity.getSkills().getLearnLevel(getRequiredSkill());
            int staminaUsed = 10-2*learnLevel;
            sourceEntity.getPools().expendStamina(staminaUsed);

            int associatedStat = sourceEntity.getStats().getStat(getRequiredSkill().getAssociatedStat());
            int roll = sourceEntity.getSkills().performSkillCheck(getRequiredSkill(), 0, associatedStat);
            if(in.getBaseRoll() <= roll)
                in.setDamageDealt(0).setDidDodge(true);
            else
                in.setDamageDealt(in.getAttemptedDamage());
        }else{
            hitHistory.add(0);
            in.setDamageDealt(in.getAttemptedDamage());
        }
        return in;
    }

    private double getRatioIfDodge(){
        return sum(hitHistory,0)/(hitHistory.size() + 1.0);
    }

    private int sum(List<Integer> ints, int addition){
        int total = addition;
        for(int i : ints)
            total += i;
        return total;
    }

    private double getRatioIfNoDodge(){
        return sum(hitHistory,1)/(hitHistory.size() + 1.0);
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
        return Skill.dodge1;
    }

    @Override
    public boolean isLearnable() {
        return true;
    }
}
