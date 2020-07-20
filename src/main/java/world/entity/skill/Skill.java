package main.java.world.entity.skill;

import main.java.world.entity.Entity;
import main.java.world.entity.stat.StatValueContainer;
import main.java.world.entity.stat.StatValueContainer.Stat;
import main.java.world.trait.Trait;
import main.java.world.trait.TraitBestower;

import java.util.*;

import static main.java.world.entity.skill.Skill.VisibilityType.*;

public enum Skill implements TraitBestower {
    //Stabilize===========================
    stabilize1("Stabilize", 0, 500, Stat.WIS,
               new StatValueContainer(0, 0, 0, 12, 0, 0), visible,
               new Skill[0]),
    stabilize2("Stabilize", 1, 2000, Stat.WIS,
               new StatValueContainer(0, 0, 0, 20, 0, 0), hiddenUntilSkills,
               new Skill[]{stabilize1}),
    stabilize3("Stabilize", 2, 8000, Stat.WIS,
               new StatValueContainer(0, 0, 0, 38, 0, 0), hiddenUntilSkills,
               new Skill[]{stabilize2}),

    //Perception========================
    perception1("Perception", 0, 250, Stat.WIS,
                new StatValueContainer(0, 0, 0, 10, 0, 0), visible,
                new Skill[0]),
    perception2("Perception", 1, 1000, Stat.WIS,
                new StatValueContainer(0, 0, 0, 15, 0, 0), visible,
                new Skill[]{perception1}),
    perception3("Perception", 2, 2000, Stat.WIS,
                new StatValueContainer(0, 0, 0, 25, 0, 0), hiddenUntilStats,
                new Skill[]{perception2}),
    perception4("Perception", 3, 4000, Stat.WIS,
                new StatValueContainer(0, 0, 0, 45, 0, 0), hiddenUntilStats,
                new Skill[]{perception3}),
    perception5("Perception", 4, 8000, Stat.WIS,
                new StatValueContainer(0, 0, 0, 60, 0, 0), hiddenUntilLearnable,
                new Skill[]{perception4}),
    perception6("Perception", 5, 12000, Stat.WIS,
                new StatValueContainer(0, 0, 0, 100, 0, 0), hiddenUntilLearnable,
                new Skill[]{perception5}),
    perception7("Perception", 6, 16000, Stat.WIS,
                new StatValueContainer(0, 0, 0, 150, 0, 0), hiddenUntilLearnable,
                new Skill[]{perception6}),

    //Obscure Intent====================
    obscureIntent1("Obscure Intent", 0, 250, Stat.WIS,
                   new StatValueContainer(0, 0, 0, 10, 0, 0), visible,
                   new Skill[]{perception1}),
    obscureIntent2("Obscure Intent", 1, 1000, Stat.WIS,
                   new StatValueContainer(0, 0, 0, 15, 0, 0), visible,
                   new Skill[]{obscureIntent1}),
    obscureIntent3("Obscure Intent", 2, 2000, Stat.WIS,
                   new StatValueContainer(0, 0, 0, 25, 0, 0), hiddenUntilStats,
                   new Skill[]{obscureIntent2}),
    obscureIntent4("Obscure Intent", 3, 4000, Stat.WIS,
                   new StatValueContainer(0, 0, 0, 45, 0, 0), hiddenUntilStats,
                   new Skill[]{obscureIntent3}),
    obscureIntent5("Obscure Intent", 4, 8000, Stat.WIS,
                   new StatValueContainer(0, 0, 0, 60, 0, 0), hiddenUntilLearnable,
                   new Skill[]{obscureIntent4}),
    obscureIntent6("Obscure Intent", 5, 12000, Stat.WIS,
                   new StatValueContainer(0, 0, 0, 100, 0, 0), hiddenUntilLearnable,
                   new Skill[]{obscureIntent5}),
    obscureIntent7("Obscure Intent", 6, 16000, Stat.WIS,
                   new StatValueContainer(0, 0, 0, 150, 0, 0), hiddenUntilLearnable,
                   new Skill[]{obscureIntent6}),

    //Recover===========================
    recover1("Recover", 0, 250, Stat.TOUGH,
             new StatValueContainer(0, 0, 0, 0, 14, 10), visible,
             new Skill[0]),
    recover2("Recover", 1, 1000, Stat.TOUGH,
             new StatValueContainer(0, 0, 0, 0, 28, 20), hiddenUntilSkills,
             new Skill[]{recover1}),
    recover3("Recover", 2, 4000, Stat.TOUGH,
             new StatValueContainer(0, 0, 0, 0, 62, 40), hiddenUntilLearnable,
             new Skill[]{recover2}),
    recover4("Recover", 3, 16000, Stat.TOUGH,
             new StatValueContainer(0, 0, 0, 0, 100, 88), hiddenUntilLearnable,
             new Skill[]{recover3}),

