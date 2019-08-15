package world.entity.stance;

import world.entity.Entity;
import world.entity.StatContainer;
import world.entity.skill.Skill;

public class BaseStance extends Stance{
    private static final double BASE_HP_PER_TOUGH_PER_SEC = 0.02;
    private static final double BASE_STAM_PER_FIT_PER_SEC = 0.02;
    private static final double MP_PER_INT_PER_SEC = 0.013;
    private static final double MP_PER_WIS_PER_SEC = 0.007;
    private static final double BURN_PER_WIS_PER_SEC = 0.013;
    private static final double BURN_PER_INT_PER_SEC = 0.007;

    public int getDamageDealt(int baseDamage, Entity aggressor, int hitRoll){
        return baseDamage;
    }

    public int getXPGained(int baseXP){
        return baseXP;
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

    public boolean isLearnable(){
        return true;
    }
}
