package world.entity.skill;

import world.entity.Entity;
import world.entity.stance.Stance;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SkillContainer implements Entity.SqlExtender {
    public static final String SIGNIFIER = "skills";

    public static final int UNLEARNED = -1;

    private static Random RND = new Random(System.currentTimeMillis());

    private Entity sourceEntity;
    private Map<String, Integer> skillLevels;

    public SkillContainer(Entity sourceEntity){
        skillLevels = new HashMap<>();
        this.sourceEntity = sourceEntity;

        updateSkills();
    }

    private void updateSkills(){
        for(Skill skill: SkillTable.getEntitySkills(sourceEntity))
            skillLevels.put(skill.getSavableName(),skill.getElevationLevel());
    }

    public int getLearnLevel(Skill skill){
        if(skillLevels.containsKey(skill.getSavableName()))
            return skillLevels.get(skill.getSavableName());
        return UNLEARNED;
    }

    public boolean learnSkill(Skill toLearn){
        boolean status = false;
        if(sourceEntity.getProgression().getIP() >= toLearn.getIPCost()){
            sourceEntity.getProgression().addIP(-1 * toLearn.getIPCost());
            status = true;
        }

        if(status && SkillTable.learnSkill(sourceEntity,toLearn, toLearn.getElevationLevel()))
            skillLevels.put(toLearn.getSavableName(), toLearn.getElevationLevel());

        if(!status) //if we failed, refund them the ip
            sourceEntity.getProgression().addIP(toLearn.getIPCost());

        return status;
    }

    public int performSkillCheck(Skill toCheck, int baseNumber, int statLevel){
        int baseRoll = RND.nextInt(101);
        int skillBonus;

        int skillLevel = getLearnLevel(toCheck);
        if(skillLevel != UNLEARNED)
            skillBonus = skillLevel * 10;
        else
            skillBonus = -20;

        //on crit return 100 or the highest possible, whichever is higher
        if(baseRoll == 0) return baseNumber > 0? 100 + baseNumber: 100;

        return baseNumber + (statLevel - baseRoll) + skillBonus;
    }

    @Override
    public String getSignifier() {
        return SIGNIFIER;
    }

    @Override
    public Object[] getInsertSqlValues() {
        return new Object[0];
    }

    @Override
    public String[] getSqlColumnHeaders() {
        return new String[0];
    }

    @Override
    public void registerStance(Stance toRegister) {

    }
}