    //Dodge===========================
    dodge1("Dodge", 0, 750, Stat.DEX,
           new StatValueContainer(0, 12, 0, 0, 0, 0), visible,
           new Skill[0]),
    dodge2("Dodge", 1, 3000, Stat.DEX,
           new StatValueContainer(0, 32, 0, 0, 0, 0), hiddenUntilSkills,
           new Skill[]{dodge1}),
    dodge3("Dodge", 2, 6000, Stat.DEX,
           new StatValueContainer(0, 64, 0, 0, 0, 0), hiddenUntilLearnable,
           new Skill[]{dodge2}),
    dodge4("Dodge", 3, 24000, Stat.DEX,
           new StatValueContainer(0, 120, 0, 0, 0, 0), hiddenUntilLearnable,
           new Skill[]{dodge3}),

    //Deflect===========================
    deflect1("Deflect", 0, 750, Stat.STR,
             new StatValueContainer(12, 0, 0, 0, 0, 0), visible,
             new Skill[0]),
    deflect2("Deflect", 1, 3000, Stat.STR,
             new StatValueContainer(32, 0, 0, 0, 0, 0), hiddenUntilSkills,
             new Skill[]{deflect1}),
    deflect3("Deflect", 2, 6000, Stat.STR,
             new StatValueContainer(64, 0, 0, 0, 0, 0), hiddenUntilLearnable,
             new Skill[]{deflect2}),
    deflect4("Deflect", 3, 24000, Stat.STR,
             new StatValueContainer(120, 0, 0, 0, 0, 0), hiddenUntilLearnable,
             new Skill[]{deflect3}),
    //Athletics=========================
    athletics1("Athletics", 0, 250, Stat.STR,
               new StatValueContainer(12, 0, 0, 0, 0, 0), visible,
               new Skill[0]),
    athletics2("Athletics", 1, 1000, Stat.STR,
               new StatValueContainer(32, 10, 0, 0, 0, 0), hiddenUntilSkills,
               new Skill[]{athletics1}),
    athletics3("Athletics", 2, 4000, Stat.STR,
               new StatValueContainer(64, 20, 0, 0, 0, 0), hiddenUntilLearnable,
               new Skill[]{athletics2}),
    athletics4("Athletics", 3, 16000, Stat.STR,
               new StatValueContainer(120, 40, 0, 0, 0, 0), hiddenUntilLearnable,
               new Skill[]{athletics3}),
    //Acrobatics=========================
    acrobatics1("Acrobatics", 0, 250, Stat.DEX,
                new StatValueContainer(0, 12, 0, 0, 0, 0), visible,
                new Skill[0]),
    acrobatics2("Acrobatics", 1, 1000, Stat.DEX,
                new StatValueContainer(10, 32, 0, 0, 0, 0), hiddenUntilSkills,
                new Skill[]{acrobatics1}),
    acrobatics3("Acrobatics", 2, 4000, Stat.DEX,
                new StatValueContainer(20, 64, 0, 0, 0, 0), hiddenUntilLearnable,
                new Skill[]{acrobatics2}),
    acrobatics4("Acrobatics", 3, 16000, Stat.DEX,
                new StatValueContainer(40, 120, 0, 0, 0, 0), hiddenUntilLearnable,
                new Skill[]{acrobatics3});

    public static final String[] modifiers = {"", "Improved", "Refined", "Masterful", "Perfected", "Unnatural","Godlike"};

    private final String savableName;
    private final int ipCost;
    private final Skill[] requiredSkills;
    private final Set<Trait> requiredTraits;
    private final Set<Trait> bestowedTraits;
    private final StatValueContainer requiredStats;
    private final VisibilityType visibilityType;
    private final int elevationLevel;
    private final Stat associatedStat;

    Skill(String savableName, int elevationLevel, int ipCost, Stat associatedStat, StatValueContainer requiredStats, VisibilityType visibilityType, Skill[] requiredSkills) {
        this(savableName,elevationLevel,ipCost,associatedStat,requiredStats,visibilityType,requiredSkills,new Trait[0], new Trait[0]);
    }

