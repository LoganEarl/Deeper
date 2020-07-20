package main.java.world.entity.skill;

import main.java.world.entity.Entity;
import main.java.world.trait.Trait;
import main.java.world.trait.TraitBestower;

import java.util.*;

public class SkillContainer implements Entity.SqlExtender, TraitBestower {
    public static final String SIGNIFIER = "skills";

    public static final int UNLEARNED = -1;

    private static Random RND = new Random(System.currentTimeMillis());

    private String containerID;
    private String databaseName;
    private Map<String, Integer> skillLevels;
    private Skill[] skills;

    public SkillContainer(String containerID, String databaseName){
        skillLevels = new HashMap<>();
        this.containerID = containerID;
        this.databaseName = databaseName;
        updateSkills();
    }

    private void updateSkills(){
        for(Skill skill: SkillTable.getSkillsByContainerID(containerID,databaseName))
            skillLevels.put(skill.getSavableName(),skill.getElevationLevel());
        skills = SkillTable.getSkillsByContainerID(containerID, databaseName);
    }

    public int getLearnLevel(Skill skill){
        if(skillLevels.containsKey(skill.getSavableName()))
            return skillLevels.get(skill.getSavableName());
        return UNLEARNED;
    }

    public boolean learnSkill(Skill toLearn, Entity sourceEntity){
        boolean status = false;
        if(sourceEntity.getProgression().getIP() >= toLearn.getIPCost()){
            sourceEntity.getProgression().subtractIP(toLearn.getIPCost());
            status = true;
        }

        if(status && SkillTable.learnSkill(sourceEntity.getID(),toLearn, toLearn.getElevationLevel(), sourceEntity.getDatabaseName()))
            skillLevels.put(toLearn.getSavableName(), toLearn.getElevationLevel());

        if(!status) //if we failed, refund them the ip
            sourceEntity.getProgression().addIP(toLearn.getIPCost());

        updateSkills();

        return status;
    }

    public int getSkillBonus(Skill toCheck){
        int skillLevel = getLearnLevel(toCheck);
        int skillBonus;
        if(skillLevel != UNLEARNED)
            skillBonus = skillLevel * 10;
        else
            skillBonus = -20;
        return skillBonus;
    }

    public int performSkillCheck(Skill toCheck, int baseNumber, Entity sourceEntity){
        int statLevel = sourceEntity.getStats().getStat(toCheck.getAssociatedStat(), sourceEntity.getTransitiveTraits());
        return performSkillCheck(toCheck, baseNumber, statLevel);
    }

    public int performSkillCheck(Skill toCheck, int baseNumber, int statLevel){
        int baseRoll = RND.nextInt(100);
        int skillBonus = getSkillBonus(toCheck);

        //on crit return 100 or the highest possible, whichever is higher
        if(baseRoll == 0) return baseNumber > 0? 100 + baseNumber: 100;

        return baseNumber + (statLevel - baseRoll) + skillBonus;
    }

    public Skill[] getKnownSkills(){
        return skills;
    }

    @Override
    public Set<Trait> getBestowedTraits() {
        Set<Trait> traits = new HashSet<>();
        for(Skill skill: skills)
            traits.addAll(skill.getBestowedTraits());

        return traits;
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
}
