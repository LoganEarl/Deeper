package world.entity.skill;

import com.sun.istack.internal.Nullable;
import world.entity.Entity;
import world.entity.StatContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static world.entity.skill.Skill.VisibilityType.*;

public enum Skill {
    //Stabilize===========================
    stabilize1("stabilize1", "Stabilize", 500,
            new StatContainer(0, 0, 0, 12, 0, 0), visible),
    stabilize2("stabilize2", "Improved Stabilize", 2000,
            new StatContainer(0, 0, 0, 20, 0, 0), hiddenUntilSkills,
            stabilize1),
    stabilize3("stabilize3", "Refined Stabilize", 8000,
            new StatContainer(0, 0, 0, 38, 0, 0), hiddenUntilSkills,
            stabilize2),

    //Recover===========================
    recover1("recover1", "Recover", 250,
            new StatContainer(0, 0, 0, 0, 14, 10), visible),
    recover2("recover2", "Improved Recover", 1000,
            new StatContainer(0, 0, 0, 0, 28, 20), hiddenUntilSkills,
            recover1),
    recover3("recover3", "Unnatural Recover", 4000,
            new StatContainer(0, 0, 0, 0, 62, 40), hiddenUntilLearnable,
            recover2),
    recover4("recover4", "Supernatural Recover", 16000,
            new StatContainer(0, 0, 0, 0, 100, 88), hiddenUntilLearnable,
            recover3),

    //Dodge===========================
    dodge1("dodge1", "Dodge", 750,
            new StatContainer(0, 12, 0, 0, 0, 0), visible),
    dodge2("dodge2", "Improved Dodge", 3000,
            new StatContainer(0, 32, 0, 0, 0, 0), hiddenUntilSkills,
            dodge1),
    dodge3("dodge3", "Unnatural Dodge", 6000,
            new StatContainer(0, 64, 0, 0, 0, 0), hiddenUntilLearnable,
            dodge2),
    dodge4("dodge4", "Supernatural Dodge", 24000,
            new StatContainer(0, 120, 0, 0, 0, 0), hiddenUntilLearnable,
            dodge3),

    //Deflect===========================
    deflect1("deflect1", "Deflect", 750,
            new StatContainer(12, 0, 0, 0, 0, 0), visible),
    deflect2("deflect2", "Improved Deflect", 3000,
            new StatContainer(32, 0, 0, 0, 0, 0), hiddenUntilSkills,
            deflect1),
    deflect3("deflect3", "Unnatural Deflect", 6000,
            new StatContainer(64, 0, 0, 0, 0, 0), hiddenUntilLearnable,
            deflect2),
    deflect4("deflect4", "Supernatural Deflect", 24000,
            new StatContainer(120, 0, 0, 0, 0, 0), hiddenUntilLearnable,
            deflect3);


    private String savableName;
    private String displayName;
    private int xpCost;
    private Skill[] requiredSkills;
    private StatContainer requiredStats;
    private VisibilityType visibilityType;

    Skill(String savableName, String displayName, int xpCost, @Nullable StatContainer requiredStats, VisibilityType visibilityType, Skill... requiredSkills) {
        this.displayName = displayName;
        this.savableName = savableName;
        this.xpCost = xpCost;
        this.requiredSkills = requiredSkills;
        this.requiredStats = requiredStats;
        this.visibilityType = visibilityType;
    }

    public String getSavableName() {
        return savableName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static List<Skill> getSkillsThatRequire(Skill skill){
        List<Skill> toReturn = new ArrayList<>();
        for (Skill s : Skill.values()) {
            if(Arrays.asList(s.requiredSkills).contains(skill))
                toReturn.add(s);
        }
        return toReturn;
    }

    public int getXpCost() {
        return xpCost;
    }

    public StatContainer getRequiredStats() {
        return requiredStats;
    }

    public Skill[] getRequiredSkills() {
        return requiredSkills;
    }

    public static Skill getSkillOfDisplayName(String displayName) {
        for (Skill skill : Skill.values()) {
            if (skill.displayName.toLowerCase().equals(displayName.toLowerCase()))
                return skill;
        }
        return null;
    }

    public static Skill getSkillOfDatabaseName(String databaseName) {
        for (Skill skill : Skill.values()) {
            if (skill.savableName.toLowerCase().equals(databaseName.toLowerCase()))
                return skill;
        }
        return null;
    }

    public boolean isLearnableByEntity(Entity entity){
        for(Skill skill:requiredSkills)
            if(!SkillTable.entityHasSkill(entity,skill))
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
        switch (visibilityType) {
            case visible:
                return true;
            case hiddenUntilSkills:
                for (Skill prereq : requiredSkills)
                    if (!SkillTable.entityHasSkill(entity, prereq)) return false;
                return true;
            case hiddenUntilLearnable:
                for (Skill prereq : requiredSkills)
                    if (!SkillTable.entityHasSkill(entity, prereq)) return false;
            case hiddenUntilStats:
                StatContainer stats = entity.getStats();
                return stats.getStrength() >= requiredStats.getStrength() &&
                        stats.getDexterity() >= requiredStats.getDexterity() &&
                        stats.getIntelligence() >= requiredStats.getIntelligence() &&
                        stats.getWisdom() >= requiredStats.getWisdom() &&
                        stats.getFitness() >= requiredStats.getFitness() &&
                        stats.getToughness() >= requiredStats.getToughness();
            case hidden:
                return false;
        }
        return false;
    }

    public enum VisibilityType {
        visible, hiddenUntilSkills, hiddenUntilStats, hiddenUntilLearnable, hidden
    }
}