    Skill(String savableName, int elevationLevel, int ipCost, Stat associatedStat, StatValueContainer requiredStats, VisibilityType visibilityType, Skill[] requiredSkills, Trait[] requiredTraits, Trait[] bestowedTraits) {
        this.elevationLevel = elevationLevel;
        this.savableName = savableName;
        this.ipCost = ipCost;
        this.requiredSkills = requiredSkills;
        this.requiredStats = requiredStats;
        this.visibilityType = visibilityType;
        this.associatedStat = associatedStat;
        Set<Trait> tempSet = new HashSet<>(Arrays.asList(requiredTraits));
        this.requiredTraits = tempSet;
        tempSet = new HashSet<>(Arrays.asList(bestowedTraits));
        this.bestowedTraits = tempSet;
    }

    public String getSavableName() {
        return savableName;
    }

    public Stat getAssociatedStat() {
        return associatedStat;
    }

    public String getDisplayName() {
        String modifier = elevationLevel > 0?  modifiers[elevationLevel] + " ": "";
        return modifier + savableName;
    }

    public static List<Skill> getSkillsThatRequire(Skill skill){
        List<Skill> toReturn = new ArrayList<>();
        for (Skill s : Skill.values()) {
            if(Arrays.asList(s.requiredSkills).contains(skill))
                toReturn.add(s);
        }
        return toReturn;
    }

    public int getIPCost() {
        return ipCost;
    }

    public StatValueContainer getRequiredStats() {
        return requiredStats;
    }

    public Skill[] getRequiredSkills() {
        return requiredSkills;
    }

    public Set<Trait> getRequiredTraits(){
        return requiredTraits;
    }

    public Set<Trait> getBestowedTraits(){
        return bestowedTraits;
    }

    public static Skill getSkillOfDisplayName(String savableName, int elevationLevel) {
        for (Skill skill : Skill.values()) {
            if (skill.getSavableName().toLowerCase().equals(savableName.toLowerCase()) && skill.getElevationLevel() == elevationLevel)
                return skill;
        }
        return null;
    }

    public int getElevationLevel() {
        return elevationLevel;
    }

    public static Skill getSkill(String savableName, int elevationLevel) {
        if(savableName != null) {
            for (Skill skill : Skill.values()) {
                if (skill.savableName.toLowerCase().equals(savableName.toLowerCase()) && skill.elevationLevel == elevationLevel)
                    return skill;
            }
        }
        return null;
    }

    public static Skill getGeneralSkill(String savableName){
        return getSkill(savableName, 0);
    }

    public static Skill getLearnLevel(Skill baseSkill, int learnLevel){
        if(baseSkill == null)
            return null;

        for(Skill skill : Skill.values()){
            if (skill.savableName.equals(baseSkill.savableName) && skill.elevationLevel == learnLevel) return skill;
        }
        return baseSkill;
    }

    public boolean isLearnableByEntity(Entity entity){
        SkillContainer skillSet = entity.getSkills();
        for(Skill skill:requiredSkills)
            if(skillSet.getLearnLevel(skill) == SkillContainer.UNLEARNED)
                return false;

        StatValueContainer entityStats = entity.getStats().getAugmentedValues();
        return meetsStatRequirements(entityStats);
    }

    public boolean isVisibleToEntity(Entity entity) {
        SkillContainer skillSet = entity.getSkills();
        StatValueContainer statSet = entity.getStats().getAugmentedValues();

        switch (visibilityType) {
            case visible:
                return true;
            case hiddenUntilSkills:
                for (Skill prereq : requiredSkills)
                    if (skillSet.getLearnLevel(prereq) == SkillContainer.UNLEARNED) return false;
                return true;
            case hiddenUntilLearnable:
                for (Skill prereq : requiredSkills)
                    if (skillSet.getLearnLevel(prereq) == SkillContainer.UNLEARNED) return false;
            case hiddenUntilStats:
                return meetsStatRequirements(statSet);
            case hidden:
                return false;
        }
        return false;
    }

    private boolean meetsStatRequirements(StatValueContainer stats){
        return stats.getStrength() >= requiredStats.getStrength() &&
                stats.getDexterity() >= requiredStats.getDexterity() &&
                stats.getIntelligence() >= requiredStats.getIntelligence() &&
                stats.getWisdom() >= requiredStats.getWisdom() &&
                stats.getFitness() >= requiredStats.getFitness() &&
                stats.getToughness() >= requiredStats.getToughness();
    }

    public enum VisibilityType {
        visible, hiddenUntilSkills, hiddenUntilStats, hiddenUntilLearnable, hidden
    }
}
