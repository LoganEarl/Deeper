package main.java.world.entity.skill;

import main.java.world.entity.Entity;
import main.java.world.entity.EntityTable;
import main.java.world.entity.StatContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static main.java.world.entity.skill.Skill.VisibilityType.*;

public enum Skill {
    //Stabilize===========================
    stabilize1("Stabilize", 0, 500, EntityTable.WIS,
            new StatContainer(0, 0, 0, 12, 0, 0), visible),
    stabilize2("Stabilize", 1, 2000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 20, 0, 0), hiddenUntilSkills,
            stabilize1),
    stabilize3("Stabilize", 2, 8000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 38, 0, 0), hiddenUntilSkills,
            stabilize2),

    //Perception========================
    perception1("Perception",0,250, EntityTable.WIS,
            new StatContainer(0, 0, 0, 10, 0, 0), visible),
    perception2("Perception",1,1000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 15, 0, 0), visible,
            perception1),
    perception3("Perception",2,2000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 25, 0, 0), hiddenUntilStats,
            perception2),
    perception4("Perception",3,4000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 45, 0, 0), hiddenUntilStats,
            perception3),
    perception5("Perception",4,8000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 60, 0, 0), hiddenUntilLearnable,
            perception4),
    perception6("Perception",5,12000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 100, 0, 0), hiddenUntilLearnable,
            perception5),
    perception7("Perception",6,16000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 150, 0, 0), hiddenUntilLearnable,
            perception6),

    //Obscure Intent====================
    obscureIntent1("Obscure Intent",0,250,EntityTable.WIS,
            new StatContainer(0, 0, 0, 10, 0, 0), visible,
            perception1),
    obscureIntent2("Obscure Intent",1,1000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 15, 0, 0), visible,
            obscureIntent1, perception2),
    obscureIntent3("Obscure Intent",2,2000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 25, 0, 0), hiddenUntilStats,
            obscureIntent2, perception3),
    obscureIntent4("Obscure Intent",3,4000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 45, 0, 0), hiddenUntilStats,
            obscureIntent3, perception4),
    obscureIntent5("Obscure Intent",4,8000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 60, 0, 0), hiddenUntilLearnable,
            obscureIntent4, perception5),
    obscureIntent6("Obscure Intent",5,12000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 100, 0, 0), hiddenUntilLearnable,
            obscureIntent5, perception6),
    obscureIntent7("Obscure Intent",6,16000, EntityTable.WIS,
            new StatContainer(0, 0, 0, 150, 0, 0), hiddenUntilLearnable,
            obscureIntent6, perception7),

    //Recover===========================
    recover1("Recover", 0, 250, EntityTable.TOUGH,
            new StatContainer(0, 0, 0, 0, 14, 10), visible),
    recover2("Recover", 1, 1000, EntityTable.TOUGH,
            new StatContainer(0, 0, 0, 0, 28, 20), hiddenUntilSkills,
            recover1),
    recover3("Recover", 2, 4000, EntityTable.TOUGH,
            new StatContainer(0, 0, 0, 0, 62, 40), hiddenUntilLearnable,
            recover2),
    recover4("Recover", 3, 16000, EntityTable.TOUGH,
            new StatContainer(0, 0, 0, 0, 100, 88), hiddenUntilLearnable,
            recover3),

    //Dodge===========================
    dodge1("Dodge", 0, 750, EntityTable.DEX,
            new StatContainer(0, 12, 0, 0, 0, 0), visible),
    dodge2("Dodge", 1, 3000, EntityTable.DEX,
            new StatContainer(0, 32, 0, 0, 0, 0), hiddenUntilSkills,
            dodge1),
    dodge3("Dodge", 2, 6000, EntityTable.DEX,
            new StatContainer(0, 64, 0, 0, 0, 0), hiddenUntilLearnable,
            dodge2),
    dodge4("Dodge", 3, 24000, EntityTable.DEX,
            new StatContainer(0, 120, 0, 0, 0, 0), hiddenUntilLearnable,
            dodge3),

    //Deflect===========================
    deflect1("Deflect", 0, 750, EntityTable.STR,
            new StatContainer(12, 0, 0, 0, 0, 0), visible),
    deflect2("Deflect", 1, 3000, EntityTable.STR,
            new StatContainer(32, 0, 0, 0, 0, 0), hiddenUntilSkills,
            deflect1),
    deflect3("Deflect", 2, 6000, EntityTable.STR,
            new StatContainer(64, 0, 0, 0, 0, 0), hiddenUntilLearnable,
            deflect2),
    deflect4("Deflect", 3, 24000, EntityTable.STR,
            new StatContainer(120, 0, 0, 0, 0, 0), hiddenUntilLearnable,
            deflect3);

    public static final String[] modifiers = {"", "Improved", "Refined", "Masterful", "Perfected", "Unnatural","Godlike"};

    private String savableName;
    private int ipCost;
    private Skill[] requiredSkills;
    private StatContainer requiredStats;
    private VisibilityType visibilityType;

    private int elevationLevel;

    private String associatedStat;

    Skill(String savableName, int elevationLevel, int ipCost, String associatedStat, StatContainer requiredStats, VisibilityType visibilityType, Skill... requiredSkills) {
        this.elevationLevel = elevationLevel;
        this.savableName = savableName;
        this.ipCost = ipCost;
        this.requiredSkills = requiredSkills;
        this.requiredStats = requiredStats;
        this.visibilityType = visibilityType;
        this.associatedStat = associatedStat;
    }

    public String getSavableName() {
        return savableName;
    }

    public String getAssociatedStat() {
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

    public StatContainer getRequiredStats() {
        return requiredStats;
    }

    public Skill[] getRequiredSkills() {
        return requiredSkills;
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
        for (Skill skill : Skill.values()) {
            if (skill.savableName.toLowerCase().equals(savableName.toLowerCase()) && skill.elevationLevel == elevationLevel)
                return skill;
        }
        return null;
    }

    public static Skill getLearnLevel(Skill baseSkill, int learnLevel){
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

        StatContainer entityStats = entity.getStats();
        return entityStats.getStrength() >= requiredStats.getStrength() &&
                entityStats.getDexterity() >= requiredStats.getDexterity() &&
                entityStats.getIntelligence() >= requiredStats.getIntelligence() &&
                entityStats.getWisdom() >= requiredStats.getWisdom() &&
                entityStats.getFitness() >= requiredStats.getFitness() &&
                entityStats.getToughness() >= requiredStats.getToughness();
    }

    public boolean isVisibleToEntity(Entity entity) {
        SkillContainer skillSet = entity.getSkills();
        StatContainer statSet = entity.getStats();

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
                return statSet.getStrength() >= requiredStats.getStrength() &&
                        statSet.getDexterity() >= requiredStats.getDexterity() &&
                        statSet.getIntelligence() >= requiredStats.getIntelligence() &&
                        statSet.getWisdom() >= requiredStats.getWisdom() &&
                        statSet.getFitness() >= requiredStats.getFitness() &&
                        statSet.getToughness() >= requiredStats.getToughness();
            case hidden:
                return false;
        }
        return false;
    }

    public enum VisibilityType {
        visible, hiddenUntilSkills, hiddenUntilStats, hiddenUntilLearnable, hidden
    }
}
